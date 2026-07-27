---
title: TeamCity Cloudflare Access operations
type: runbook
scope: repository-ci
owner: ci-platform
status: active
last-reviewed: 2026-07-19
review-cycle-days: 90
sources:
  - .teamcity/settings.kts
  - docs/ci/external-topology.yaml
  - docs/ci/windows-runtime.yaml
  - repo/water-my-plants-project-config/plugin/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/figma/task/RerunTeamCityFigmaSyncTask.kt
  - repo/water-my-plants-project-config/plugin/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/teamcity/auth/EnvironmentTeamCityAutomationCredentialsProvider.kt
  - repo/figma-documentation-sync/teamcity-adapter/src/main/kotlin/com/marmatsan/figmaDocumentationSync/teamcityAdapter/TeamCityRestRunStarter.kt
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/teamcity/RunTeamCityInfrastructureHealthTask.kt
  - .teamcity/scripts/invoke-infrastructure-health-at-startup.ps1
  - .teamcity/scripts/install-infrastructure-health-startup-task.ps1
---

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
  in an operator-managed vault and exposed only to the rerun task process.
- The GitHub App webhook uses a dedicated bypass destination for only
  `/app/webhooks/githubapp`.
- PowerShell SecretManagement and SecretStore are installed for local service
  token storage.

## Windows Service Runtime

The reviewed service inventory is versioned in
[`../ci/windows-runtime.yaml`](../ci/windows-runtime.yaml). Follow
[`../ci/windows-runtime-validation.md`](../ci/windows-runtime-validation.md)
before updating its validation date.

The supported local runtime uses three automatic Windows services:

| CI actor | Service name | Windows display name | Service account | Responsibility |
|----------|--------------|----------------------|-----------------|----------------|
| TeamCity Server | `TeamCity` | `TeamCity Server` | `NT SERVICE\TeamCity` | Hosts the TeamCity server and owns its data directory. |
| Build Agent | `TCBuildAgent` | `TeamCity Build Agent` | `NT SERVICE\TCBuildAgent` | Executes repository jobs in the agent work directories. |
| Cloudflare Tunnel | `Cloudflared` | `Cloudflared agent` | `LocalSystem` | Publishes the private TeamCity origin through Cloudflare Tunnel. |

Inspect status, startup mode, and service identity from an elevated PowerShell
session:

```powershell
Get-Service -Name TeamCity,TCBuildAgent,Cloudflared
Get-CimInstance Win32_Service |
    Where-Object Name -In @("TeamCity", "TCBuildAgent", "Cloudflared") |
    Select-Object Name,DisplayName,State,StartMode,StartName
```

Do not print or document the `Cloudflared` service command line. The installed
service definition can contain the tunnel credential.

All three services should report `Running` and `Automatic`. They have no
repository-managed Windows dependency relationship, so use this recovery order
when the host restarts or the installation is repaired:

1. start `TeamCity` and wait for `http://localhost:8111` to respond;
2. start `TCBuildAgent` and confirm the agent is connected and authorized;
3. start `Cloudflared` and validate the public HTTPS route.

Use the reverse order for planned shutdown:

```powershell
Stop-Service Cloudflared
Stop-Service TCBuildAgent
Stop-Service TeamCity
```

Service management requires an elevated shell. Once the services are
installed, `runAll.bat start` is not the canonical runtime and should not be
used alongside the Windows services.

### Recover an Agent Upgrade Blocked by Service Permissions

After a TeamCity server upgrade, the Windows agent can remain disconnected with
`Agent has unregistered (will upgrade)` even though `TCBuildAgent` reports
`Running`. Confirm this failure mode in
`C:\TeamCity\buildAgent\logs\upgrade.log`; the relevant signature is
`OpenSCManager failed - Access is denied` or Windows system error `5` while the
updater tries to stop or start `TCBuildAgent`.

The virtual service account is intentionally not given broad Windows service
manager permissions. Do not work around the failure by killing the service
wrapper, deleting the `update` or `backup` directories, running the agent as
`LocalSystem`, or granting `FullControl` over `C:\TeamCity`.

Before recovery, confirm that the server is healthy and that the agent is not
running a build. Drain its queue or temporarily disable the agent because a
manually started, authorized agent can accept queued builds immediately.

