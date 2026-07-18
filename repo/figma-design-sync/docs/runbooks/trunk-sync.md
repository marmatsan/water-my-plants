---
title: Figma trunk sync
type: runbook
scope: repo/figma-design-sync
owner: figma-design-sync
status: active
last-reviewed: 2026-07-18
review-cycle-days: 90
sources:
  - .teamcity/settings.kts
  - repo/figma-design-sync/tools/src/app/sync-trunk-design-model.mcp.ts
---

# Figma Trunk Sync Runbook

## Purpose

`main` is the source of truth for the dependency design model. Figma is
considered synchronized only when the configured metadata page stores the same
`modelHash` generated from `main`.

This runbook is the official execution path. Keep reusable details in the
fine-grained runbooks:

| Runbook | Use it for |
|---------|------------|
| [official-artifact-visual-sync.md](official-artifact-visual-sync.md) | Choosing and validating the TeamCity `design-model.json` artifact, and deciding whether branch-local visual iteration is allowed. |
| [mcp-chunk-transport.md](mcp-chunk-transport.md) | Building the MCP bundle, staging official payloads through PNG or chunk fallback, running targets, and writing metadata. |
| [visual-sync-efficiency.md](visual-sync-efficiency.md) | Reading the visual plan, probing MCP capabilities, and resuming checkpointed execution without repeating completed work. |
| [target-scopes.md](../reference/target-scopes.md) | Understanding the complete target order and choosing partial diagnostic scopes. |
| [visual-sync-contract.md](../reference/visual-sync-contract.md) | Validating the expected Figma component, connector, layout, and locking behavior. |
| [troubleshooting.md](troubleshooting.md) | Diagnosing failed or visually incorrect sync runs. |

## Ownership

TeamCity owns the repository workflow:

- Pull request `CI` runs repository verification. It must not generate or
  publish `build/reports/figma-sync/design-model.json`.
- Post-merge `Figma Sync` runs on `main`, generates the only authoritative
  `design-model.json`, publishes it as an artifact, and verifies Figma metadata
  with `checkFigmaTrunkSync`.
- The Figma write step is currently MCP-operated. When repairing a failed
  `Figma Sync` run on `main`, use the `design-model.json` artifact published by
  TeamCity's `Generate main design model` job. Do not regenerate the model from
  a feature branch to repair `main`.

Treat artifact generation, metadata verification, and visual writing as
separate phases. A TeamCity `Figma Sync` run can successfully generate and
publish a valid official `design-model.json` artifact while the later MCP
visual write still fails because the Figma component contract or writer code is
stale. Do not interpret a valid artifact, or a green repository check, as proof
that the visual MCP write has completed.

Local execution is useful for diagnosis, but it is not required before every
commit because Figma sync depends on external Figma state, credentials, and the
MCP write flow.

For fast visual iteration on component shape, colors, connectors, layout, or
fixture data, use [visual-preview.md](visual-preview.md). Preview is
intentionally non-authoritative and must not write official metadata.

## Sources

| Source | Value |
|--------|-------|
| Figma page URL | `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908` |
| Figma file key | `YBZXsd8oyGLbcI2KWxJvRK` |
| Metadata page node id | `62934:908` |
| Shared plugin data namespace | `water_my_plants_sync` |
| Temporary staging namespace | `water_my_plants_sync_staging` |
| Generated model artifact | `build/reports/figma-sync/design-model.json` |
| Repository versions file | `repo/dependency-catalog/versions.properties` |
| Figma versions section | `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62936-183` |
| Figma UML documentation page | `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386` |
| Root settings file | `settings.gradle.kts` |

## Official Execution Path

1. Let TeamCity run `Figma Sync` on `<default>` / `main`.
2. Download the `build/reports/figma-sync` artifact from `Figma Sync > Generate
   main design model`. Keep `design-model.json`, `visual-sync-plan.json`, and
   both generated runner directories together.
3. Validate the artifact with
   [official-artifact-visual-sync.md](official-artifact-visual-sync.md).
4. Use the generated runner whose manifest identity matches the official
   artifact. Rebuild only for branch-local writer diagnosis; the official plan
   must classify any different `writerHash` through its writer-scope
   fingerprints, or fail closed to a full visual plan.
5. Stage the official model and generated MCP script through the PNG payload
   transport, or the chunk fallback when needed, using the process
   documented in [mcp-chunk-transport.md](mcp-chunk-transport.md).
6. Follow `visual-sync-plan.json`. A `full` plan executes every generated MCP
   file; a `partial` plan executes `preflight` plus its listed scopes; `none`
   skips visual and metadata writes. Within the selected plan, execute files in
   lexical order and checkpoint each result. Catalog targets remain root by
   root and finish with cleanup-only calls. Every visual call uses
   `writeMetadata=false`.
7. Check every managed Figma section against
   [visual-sync-contract.md](../reference/visual-sync-contract.md).
8. After all visual targets are correct, run only the `metadata` target with
   `writeMetadata=true`.
9. Optionally rerun only TeamCity `Check Figma trunk sync`, or run
    `checkFigmaTrunkSync` locally, as an early diagnostic after writing
    metadata.
