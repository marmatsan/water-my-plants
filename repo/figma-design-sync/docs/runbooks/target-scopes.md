# Figma Sync Target Scopes

## Purpose

Use this runbook to understand the ordered visual targets in the Figma model.
An official integration always synchronizes the complete visual target set.
Granular targets are reserved for supervised diagnosis and repair.

## Included Builds

Default included builds:

| Gradle build name | Model name | Root directory | Module path prefix | Publishes catalogs | Publishes convention plugins |
|-------------------|------------|----------------|--------------------|--------------------|------------------------------|
| `dependency-catalog` | `dependencyCatalog` | `repo/dependency-catalog` | `:dependency-catalog` | No | No |
| `figma-design-sync` | `figmaDesignSync` | `repo/figma-design-sync` | `:figma-design-sync` | Yes | No |
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
| `figmaDesignSync.libraries` | `repo/figma-design-sync/settings.gradle.kts` | `63573:260` |
| `figmaDesignSync.plugins` | `repo/figma-design-sync/settings.gradle.kts` | `63573:346` |

CI documentation visual targets:

| Model target | Source | Figma scope |
|--------------|--------|-------------|
| `ci.overview` | `content.ci` aggregate | `Overview` inside page `63153:2876` |
| `ci.pullRequestIntegration` | Effective `.teamcity/settings.kts` model and branch protection contract | `Pull Request Integration` inside page `63153:2876` |
| `ci.postMergeDesignDocumentation` | Effective Figma Sync pipeline and the operator/MCP loop | `Post-merge Design Documentation` inside page `63153:2876` |
| `ci.infrastructureAndAccess` | `docs/ci/external-topology.yaml` | `Infrastructure and Access` inside page `63153:2876` |
| `ci.windowsRuntime` | `docs/ci/windows-runtime.yaml` | `Windows Service Runtime` inside page `63153:2876` |

The five targets share the parent section `Continuous Integration and Design
Documentation`. A diagnostic target reuses the parent and only replaces nodes
and connectors managed by the requested child section, but it does not complete
an official integration.

## Execution Order

