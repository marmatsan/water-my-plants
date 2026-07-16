# External CI Topology Validation

Use this runbook to validate `docs/ci/external-topology.yaml` against the active
services. The review is manual because Cloudflare, GitHub, TeamCity, and Figma
configuration is not fully represented by repository code.

Use
[`../runbooks/teamcity-cloudflare-access.md`](../runbooks/teamcity-cloudflare-access.md)
for the executable HTTPS, CLI, and GitHub App webhook configuration and
recovery procedure.

## Validation Procedure

1. Confirm the public TeamCity UI route uses Cloudflare Access and Cloudflare
   Tunnel.
2. Confirm TeamCity CLI requests require Cloudflare Service Auth and a TeamCity
   access token.
3. Run
   `pwsh -File tools/teamcity/invoke-figma-sync-rerun.ps1 -ValidateOnly` and
   confirm it validates the HTTPS session without queueing a build. Confirm the
   TeamCity Access application still has both its user `Allow` policy and CLI
   `Service Auth` policy so the mutation request can use `cf-access-token`.
4. Confirm the dedicated GitHub App webhook path bypasses interactive Access,
   validates the webhook signature, and reaches TeamCity through the tunnel.
5. Confirm TeamCity obtains sources and publishes checks through the GitHub App.
6. Confirm the TeamCity server dispatches both pipelines to the expected build
   agent and that Figma Sync remains restricted to `main`.
7. Confirm the build agent can read Figma metadata and that visual writes still
   require an operator-approved Codex/MCP action.
8. Compare every node and connection in the YAML with the verified state.
9. Update `validation.lastValidatedOn` only after all checks pass.
10. Run `.\gradlew.bat checkCiExternalTopologyFreshness` and the normal
   repository checks.

Do not record tokens, secrets, concrete account identifiers, personal names, or
layout coordinates in the topology file. The freshness task is intentionally
non-blocking: an expired date produces a visible CI warning but does not prevent
a merge.
