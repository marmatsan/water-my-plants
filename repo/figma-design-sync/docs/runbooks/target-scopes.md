# Figma Sync Target Scopes

## Purpose

Use this runbook to choose the smallest visual target that covers the Figma
section being changed. Prefer granular target execution over broad reruns.

## Included Builds

Default included builds:

| Gradle build name | Model name | Root directory | Module path prefix | Publishes catalogs | Publishes convention plugins |
|-------------------|------------|----------------|--------------------|--------------------|------------------------------|
| `dependency-catalog` | `dependencyCatalog` | `repo/dependency-catalog` | `:dependency-catalog` | No | No |
| `figma-design-sync` | `figmaDesignSync` | `repo/figma-design-sync` | `:figma-design-sync` | Yes | No |
| `gradle-plugins` | `gradlePlugins` | `repo/gradle-plugins` | `:gradle-plugins` | Yes | Yes |

`repo/dependency-catalog` has no settings-catalog visual target. It contributes
the versions file and the standalone `:dependency-catalog` module.

## Visual Target Map

Catalog tree visual targets:

| Model target | Source | Figma section |
|--------------|--------|---------------|
| `waterMyPlants.libraries` | `repo/dependency-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt` | `63069:629` |
| `waterMyPlants.plugins` | `repo/dependency-catalog/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt` | `63069:594` |
| `waterMyPlants.customGradleConventionPlugins` | `repo/gradle-plugins/**/build.gradle.kts` | `63216:6907` |
| `waterMyPlants.customGradlePlugins` | repository included-build `**/build.gradle.kts` files that declare regular Gradle plugins | `63330:551` |
| `gradlePlugins.libraries` | `repo/gradle-plugins/settings.gradle.kts` | `63099:951` |
| `gradlePlugins.plugins` | declared catalog target from `repo/gradle-plugins/settings.gradle.kts` `create("plugins")` | removed when source catalog is absent |
| `figmaDesignSync.libraries` | `repo/figma-design-sync/settings.gradle.kts` | `63573:260` |
| `figmaDesignSync.plugins` | `repo/figma-design-sync/settings.gradle.kts` | `63573:346` |

## Execution Order

Run visual updates by granular target. Do not run `metadata` until every visual
target has completed successfully.

| Order | Target | Scope | Typical failure | Quick check |
|-------|--------|-------|-----------------|-------------|
| 1 | `versions` | Version variables and `.project version` nodes | Missing variable collection or stale version section | Returned `updatedVersions` contains the expected version keys. |
| 2 | `waterMyPlants.libraries` | Main app libraries and usage chips | Ambiguous `.artifact` / `.artifacts bundle` usage headings or hidden usage blocks on the visible instance | Returned `completedTargets` contains only this target and a spot-checked artifact with model usage shows `Applied by plugin` / `Used by module` chips. |
| 3 | `waterMyPlants.plugins` | Main app plugin catalog tree | Missing `.tree node` property or connector binding issue | Returned catalog nodes match the plugin tree and connectors stay in the section. |
| 4 | `waterMyPlants.customGradleConventionPlugins` | Convention plugin catalog | Stale convention plugin names or missing usage chip variants | Returned nodes include the convention plugin ids expected from `repo/gradle-plugins`. |
| 5 | `waterMyPlants.customGradlePlugins` | Regular custom Gradle plugin catalog | A regular plugin is modeled as a convention plugin, or the reverse | Returned nodes include `com.marmatsan.figmaDesignSync` as a regular plugin. |
| 6 | `gradlePlugins.libraries` | `repo/gradle-plugins` libraries catalog | Large artifact/bundle update with stale nested component internals | Returned `completedTargets` contains the target and no metadata. |
| 7 | `gradlePlugins.plugins` | Declared catalog target from `repo/gradle-plugins` plugins catalog | Stale hidden section after removing `create("plugins")` | Empty or omitted catalog removes the target section; declared catalog nodes match the settings catalog. |
| 8 | `figmaDesignSync.libraries` | `repo/figma-design-sync` libraries catalog | Large artifact/bundle update with stale nested component internals | Returned `completedTargets` contains the target and no metadata. |
| 9 | `figmaDesignSync.plugins` | `repo/figma-design-sync` plugins catalog | Missing plugin tree connector or stale plugin aliases | Returned catalog nodes match the settings catalog. |
| 10 | `metadata` | Shared plugin sync metadata | Metadata written before visual targets complete | Figma shared plugin data matches the TeamCity artifact. |

## Subtree Scoped Runs

When a catalog target is too large for one MCP call, or a visual fix only
affects one top-level catalog root, run the target against the matching child
section and filter the TeamCity model with `--roots`.

```powershell
cd repo\figma-design-sync\tools
npm run build
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --target=waterMyPlants.libraries --roots=androidx --section-node-id=63069:630
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

When a target fails, fix that target's component or TypeScript contract, merge
the fix to `main`, regenerate the official TeamCity artifact when model content
changes, and resume from the failed target. Do not repeat already-successful
targets unless the fix changes their source data or shared component contract.