Generate the complete official visual runner by omitting `--target`, or by
passing `--target=all`. It executes `preflight` followed by every visual target
in the order below and never writes metadata:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json
```

Do not run `metadata` until that complete visual execution has succeeded and
all affected sections have been checked. Then generate a separate metadata
runner.

For supervised diagnosis only, an official-artifact runner may select one
target by explicitly acknowledging that it is partial:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --targets=preflight,waterMyPlants.libraries --allow-partial=true
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

If the preflight fails, visual targets are not executed. A partial preflight or
repair can diagnose the issue, but the complete runner must pass afterward.

| Order | Target | Scope | Typical failure | Quick check |
|-------|--------|-------|-----------------|-------------|
| 0 | `preflight` | Figma variables, component contracts, configured sections, and target model shape | Missing component property, usage chip variant, section, variable collection, or invalid root filter | Returned `checkedComponents`, `checkedSections`, `checkedVariables`, and `checkedTargets` are populated and `mutatedNodeIds` is empty. |
| 1 | `headers` | Parent documentation `.Header` links | Stale `build-logic` URL, centered link text, missing `Link` property, or multiple source paths sharing one hyperlink | Every displayed source path is left-aligned, opens its own canonical GitHub `main` URL, and `updatedHeaders` lists all configured parent sections. |
| 2 | `versions` | Version variables and `.dependency version` nodes | Missing variable collection, stale version section, or duplicate renamed version key | Returned `updatedVersions` contains the expected version keys and stale visual version nodes are removed. |
| 3 | `waterMyPlants.libraries` | Main app libraries and usage chips | Ambiguous `.artifact` / `.artifacts bundle` usage headings or hidden usage blocks on the visible instance | Returned `completedTargets` contains `preflight` and this target, and a spot-checked artifact with model usage shows `Applied by plugin` / `Used by module` chips. |
| 4 | `waterMyPlants.plugins` | Main app plugin catalog tree | Missing `.tree node` property or connector binding issue | Returned catalog nodes match the plugin tree and connectors stay in the section. |
| 5 | `waterMyPlants.customGradleConventionPlugins` | Convention plugin catalog | Stale convention plugin names or missing usage chip variants | Returned nodes include the convention plugin ids expected from `repo/gradle-plugins`. |
| 6 | `waterMyPlants.customGradlePlugins` | Regular custom Gradle plugin catalog | A regular plugin is modeled as a convention plugin, or the reverse | Returned nodes include `com.marmatsan.figmaDesignSync` as a regular plugin. |
| 7 | `gradlePlugins.libraries` | `repo/gradle-plugins` libraries catalog | Large artifact/bundle update with stale nested component internals | Returned `completedTargets` contains `preflight` and the target, with no metadata. |
| 8 | `gradlePlugins.plugins` | Declared catalog target from `repo/gradle-plugins` plugins catalog | Stale hidden section after removing `create("plugins")` | Empty or omitted catalog removes the target section; declared catalog nodes match the settings catalog. |
| 9 | `figmaDesignSync.libraries` | `repo/figma-design-sync` libraries catalog | Large artifact/bundle update with stale nested component internals | Returned `completedTargets` contains `preflight` and the target, with no metadata. |
| 10 | `figmaDesignSync.plugins` | `repo/figma-design-sync` plugins catalog | Missing plugin tree connector or stale plugin aliases | Returned catalog nodes match the settings catalog. |
| 11 | `ci.overview` | Simplified PR and post-merge journeys | Generic connector labels or missing check/gate distinction | Trigger, check, gate, merge, artifact, and hash-verification connections have explicit labels. |
| 12 | `ci.pullRequestIntegration` | Detailed PR pipeline, jobs, checks, and merge gate | Effective TeamCity job or published check missing from Figma | Nodes and summarized steps match `content.ci.teamCity`. |
| 13 | `ci.postMergeDesignDocumentation` | Official model generation and operator-assisted visual update loop | Automatic Figma write implied, artifact missing, or rerun loop absent | `design-model.json`, the operator/Codex handoff, and `Rerun via HTTPS client` are visible. |
| 14 | `ci.infrastructureAndAccess` | GitHub, Cloudflare, TeamCity, Figma, browser, CLI, and operator topology | Connection collapsed or external system duplicated from TeamCity DSL | Nodes and directed connections match `content.ci.externalTopology`. |
| 15 | `ci.windowsRuntime` | TeamCity Server, Build Agent, and Cloudflare Tunnel Windows services | Runtime block hidden, stale service identity, or incorrect icon environment | Three nodes match `content.ci.windowsRuntime`, expose complete runtime fields, and have no inferred connectors. |
| 16 | `metadata` | Shared plugin sync metadata | Metadata written before visual targets complete | Figma shared plugin data matches the TeamCity artifact. |

## Subtree Scoped Runs

When a catalog target is too large for one MCP call, or a visual fix only
affects one top-level catalog root, run the target against the matching child
section and filter the TeamCity model with `--roots`.

```powershell
cd repo\figma-design-sync\tools
npm run build
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --target=waterMyPlants.libraries --roots=androidx --section-node-id=63069:630 --allow-partial=true
```

`--roots` filters only top-level catalog roots before the tree is flattened.
Library roots match `group`; plugin roots match `id`. If the root is missing,
the runner fails before mutating Figma and prints the available roots.

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
| `figmaDesignSync.libraries` | `io` | `63573:286` |
| `figmaDesignSync.libraries` | `me` | `63573:295` |
| `figmaDesignSync.libraries` | `org` | `63573:273` |
| `figmaDesignSync.plugins` | `com` | `63573:358` |
| `figmaDesignSync.plugins` | `org` | `63573:347` |

Use a subtree scoped run instead of editing `design-model.json` manually. The
artifact must still come from `main`; the filter is a transport/runtime scope,
not a different source of truth.

## Failure Rule

When a target fails, use a partial runner only to diagnose and verify the local
repair. Fix the component or TypeScript contract, merge the fix to `main`, and
regenerate the official TeamCity artifact when model content changes. Before
writing metadata, rerun the complete official visual target set from
`preflight`; a successful partial repair never closes the integration.