10. Rerun the complete TeamCity `Figma Sync` pipeline with
    `.\gradlew.bat rerunTeamCityFigmaSync -PfigmaTeamCityWait=true`. Confirm that
    `Generate main design model`, `Check Figma trunk sync`, and the aggregate
    pipeline all succeed so TeamCity publishes a successful final status.

Do not write metadata before visual targets are reconciled. `checkFigmaTrunkSync`
trusts the metadata hash, so premature metadata can make CI pass while Figma is
still visually stale.

A successful standalone `Check Figma trunk sync` does not change the result of
an earlier failed aggregate `Figma Sync` run. The complete pipeline must be
rerun after the MCP write. Visual staleness is determined by `modelHash`,
`writerHash`, model target fingerprints, and writer scope fingerprints.
`gitSha` identifies the official
artifact and checkpoint but does not invalidate unchanged visuals by itself.
Follow the generated plan and fail closed to a full visual run when its identity
or previous Figma metadata cannot be validated.

## Failure Recovery

When a visual target fails:

- Fix that target's component or TypeScript contract.
- Merge the fix to `main` if it affects generated model content or the official
  sync code used by TeamCity.
- Regenerate the official TeamCity artifact when model content changes.
- Use `--allow-partial=true` only to diagnose or verify the focused repair.
- Complete every scope selected by the new TeamCity visual plan before writing
  metadata. Mapped writer changes may select a target family; shared or
  unexplained writer changes select the complete target set. Ad hoc partial
  success does not complete an official synchronization.

Branch-local visual iteration with an already-official artifact is allowed only
for visual representation changes. The rules are in
[official-artifact-visual-sync.md](official-artifact-visual-sync.md).

## Verify

`checkFigmaTrunkSync` reads Figma shared plugin data through the Figma REST API
and compares it with a freshly generated model from the current checkout.

```powershell
$line = Get-Content -Path .env | Where-Object { $_ -like 'FIGMA_FILE_CONTENT_ACCESS_TOKEN=*' } | Select-Object -First 1
$env:FIGMA_FILE_CONTENT_ACCESS_TOKEN = ($line -split '=', 2)[1].Trim('"')
.\gradlew.bat checkFigmaTrunkSync
```

The token must have read access to the Figma file and the `file_content:read`
scope.

The check fails when:

- Figma does not expose shared plugin data for `water_my_plants_sync`.
- Figma is missing `modelHash` or `gitSha`.
- Figma's `modelHash` differs from the model generated from the checkout.
- The visual MCP sync refused to write metadata because required variables,
  sections, instances, or connector templates were missing.

The check contract is intentionally narrow:

- Generate the model from the current checkout.
- Read `water_my_plants_sync.modelHash` from the configured Figma page.
- Pass only when both hashes are identical.

It does not prove that every `.tree node`, connector, variable, or UML section
is visually correct. Those are obligations of the MCP write step and the UML
publication runbook.

## TeamCity Integration

`CI` is the pull request gate required by GitHub branch protection:

- Run normal verification.
- Do not run `generateFigmaDesignModel`.
- Do not publish `build/reports/figma-sync/design-model.json`.

`CI` does not generate or verify Figma sync state because Figma represents
`main`, not every short-lived branch.

`Figma Sync` is the post-merge pipeline for `main`:

- Trigger after `CI` succeeds on `<default>`.
- Run `generateFigmaDesignModel` from `main`.
- Publish `build/reports/figma-sync/design-model.json`.
- Run `checkFigmaTrunkSync` against the metadata currently stored in Figma.
- Verify every added or changed `.puml` diagram has been rendered and published
  to the Figma UML documentation page in its own locked section.

Until the Figma write step is automated inside TeamCity, the first `Figma Sync`
run after a model-affecting merge can identify that `main` is not yet reflected
in Figma, but the actual write still happens through MCP. After the MCP sync
writes metadata with the TeamCity artifact, rerun `Figma Sync`. A local
`.\gradlew.bat checkFigmaTrunkSync` run is diagnostic only and does not replace
the TeamCity status.

TeamCity parameter setup for verification:

- Secure parameter: `figma.file.content.access.token`
- Environment parameter:
  `env.FIGMA_FILE_CONTENT_ACCESS_TOKEN=%figma.file.content.access.token%`

## Release Barrier

Before creating `release/<version>`:

- `main` must pass the normal build and test suite.
- `generateFigmaDesignModel` must produce the current model.
- Figma must be synced through the MCP step.
- Every `.puml` diagram added or changed on `main` must already live in the
  Figma UML documentation page.
- `checkFigmaTrunkSync` must pass.

Only after that barrier is green:

- Create `release/<version>`.
- Stabilize the release branch.
- Merge `release/<version>` into `main`.
- Publish the stable version.

After merging changes that affect generated model content, regenerate the model
from `main` and sync Figma again. Commits that only change traceability
metadata, CI settings, or unrelated files do not require a Figma sync if
`modelHash` stays unchanged.

## Prerequisites

- Use a TeamCity Figma Sync run for the exact merged `main` revision.
- Confirm the CI pipeline succeeded before accepting its generated model.
- Use a write-capable MCP endpoint and the TeamCity-generated visual plan.

## Prohibited Actions

- Do not generate or publish the official model from a feature branch.
- Do not write official metadata after an incomplete plan.
- Do not replace the aggregate post-merge verification with a standalone check.
