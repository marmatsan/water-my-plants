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
3. Confirm the dedicated GitHub App webhook path bypasses interactive Access,
   validates the webhook signature, and reaches TeamCity through the tunnel.
4. Confirm TeamCity obtains sources and publishes checks through the GitHub App.
5. Confirm the TeamCity server dispatches both pipelines to the expected build
   agent and that Figma Sync remains restricted to `main`.
6. Confirm the build agent can read Figma metadata and that visual writes still
   require an operator-approved Codex/MCP action.
7. Compare every node and connection in the YAML with the verified state.
8. Update `validation.lastValidatedOn` only after all checks pass.
9. Run `.\gradlew.bat checkCiExternalTopologyFreshness` and the normal
   repository checks.

Do not record tokens, secrets, concrete account identifiers, personal names, or
layout coordinates in the topology file. The freshness task is intentionally
non-blocking: an expired date produces a visible CI warning but does not prevent
a merge.
