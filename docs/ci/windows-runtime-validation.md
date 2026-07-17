# Windows CI Runtime Validation

Use this runbook to validate `docs/ci/windows-runtime.yaml` against the Windows
services installed on the TeamCity host. The YAML is a reviewed inventory, not
an installation script.

Use
[`../runbooks/teamcity-cloudflare-access.md`](../runbooks/teamcity-cloudflare-access.md)
for service recovery, health checks, and security constraints.

## Validation Procedure

1. Open an elevated PowerShell session on the TeamCity host.
2. Run `Get-Service -Name TeamCity,TCBuildAgent,Cloudflared` and confirm that all
   three services exist and are running.
3. Query `Win32_Service` as documented in the operational runbook and compare
   each service name, display name, startup mode, and service account with the
   YAML.
4. Do not inspect, print, or copy the Cloudflared service command line because
   it can contain the tunnel credential.
5. Confirm the local TeamCity origin, public HTTPS route, webhook bypass, and
   TeamCity CLI health checks from the operational runbook.
6. Update `validation.lastValidatedOn` only after every service entry matches
   the active host.
7. Run `.\gradlew.bat checkCiWindowsRuntimeFreshness` and the normal repository
   checks.

The freshness task is intentionally non-blocking: an expired date produces a
visible CI warning but does not prevent a merge.
