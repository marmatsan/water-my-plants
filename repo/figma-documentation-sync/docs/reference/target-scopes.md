---
title: Figma sync target scopes
type: reference
scope: repo/figma-documentation-sync
owner: figma-documentation-sync
status: active
last-reviewed: 2026-07-19
review-cycle-days: 90
sources:
  - repo/figma-documentation-sync/project-config/src/main/kotlin/com/marmatsan/figmaDocumentationSync/projectConfig/WaterMyPlantsFigmaWriterProjectConfig.kt
  - repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/writer/CanonicalMcpRunnerGenerator.kt
---

# Figma Sync Target Scopes

## Purpose

Use this reference to understand the ordered visual targets in the Figma model.
TeamCity always generates the complete runner. The canonical visual plan then
requires either all targets (`full`), only changed fingerprints plus preflight
(`partial`), or no write (`none`). Ad hoc granular targets remain reserved for
supervised diagnosis and repair.

## Included Builds

Default included builds:

| Gradle build name | Model name | Root directory | Module path prefix | Publishes catalogs | Publishes convention plugins |
|-------------------|------------|----------------|--------------------|--------------------|------------------------------|
| `dependency-catalog` | `dependencyCatalog` | `repo/dependency-catalog` | `:dependency-catalog` | No | No |
| `figma-documentation-sync` | `figmaDocumentationSync` | `repo/figma-documentation-sync` | `:figma-documentation-sync` | Yes | No |
| `gradle-plugins` | `gradlePlugins` | `repo/gradle-plugins` | `:gradle-plugins` | Yes | Yes |

`repo/dependency-catalog` has no settings-catalog visual target. It contributes
the versions file and the `:dependency-catalog:catalog-core` and
`:dependency-catalog:water-my-plants-catalog` modules.

## Visual Target Map

Catalog tree visual targets:

| Model target | Source | Figma section |
|--------------|--------|---------------|
| `waterMyPlants.libraries` | `repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt` | `63069:629` |
| `waterMyPlants.plugins` | `repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt` | `63069:594` |
| `waterMyPlants.customGradleConventionPlugins` | `repo/gradle-plugins/**/build.gradle.kts` | `63216:6907` |
| `waterMyPlants.customGradlePlugins` | repository included-build `**/build.gradle.kts` files that declare regular Gradle plugins | `63330:551` |
| `gradlePlugins.libraries` | `repo/gradle-plugins/settings.gradle.kts` | `63099:951` |
| `gradlePlugins.plugins` | declared catalog target from `repo/gradle-plugins/settings.gradle.kts` `create("plugins")` | removed when source catalog is absent |
| `figmaDocumentationSync.libraries` | `repo/figma-documentation-sync/settings.gradle.kts` | `63573:260` |
| `figmaDocumentationSync.plugins` | `repo/figma-documentation-sync/settings.gradle.kts` | `63573:346` |

CI documentation visual targets:

| Model target | Source | Figma scope |
|--------------|--------|-------------|
| `ci.overview` | `content.ci` aggregate | `Overview` inside page `63153:2876` |
| `ci.pullRequestIntegration` | Effective `.teamcity/settings.kts` model and branch protection contract | `Pull Request Integration` inside page `63153:2876` |
| `ci.postMergeDesignDocumentation` | Effective Figma Sync pipeline and the operator/MCP loop | `Post-merge Design Documentation` inside page `63153:2876` |
| `ci.jobTasks` | Ordered TeamCity phases, Gradle tasks, decisions, and outcomes | `Job Tasks` inside page `63153:2876` |
| `ci.infrastructureAndAccess` | `docs/ci/external-topology.yaml` | `Infrastructure and Access` inside page `63153:2876` |
| `ci.windowsRuntime` | `docs/ci/windows-runtime.yaml` | `Windows Service Runtime` inside page `63153:2876` |

The six targets share the parent section `Continuous Integration and
Documentation Automation`. Both a generated partial plan and a diagnostic
target reuse the parent and only replace nodes and connectors managed by the
requested child section. Only the TeamCity-generated plan can authorize that
subset as a canonical integration.

## Execution Order

Generate the complete canonical visual runner by omitting `--target`, or by
passing `--target=all`. It creates bounded MCP runner files for `preflight` and
each visual target in the order below, and never writes metadata. Catalog
targets are expanded into one file per declared root plus a final cleanup file:

```powershell
.\gradlew.bat runFigmaMcp -PfigmaMcpManifest="PATH\TO\visual\manifest.json" -PfigmaMcpPlan="PATH\TO\visual-sync-plan.json" -PfigmaMcpDryRun=true
```