From an elevated PowerShell session, stop the service and start the downloaded
agent update under the elevated identity:

```powershell
Stop-Service TCBuildAgent
Set-Location C:\TeamCity\buildAgent\bin
.\agent.bat start
```

Keep that session open. Wait until the TeamCity Agents page reports the agent
as connected and its version matches the server build number. If the command
continues running in console mode, use a second elevated PowerShell session to
return ownership to the Windows service after all active builds finish:

```powershell
Set-Location C:\TeamCity\buildAgent\bin
.\agent.bat stop
Start-Service TCBuildAgent
```

Verify that `TCBuildAgent` is `Running`, the agent is connected and authorized,
and the agent and server build numbers still match. If this recovery is needed
repeatedly, treat changing the service identity or service ACL as a reviewed
Windows-security change; do not expand permissions ad hoc during an incident.

Validate the local and public boundaries after recovery:

```powershell
Get-Service -Name TeamCity,TCBuildAgent,Cloudflared
curl.exe -I http://localhost:8111
curl.exe -I https://teamcity.marmatsan.dev
curl.exe -sS -D - -o NUL https://teamcity.marmatsan.dev/app/webhooks/githubapp
teamcity auth status
```

An unauthenticated request to the public root should receive the Cloudflare
Access redirect. A `GET` to the webhook path should reach TeamCity and return
`400 Bad Request` because it is not a signed GitHub `POST`; an Access redirect
there means the path-specific bypass policy is broken.

## Queue Infrastructure Health After Startup

The daily 06:00 TeamCity trigger remains useful when the server runs
continuously, but it cannot recover a time slot missed while this workstation
was powered off. Windows Task Scheduler therefore queues the same non-gating
pipeline after startup:

```powershell
pwsh -NoProfile -File .teamcity/scripts/install-infrastructure-health-startup-task.ps1 -RunNow
```

The task uses an `AtStartup` trigger and `StartWhenAvailable`. It runs under the
interactive user identity so PowerShell SecretStore remains available; when no
user session exists at boot, Windows starts the task once that condition becomes
available. The adapter waits for `http://127.0.0.1:8111/healthCheck/ready`, loads
only `TeamCityAutomationToken`, and invokes the Kotlin
`runTeamCityInfrastructureHealth` task. The Kotlin REST adapter allows plain
HTTP only for a loopback host, never follows redirects, and does not use the
public Cloudflare route.

Inspect or test the installed task without exposing credentials:

```powershell
Get-ScheduledTask -TaskName "Water My Plants - Infrastructure Health at startup"
Get-ScheduledTaskInfo -TaskName "Water My Plants - Infrastructure Health at startup"
Start-ScheduledTask -TaskName "Water My Plants - Infrastructure Health at startup"
```

Keep the scheduled action pointed at the stable repository checkout. Re-run the
installer if that checkout moves. Do not place the TeamCity token in the task
arguments, environment block, or exported task XML.

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
with a Bearer token. TeamCity CLI 1.3.0 still retains the Cloudflare cookie for
the mutating request, so the repository task separates its read and write
transports:

1. It exchanges the Cloudflare service-token headers for the short-lived Access
   JWT returned in `CF_Authorization`.
2. It invokes `teamcity.exe` with temporary `TEAMCITY_TOKEN` and
   `TEAMCITY_HEADER_CF_ACCESS_TOKEN` values only for active-run checks and
   waiting.
3. It queues the build through `TeamCityRestRunStarter`, which sends Bearer and
   `CF-Access-Token` headers without installing a cookie handler and without
   following redirects.

The POST neither resends the service-token pair nor replays the exchanged
`CF_Authorization` cookie. TeamCity therefore evaluates it as a Bearer request
without a session and skips the CSRF check. Do not remove the application's
`Allow` policy while this transport is in use; Cloudflare requires
service-token headers on every request when an application has only `Service
Auth` policies.

The repository-owned rerun orchestration is Kotlin. Its credential port reads
`TEAMCITY_TOKEN` and either `TEAMCITY_HEADER_CF_ACCESS_TOKEN` or the
`TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID` and
`TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET` pair. The default adapter exchanges
the service-token pair for the short-lived JWT and clears the pair from the
TeamCity CLI child environment.

