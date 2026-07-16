# TeamCity Cloudflare Access Runbook

## Purpose

Use this runbook to configure, validate, or recover access to the TeamCity
server at `https://teamcity.marmatsan.dev`.

The public hostname is exposed through Cloudflare Tunnel and has three distinct
access paths. Do not collapse them into one policy:

| Client | Cloudflare policy | TeamCity authentication |
|--------|-------------------|-------------------------|
| Browser | Interactive `Allow` policy | TeamCity user session |
| TeamCity CLI | `Service Auth` policy | TeamCity access token |
| GitHub App webhook | Path-specific `Bypass` policy | GitHub webhook signature |

The repository documents the contract, but Cloudflare, GitHub, and TeamCity
credentials remain operator-managed state and must not be committed.

## Prerequisites

- Cloudflare Tunnel routes the public hostname to the private TeamCity origin.
- The browser `Allow` policy grants only intended users access to the UI.
- A Cloudflare service token is included by the CLI `Service Auth` policy.
- The Access application keeps at least one `Allow` policy in addition to
  `Service Auth`, which permits subsequent requests to use the raw
  `cf-access-token` JWT instead of resending the service-token pair.
- TeamCity CLI is authenticated to the HTTPS server with a TeamCity access
  token stored in the system keyring.
- A separate TeamCity automation token with only project view, build
  configuration view, build runtime view, and run build permissions is stored
  in PowerShell SecretStore.
- The GitHub App webhook uses a dedicated bypass destination for only
  `/app/webhooks/githubapp`.
- PowerShell SecretManagement and SecretStore are installed for local service
  token storage.

## Store The Cloudflare Service Token

Store the Cloudflare service token as a `PSCredential`. Use its client ID as the
username and its client secret as the password:

```powershell
Set-Secret -Vault TeamCitySecrets -Name TeamCityCloudflareAccess -Secret (Get-Credential)
```

Configure SecretStore according to the workstation security policy. Never put
the client ID, client secret, or TeamCity access token in the repository,
PowerShell profile, command history, or `teamcity.toml`.

## Store The TeamCity Automation Token

Create a TeamCity access token named `figma-sync-automation` with only these
permissions:

- `View project and all parent projects (VIEW_PROJECT)`;
- `View build configuration settings (VIEW_BUILD_CONFIGURATION_SETTINGS)`;
- `View build runtime parameters and data (VIEW_BUILD_RUNTIME_DATA)`;
- `Run build (RUN_BUILD)`.

Store the token as a `SecureString` in the same vault:

```powershell
$teamCityToken = Read-Host "TeamCity automation token" -AsSecureString
Set-Secret -Vault TeamCitySecrets -Name TeamCityAutomationToken -Secret $teamCityToken
Remove-Variable teamCityToken
```

Do not reuse a broad administrator token or the TeamCity CLI token stored in
the system keyring. The repository client needs only enough access to inspect
active runs and queue `Figma Sync`.

## Run TeamCity CLI Through Service Auth

The PowerShell profile may expose a `teamcity` wrapper that loads the
Cloudflare credential only for the duration of one command:

```powershell
function teamcity {
    $credential = Get-Secret TeamCityCloudflareAccess -ErrorAction Stop
    $previousId = $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID
    $previousSecret = $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET
    $exitCode = 1

    try {
        $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID = $credential.UserName
        $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET =
            $credential.GetNetworkCredential().Password
        & teamcity.exe @args
        $exitCode = $LASTEXITCODE
    } finally {
        if ($null -eq $previousId) {
            Remove-Item Env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID -ErrorAction SilentlyContinue
        } else {
            $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID = $previousId
        }

        if ($null -eq $previousSecret) {
            Remove-Item Env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET -ErrorAction SilentlyContinue
        } else {
            $env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET = $previousSecret
        }

        Remove-Variable credential -ErrorAction SilentlyContinue
    }

    $global:LASTEXITCODE = $exitCode
}
```

Verify both authentication layers and confirm the Cloudflare secret is not left
in the shell environment:

```powershell
teamcity auth status
Test-Path Env:TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET
```

The first command must report the HTTPS TeamCity server as API compatible. The
second command must return `False` after the wrapper exits.

## Configure The GitHub App Webhook

Use the TeamCity GitHub App connection's webhook URL:

```text
https://teamcity.marmatsan.dev/app/webhooks/githubapp
```

The GitHub App subscribes to the events required by TeamCity:

- `push`;
- `pull_request`;
- `check_suite`;
- `check_run`.

The webhook secret configured in GitHub and TeamCity must match. Cloudflare
Access must bypass interactive authentication only for the exact webhook path.
Do not bypass the whole TeamCity hostname or its REST API.

Validate the route by creating a temporary branch and checking the GitHub App
advanced delivery log. A valid `push` delivery must receive HTTP `200` from
TeamCity. Delete the temporary branch after validation.

## Mutating Requests And CSRF

Read-only CLI commands such as `teamcity auth status`, `teamcity run list`, and
`teamcity run view` work through the public HTTPS route. A mutating command such
as `teamcity run start` or `teamcity run restart` can fail with:

```text
403 Forbidden: failed CSRF check
```

Cloudflare Access can add a `CF_Authorization` cookie to the authenticated
request. TeamCity then treats the POST as cookie-authenticated and requires an
`X-TC-CSRF-Token` value that TeamCity CLI does not currently provide.

The CSRF value returned by `authenticationTest.html?csrf` is session-bound.
Cloudflare Service Auth creates a `CF_Authorization` cookie, but TeamCity does
not create its own stable session cookie for Bearer-authenticated requests in
this route. Two consecutive CSRF reads therefore return different values, even
when the HTTP client reuses one cookie container.

TeamCity recommends clearing session cookies for unsafe requests authenticated
with a Bearer token. The repository client follows that contract:

1. It performs read-only idempotency checks with the Cloudflare service-token
   headers.
2. It captures the short-lived Access JWT returned in `CF_Authorization`.
3. It sends the queue POST in a new cookie-free session using
   `cf-access-token` and the TeamCity Bearer token.

The POST does not resend the service-token pair because that would cause
Cloudflare to inject a new `CF_Authorization` cookie into the request seen by
TeamCity. Do not remove the application's `Allow` policy while this transport
is in use; Cloudflare requires service-token headers on every request when an
application has only `Service Auth` policies.

Use the repository-owned HTTPS client for the supported rerun path:

```powershell
pwsh -File tools/teamcity/invoke-figma-sync-rerun.ps1 -ValidateOnly
pwsh -File tools/teamcity/invoke-figma-sync-rerun.ps1 -Wait
```

The client:

- accepts only the public HTTPS TeamCity URL;
- is fixed to `WaterMyPlants_WaterMyPlantsFigmaSync` on `main`;
- loads Cloudflare and TeamCity credentials from SecretStore;
- uses one session for read-only active-run checks and a separate cookie-free
  session for the queue POST;
- converts the Cloudflare authorization cookie into the raw
  `cf-access-token` header for the cookie-free POST;
- reuses a recently queued or running `Figma Sync` instead of creating a
  duplicate, while ignoring stale runs that TeamCity has left active;
- never retries an uncertain POST before checking whether TeamCity accepted it;
- does not print either secret.

Run its offline contract tests after changing the client:

```powershell
pwsh -File tools/teamcity/tests/test-teamcity-https-client.ps1
```

The TeamCity UI remains the fallback when the local secret provider or HTTPS
client is unavailable. Do not disable CSRF, copy session cookies, expose
`localhost:8111`, or bypass Cloudflare for the public REST API.

## Verification

After configuration or recovery:

1. Open the TeamCity UI through the HTTPS hostname and complete interactive
   authentication.
2. Run `teamcity auth status` and one read-only run query through the wrapper.
3. Run the repository client with `-ValidateOnly`; it must authenticate both
   access boundaries and return state `Validated` without queueing a build.
4. Confirm a GitHub App test webhook and a real `push` delivery return HTTP
   `200`.
5. Confirm GitHub push or pull request events start the expected TeamCity
   pipeline and publish its repository check.
6. Run the manual validation in
   [`../ci/external-topology-validation.md`](../ci/external-topology-validation.md).

## Secret Rotation

- When the TeamCity access token expires, run `teamcity auth login` against the
  HTTPS server and approve only the required permissions. The replacement token
  remains in the system keyring.
- When `figma-sync-automation` expires, create a replacement with the same
  minimal permissions and replace `TeamCityAutomationToken` in SecretStore.
- When the Cloudflare service token rotates, replace
  `TeamCityCloudflareAccess` in SecretStore and leave the PowerShell wrapper
  unchanged.
- When the GitHub webhook secret rotates, update GitHub and the TeamCity GitHub
  App connection in the same operation, then send a test delivery.