Apply the accompanying `visual-sync-plan.json` to that complete runner. Do not
run `metadata` until every scope selected by the plan has succeeded and all
affected sections have been checked. Then use the separate metadata runner.

For supervised diagnosis only, inspect the next atomic unit from the complete
canonical artifact. Do not regenerate a partial canonical runner locally:

```powershell
.\gradlew.bat runFigmaMcp -PfigmaMcpManifest="PATH\TO\visual\manifest.json" -PfigmaMcpPlan="PATH\TO\visual-sync-plan.json" -PfigmaMcpNext=true
```

For an atomic granular runner, `completedTargets` is expected to contain both
`preflight` and the requested visual target. For example, a successful
`ci.overview` runner returns:

```text
["preflight", "ci.overview"]
```

Do not reject a runner because `preflight` appears alongside the visual target.
Generate `--target=preflight --allow-partial=true` only when no visual mutation
is intended.

If the preflight fails, visual targets are not executed. An ad hoc partial
repair can diagnose the issue, but afterward the TeamCity-generated plan must
complete. A mapped target-specific writer change selects only its affected
scope family plus `preflight`; shared, unmapped, or unexplained writer changes
produce a `full` plan automatically.

| Order | Target | Scope | Typical failure | Quick check |
|-------|--------|-------|-----------------|-------------|
| 0 | `preflight` | Figma variables, component contracts, configured sections, and target model shape | Missing component property, usage chip variant, section, variable collection, or invalid root filter | Returned `checkedComponents`, `checkedSections`, `checkedVariables`, and `checkedTargets` are populated and `mutatedNodeIds` is empty. |
| 1 | `headers` | Parent documentation `.Header` links | Stale `build-logic` URL, centered link text, missing `Link` property, or multiple source paths sharing one hyperlink | Every displayed source path is left-aligned, opens its own canonical GitHub `main` URL, and `updatedHeaders` lists all configured parent sections. |
| 2 | `versions` | Version variables and `.dependency version` nodes | Missing variable collection, stale version section, or duplicate renamed version key | Returned `updatedVersions` contains the expected version keys and stale visual version nodes are removed. |
| 3 | `waterMyPlants.libraries` | Main app libraries and usage chips | Ambiguous `.artifact` / `.artifacts bundle` usage headings or hidden usage blocks on the visible instance | Returned `completedTargets` contains `preflight` and this target, and a spot-checked artifact with model usage shows `Applied by plugin` / `Used by module` chips. |
| 4 | `waterMyPlants.plugins` | Main app plugin catalog tree | Missing `.tree node` property or connector binding issue | Returned catalog nodes match the plugin tree and connectors stay in the section. |
| 5 | `waterMyPlants.customGradleConventionPlugins` | Convention plugin catalog | Stale convention plugin names or missing usage chip variants | Returned nodes include the convention plugin ids expected from `repo/gradle-plugins`. |
| 6 | `waterMyPlants.customGradlePlugins` | Regular custom Gradle plugin catalog | A regular plugin is modeled as a convention plugin, or the reverse | Returned nodes include `com.marmatsan.figmaDocumentationSync` as a regular plugin. |
| 7 | `gradlePlugins.libraries` | `repo/gradle-plugins` libraries catalog | Large artifact/bundle update with stale nested component internals | Returned `completedTargets` contains `preflight` and the target, with no metadata. |
| 8 | `gradlePlugins.plugins` | Declared catalog target from `repo/gradle-plugins` plugins catalog | Stale hidden section after removing `create("plugins")` | Empty or omitted catalog removes the target section; declared catalog nodes match the settings catalog. |
| 9 | `figmaDocumentationSync.libraries` | `repo/figma-documentation-sync` libraries catalog | Large artifact/bundle update with stale nested component internals | Returned `completedTargets` contains `preflight` and the target, with no metadata. |
| 10 | `figmaDocumentationSync.plugins` | `repo/figma-documentation-sync` plugins catalog | Missing plugin tree connector or stale plugin aliases | Returned catalog nodes match the settings catalog. |
| 11 | `ci.overview` | Simplified PR and post-merge journeys | Generic connector labels or missing check/gate distinction | Trigger, check, gate, merge, artifact, and hash-verification connections have explicit labels. |
| 12 | `ci.pullRequestIntegration` | Detailed PR pipeline, jobs, checks, and merge gate | Effective TeamCity job, Gradle selection path, or published check missing from Figma | Nodes match `content.ci.teamCity`; `Verify` shows `prepareTeamCityCiPlan`, the dynamic `ci.plan.gradleTasks` paths, and the root `check` contract. |
| 13 | `ci.postMergeDesignDocumentation` | Canonical model generation and operator-assisted visual update loop | Automatic Figma write implied, internal results derived from an optional published status, artifact or Figma metadata input disconnected, or rerun targeting only the check job | The compact `Check Figma trunk sync` job exposes `Metadata matches` and `Visual sync required`; the mismatch branch reaches Operator, Codex/MCP, Figma, `rerunTeamCityFigmaSync`, and queues the complete `Figma Sync` pipeline again. |
| 14 | `ci.infrastructureAndAccess` | GitHub, Cloudflare, TeamCity, Figma, browser, CLI, and operator topology | Connection collapsed or external system duplicated from TeamCity DSL | Nodes and directed connections match `content.ci.externalTopology`. |
| 15 | `ci.windowsRuntime` | TeamCity Server, Build Agent, and Cloudflare Tunnel Windows services | Runtime block hidden, stale service identity, or incorrect icon environment | Three nodes match `content.ci.windowsRuntime`, expose complete runtime fields, and have no inferred connectors. |
| 16 | `metadata` | Shared plugin sync metadata | Metadata written before visual targets complete | Figma shared plugin data matches the TeamCity artifact. |