SecretStore is the supported Windows workstation adapter for that port. Use the
repository-owned PowerShell launcher to validate both authentication boundaries
without queueing a build:

```powershell
.\.teamcity\scripts\invoke-figma-sync-rerun.ps1 -ValidateOnly
```

After validation succeeds, rerun the canonical pipeline and wait for its final
result:

```powershell
.\.teamcity\scripts\invoke-figma-sync-rerun.ps1
```

The launcher reads `TeamCityAutomationToken` and
`TeamCityCloudflareAccess`, rejects missing or malformed values, clears any
stale `TEAMCITY_HEADER_CF_ACCESS_TOKEN` while the Kotlin task runs, and restores
the complete previous process environment in `finally`. It never persists or
prints either secret. It owns no TeamCity policy: Cloudflare exchange, active-run
deduplication, queueing, and waiting remain in the Kotlin
`rerunTeamCityFigmaSync` task. Keep direct environment injection only as a
diagnostic fallback when repairing the launcher itself.

The Kotlin task:

- accepts only the public HTTPS TeamCity URL;
- is fixed to `WaterMyPlants_WaterMyPlantsFigmaSync` on `main`;
- obtains the Cloudflare and dedicated TeamCity credentials through a port;
- converts the Cloudflare authorization cookie into the raw
  `cf-access-token` header used by the CLI and REST adapters;
- delegates active-run queries and waiting to TeamCity CLI;
- queues through a Kotlin JDK HTTP adapter with no cookie store or redirects;
- reuses a queued or running `Figma Sync` instead of creating a duplicate;
- checks for an accepted active run before treating an uncertain start as a
  failure;
- does not print either secret.

The TeamCity UI remains the fallback when the local secret provider or CLI is
unavailable. Do not disable CSRF, copy session cookies, expose
`localhost:8111`, or bypass Cloudflare for the public REST API.

## Verification

After configuration or recovery:

1. Open the TeamCity UI through the HTTPS hostname and complete interactive
   authentication.
2. Run `teamcity auth status` and one read-only run query through the profile
   wrapper.
3. Run `.\.teamcity\scripts\invoke-figma-sync-rerun.ps1 -ValidateOnly`; it
   must authenticate both access boundaries and return state `Validated`
   without queueing a build.
4. Confirm a GitHub App test webhook and a real `push` delivery return HTTP
   `200`.
5. Confirm GitHub push or pull request events start the expected TeamCity
   pipeline and publish its repository check.
6. Run the manual validation in
   [`../ci/external-topology-validation.md`](../ci/external-topology-validation.md).
7. Run `Infrastructure Health` once and inspect
   `build/reports/ci-health/infrastructure-health.json`. Its scheduled runs
   validate the observable boundaries daily, but an external monitor is still
   required to detect that TeamCity itself could not schedule the check.

Server-state and artifact recovery are separate from access recovery. Follow
[`teamcity-backup-recovery.md`](teamcity-backup-recovery.md) for backup capture
and isolated restore drills.

## Secret Rotation

- When the TeamCity access token expires, run `teamcity auth login` against the
  HTTPS server and approve only the required permissions. The replacement token
  remains in the system keyring.
- When `figma-sync-automation` expires, create a replacement with the same
  minimal permissions and replace `TeamCityAutomationToken` in SecretStore.
- When the Cloudflare service token rotates, replace
  `TeamCityCloudflareAccess` in SecretStore and keep the credential-port
  environment contract unchanged.
- When the GitHub webhook secret rotates, update GitHub and the TeamCity GitHub
  App connection in the same operation, then send a test delivery.

## Recovery

Use the verification commands in this runbook to identify whether the failure
belongs to Cloudflare Access, the tunnel, TeamCity authentication, the GitHub
webhook, or the Windows services. Restore one boundary at a time and preserve
the last known valid service-token and TeamCity-token entries until the
replacement is verified.

## Prohibited Actions

- Do not expose the TeamCity origin directly to bypass Cloudflare Access.
- Do not place service-token or TeamCity-token values in source control,
  command history, screenshots, or documentation.
- Do not grant the build-agent account write access to TeamCity server data.

## Sources

- `.teamcity/settings.kts`
- `docs/ci/external-topology.yaml`
- `docs/ci/windows-runtime.yaml`
- `docs/runbooks/teamcity-backup-recovery.md`
