---
title: Figma visual preview
type: runbook
scope: repo/figma-design-sync
owner: figma-design-sync
status: active
last-reviewed: 2026-07-19
review-cycle-days: 90
sources:
  - repo/figma-design-sync/tools/src/app/sync-catalog-tree-preview.mcp.ts
  - repo/figma-design-sync/tools/scripts/write-mcp-runner.ts
  - repo/figma-design-sync/plugin/src/main/kotlin/com/marmatsan/figmaDesignSync/plugin/task/visual/GenerateCiVisualPlanTask.kt
---

# Figma Visual Preview Runbook

## Purpose

Use this runbook to iterate quickly on visual sync code, component contracts, and
layout behavior without publishing official Figma sync metadata.

Preview is not a repair path for `main`. The official publication flow remains
[trunk-sync.md](trunk-sync.md): TeamCity generates the authoritative
`design-model.json` from `main`, MCP writes the official visual state, and
metadata is written last.

## Safety Contract

Preview runners:

- use fixture or explicitly provided models only for visual feedback;
- write temporary shared plugin data under `water_my_plants_sync_preview`;
- force `writeMetadata=false`;
- reject the `metadata` target;
- require a sandbox section override for catalog tree targets unless
  `--allow-official-sections=true` is passed for supervised manual repair;
- generate only ignored files under `repo/figma-design-sync/tools/dist/`.

Catalog preview also builds a smaller MCP entrypoint,
`dist/sync-catalog-tree-preview.mcp.js`, so sandbox checks can validate catalog
layout and connector behavior without transporting the full trunk-sync bundle.

Do not use preview output to make `checkFigmaTrunkSync` pass. That check is tied
to the official namespace `water_my_plants_sync` and the TeamCity artifact from
`main`.

For visual-only tooling changes on a branch, an explicitly provided model may be
the official TeamCity `main` artifact. Treat it as a stable visual input, not as
permission to write official metadata from the branch. If the branch changes the
model content itself, merge first and regenerate the artifact through
TeamCity/main.

## Fixtures

Visual fixtures live under:

```text
repo/figma-design-sync/tools/fixtures/visual/
```

Available fixtures:

| Fixture | Purpose |
|---------|---------|
| `catalog-tree.design-model.json` | Catalog tree layout, connectors, `.tree node`, `.artifact`, `.artifacts bundle`, and `.usage chip` variants. |
| `versions.design-model.json` | Version variables and `.dependency version` visual nodes. |

Fixture models keep `branch = "main"` because the MCP bundle refuses non-main
models. They use preview `gitSha` and `modelHash` values and must not be treated
as authoritative repository state.

## Generate Preview Runner Files

Build the MCP bundle and generate runner snippets from the tools directory:

```powershell
cd repo\figma-design-sync\tools
npm ci
npm run build
node dist\write-mcp-runner.mjs --mode=preview --fixture=catalog-tree --target=waterMyPlants.plugins --section-node-id=SANDBOX_SECTION_ID
```

Catalog tree preview defaults to `--entrypoint=preview-catalog`, which stages
`dist/sync-catalog-tree-preview.mcp.js` instead of the full trunk-sync bundle.
Pass it explicitly when documenting or sharing a reproduction:

```powershell
node dist\write-mcp-runner.mjs --mode=preview --entrypoint=preview-catalog --fixture=catalog-tree --target=waterMyPlants.plugins --section-node-id=SANDBOX_SECTION_ID
```

The command writes an ignored directory under:

```text
repo/figma-design-sync/tools/dist/mcp-runners/
```

Run the generated `.mcp.js` files with Figma MCP in lexical order:

1. `00-clear-staging.mcp.js`
2. `10-designModelJson-*.mcp.js`
3. `20-script-*.mcp.js`
4. `90-finalize-staging.mcp.js`
5. `99-run-target.mcp.js`

The generated `manifest.json` records the mode, target, namespace, model hash,
entrypoint, script path, script length, staged files, and optional sandbox
section id.

Do not copy long generated source from terminal output into `use_figma`. Shell
or chat output can truncate the source before it reaches Figma. Use the
generated `.mcp.js` files directly and reduce `--chunk-size` if a preview
staging snippet is too large for the MCP transport. Every generated preview
staging snippet validates its own chunk length and the previously staged length
before writing, so truncation fails before `99-run-target.mcp.js` can mutate
visuals.
After any staging failure, rerun `00-clear-staging.mcp.js` before trying again.

The preview catalog entrypoint refuses `versions`, `metadata`, and catalog
targets without a sandbox section id.

## CI Documentation Preview

CI previews use the Kotlin planner even though the compatibility runner is
packaged by TypeScript. Start from an explicitly supplied TeamCity `main`
artifact or another non-authoritative preview model that already contains
`content.ci`; do not generate the official model locally on a feature branch.