## Subtree Scoped Runs

When a visual fix affects one top-level catalog root, locate its atomic runner
file in the canonical manifest and inspect the remaining plan from that unit:

```powershell
.\gradlew.bat runFigmaMcp -PfigmaMcpManifest="PATH\TO\visual\manifest.json" -PfigmaMcpPlan="PATH\TO\visual-sync-plan.json" -PfigmaMcpFrom="RUNNER_FILE_FOR_waterMyPlants.libraries.androidx" -PfigmaMcpDryRun=true
```

`figmaMcpFrom` selects that unit and every later planned unit; the dry run does
not mutate Figma. For a supervised one-unit diagnosis, execute only the named
generated file through the supported MCP writer, then record that exact file
with `figmaMcpRecordSuccess` or `figmaMcpRecordFailure`.

The full canonical manifest already splits catalog targets into one file per
top-level root. Library roots match `group`; plugin roots match `id`. If the
root is missing, the generated scope is absent from the manifest.

Root-scoped execution also limits Figma traversal to the matching child
section. Instance, connector, lock, stroke, fill, and descendant-layout scans
must not visit sibling catalog roots or page-level connectors. The generated
canonical runner uses one root-scoped `99-*.mcp.js` unit per declared root and a
separate cleanup unit for stale nodes or sections that require a whole-catalog
view. Cleanup may inspect every direct root of that catalog, but lock and unlock
traversal remains scoped to the catalog section; it must not scan sibling
catalogs under the shared parent documentation section. The shared parent is
unlocked directly so the catalog can be mutated, while descendant `findAll`
calls start at the selected catalog section rather than at that parent. This
keeps the runtime memory boundary aligned with the model scope.

Known child sections:

| Target | Root | Child section |
|--------|------|---------------|
| `waterMyPlants.libraries` | `androidx` | `63069:630` |
| `waterMyPlants.libraries` | `com` | `63069:647` |
| `waterMyPlants.libraries` | `io` | `63069:655` |
| `waterMyPlants.libraries` | `me` | `63069:659` |
| `waterMyPlants.libraries` | `org` | `63069:665` |
| `waterMyPlants.plugins` | `com` | `63069:595` |
| `waterMyPlants.plugins` | `de` | `63069:611` |
| `waterMyPlants.plugins` | `org` | `63069:617` |
| `gradlePlugins.libraries` | `com` | `63100:1707` |
| `gradlePlugins.libraries` | `io` | `63100:2395` |
| `gradlePlugins.libraries` | `org` | `63100:1708` |
| `figmaDocumentationSync.libraries` | `io` | `63573:286` |
| `figmaDocumentationSync.libraries` | `me` | `63573:295` |
| `figmaDocumentationSync.libraries` | `org` | `63573:273` |
| `figmaDocumentationSync.plugins` | `com` | `63573:358` |
| `figmaDocumentationSync.plugins` | `org` | `63573:347` |

Use a subtree scoped run instead of editing `design-model.json` manually. The
artifact must still come from `main`; the filter is a transport/runtime scope,
not a different source of truth.

## Failure Rule

When a target fails, use a partial runner only to diagnose and verify the local
repair. Fix the component or TypeScript contract, merge the fix to `main`, and
regenerate the canonical TeamCity artifact when model content changes. Before
writing metadata, complete every scope selected by the new canonical visual plan
from `preflight`; a successful ad hoc partial repair never closes the
integration.
