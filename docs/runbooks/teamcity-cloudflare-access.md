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
- TeamCity CLI is authenticated to the HTTPS server with a TeamCity access
  token stored in the system keyring.
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

## Mutating CLI Requests And CSRF

Read-only CLI commands such as `teamcity auth status`, `teamcity run list`, and
`teamcity run view` work through the public HTTPS route. A mutating command such
as `teamcity run start` or `teamcity run restart` can fail with:

```text
403 Forbidden: failed CSRF check
```

Cloudflare Access can add a `CF_Authorization` cookie to the authenticated
request. TeamCity then treats the POST as cookie-authenticated and requires an
`X-TC-CSRF-Token` value that TeamCity CLI does not currently provide.

Use one of these recovery paths:

1. Rerun the build from the authenticated TeamCity UI.
2. On the TeamCity server host only, call the private origin with a TeamCity
   Bearer token and no Cloudflare headers or cookies.

For the second option, load `TEAMCITY_TOKEN` from an approved local secret
provider, then queue the complete Figma Sync pipeline:

```powershell
$headers = @{
    Authorization = "Bearer $env:TEAMCITY_TOKEN"
    Accept = "application/json"
}
$body = @{
    buildType = @{ id = "WaterMyPlants_WaterMyPlantsFigmaSync" }
    branchName = "main"
} | ConvertTo-Json -Depth 4 -Compress

Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8111/app/rest/buildQueue" `
    -Headers $headers `
    -ContentType "application/json" `
    -Body $body

Remove-Item Env:TEAMCITY_TOKEN -ErrorAction SilentlyContinue
```

Do not expose `localhost:8111` outside the TeamCity host, do not bypass
Cloudflare for the public REST API, and do not add a build comment unless the
token has the TeamCity `Comment build` permission.

## Verification

After configuration or recovery:

1. Open the TeamCity UI through the HTTPS hostname and complete interactive
   authentication.
2. Run `teamcity auth status` and one read-only run query through the wrapper.
3. Confirm a GitHub App test webhook and a real `push` delivery return HTTP
   `200`.
4. Confirm GitHub push or pull request events start the expected TeamCity
   pipeline and publish its repository check.
5. Run the manual validation in
   [`../ci/external-topology-validation.md`](../ci/external-topology-validation.md).

## Secret Rotation

- When the TeamCity access token expires, run `teamcity auth login` against the
  HTTPS server and approve only the required permissions. The replacement token
  remains in the system keyring.
- When the Cloudflare service token rotates, replace
  `TeamCityCloudflareAccess` in SecretStore and leave the PowerShell wrapper
  unchanged.
- When the GitHub webhook secret rotates, update GitHub and the TeamCity GitHub
  App connection in the same operation, then send a test delivery.