From the repository root, generate a plan for the target being inspected:

```powershell
.\gradlew.bat generateFigmaCiVisualPlan `
  -PfigmaCiVisualDesignModel="PATH\TO\design-model.json" `
  -PfigmaCiVisualTarget=ci.overview `
  -PfigmaCiVisualPlanOutput="build\tmp\figma-preview\ci-visual-plan.json"
```

Then package the preview runner from the tools directory:

```powershell
node dist\write-mcp-runner.mjs `
  --mode=preview `
  --model="PATH\TO\design-model.json" `
  --target=ci.overview `
  --ci-visual-plan="..\..\..\build\tmp\figma-preview\ci-visual-plan.json"
```

The runner rejects a missing plan, a plan without the requested target, or
duplicate sections for that target. It embeds only the requested section in
`SYNC_OPTIONS`, leaving TypeScript responsible solely for the Figma Plugin API
adapter and measured runtime layout.

## Catalog Tree Preview

For catalog tree targets, create or duplicate a sandbox section in Figma and pass
its node id with `--section-node-id`. The sandbox section must contain compatible
templates for the target being tested:

- at least one `.tree node` instance for the target type;
- a `simple-solid_arrow` connector template when parent/child edges are tested;
- nested `.artifact`, `.artifacts bundle`, and `.usage chip` instances when
  library usage behavior is tested.

Examples:

```powershell
node dist\write-mcp-runner.mjs --mode=preview --fixture=catalog-tree --target=waterMyPlants.libraries --section-node-id=SANDBOX_SECTION_ID
node dist\write-mcp-runner.mjs --mode=preview --fixture=catalog-tree --target=waterMyPlants.plugins --section-node-id=SANDBOX_SECTION_ID
node dist\write-mcp-runner.mjs --mode=preview --fixture=catalog-tree --target=gradlePlugins.plugins --section-node-id=SANDBOX_SECTION_ID
```

Use `--allow-official-sections=true` only for supervised manual repair where the
intent is to mutate an official section without writing metadata.

## Sandbox Cleanup

Preview sections are temporary validation artifacts. Name copied sections with
the `Preview - ` prefix, use their node id only for the current preview run, and
delete them after the result has been inspected or the run has been abandoned.

Do not leave `Preview - ...` sections in the official Figma file after a visual
iteration. They are not source of truth, are not referenced by TeamCity, and
should not be reused as stable section ids in committed documentation or
scripts.

If a preview run fails before `99-run-target.mcp.js`, clear
`water_my_plants_sync_preview` staging first, then delete the temporary preview
section. The official visual sections and `water_my_plants_sync` metadata should
remain untouched.

## Version Preview

Version preview still touches the configured Figma variable collection and
`.dependency version` frames. Prefer running it only in a copied Figma file or when
the visual mutation is intentionally being inspected in the official file:

```powershell
node dist\write-mcp-runner.mjs --mode=preview --fixture=versions --target=versions
```

Do not write metadata after a version preview run.

## Official Runner Handoff

The preview generator does not authorize an official runner. TeamCity uses the
Kotlin generator and publishes complete visual and metadata runner directories
next to the official model and visual plan. Inspect that artifact with:

```powershell
.\gradlew.bat runFigmaMcp -PfigmaMcpManifest="PATH\TO\visual\manifest.json" -PfigmaMcpPlan="PATH\TO\visual-sync-plan.json" -PfigmaMcpDryRun=true
```

Official mode stages data under `water_my_plants_sync_staging`. Only the
`metadata` target writes to the authoritative namespace, and it should be run
after every scope in the TeamCity-generated visual plan has completed
successfully. A supervised atomic diagnostic does not authorize a metadata
write.

## Verification

For tooling changes:

```powershell
cd repo\figma-design-sync\tools
npm run build
node dist\write-mcp-runner.mjs --mode=preview --fixture=catalog-tree --target=waterMyPlants.plugins --section-node-id=SANDBOX_SECTION_ID
```

For repository contract changes that affect the generated model, also run the
Gradle checks described in [trunk-sync.md](trunk-sync.md).

## Prerequisites

- Use a sandbox Figma section or an explicit preview fixture.
- Build the TypeScript writer from the current branch.
- Keep official metadata writes disabled.

## Recovery

Delete temporary preview sections and regenerate the preview runner when its
fixture or writer changes. A failed preview must not be resumed against an
official section.

## Prohibited Actions

- Do not point a preview runner at an official section.
- Do not write `water_my_plants_sync` metadata from preview mode.
- Do not treat a preview result as authorization for trunk publication.

## Sources

- `tools/src/app/sync-catalog-tree-preview.mcp.ts`
- `tools/scripts/write-mcp-runner.ts`
- `tools/tests/`
