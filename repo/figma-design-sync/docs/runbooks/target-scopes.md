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
| `gradlePlugins.plugins` | `repo/gradle-plugins/settings.gradle.kts` | `63100:2952` |
| `figmaDesignSync.libraries` | `repo/figma-design-sync/settings.gradle.kts` | `63573:260` |
| `figmaDesignSync.plugins` | `repo/figma-design-sync/settings.gradle.kts` | `63573:346` |

## Execution Order

Run visual updates by granular target. Do not run `metadata` until every visual
target has completed successfully.

| Order | Target | Scope | Typical failure | Quick check |
|-------|--------|-------|-----------------|-------------|
| 1 | `versions` | Version variables and `.project version` nodes | Missing variable collection or stale version section | Returned `updatedVersions` contains the expected version keys. |
| 2 | `waterMyPlants.libraries` | Main app libraries and usage chips | Ambiguous `.artifact` / `.artifacts bundle` usage headings or hidden usage blocks on the visible instance | Returned `completedTargets` contains only this target and a spot-checked artifact with model usage shows `Provided by` / `Required by` chips. |
| 3 | `waterMyPlants.plugins` | Main app plugin catalog tree | Missing `.tree node` property or connector binding issue | Returned catalog nodes match the plugin tree and connectors stay in the section. |
| 4 | `waterMyPlants.customGradleConventionPlugins` | Convention plugin catalog | Stale convention plugin names or missing usage chip variants | Returned nodes include the convention plugin ids expected from `repo/gradle-plugins`. |
| 5 | `waterMyPlants.customGradlePlugins` | Regular custom Gradle plugin catalog | A regular plugin is modeled as a convention plugin, or the reverse | Returned nodes include `com.marmatsan.figmaDesignSync` as a regular plugin. |
| 6 | `gradlePlugins.libraries` | `repo/gradle-plugins` libraries catalog | Large artifact/bundle update with stale nested component internals | Returned `completedTargets` contains the target and no metadata. |
| 7 | `gradlePlugins.plugins` | `repo/gradle-plugins` plugins catalog | Missing plugin tree connector or stale plugin aliases | Returned catalog nodes match the settings catalog. |
| 8 | `figmaDesignSync.libraries` | `repo/figma-design-sync` libraries catalog | Large artifact/bundle update with stale nested component internals | Returned `completedTargets` contains the target and no metadata. |
| 9 | `figmaDesignSync.plugins` | `repo/figma-design-sync` plugins catalog | Missing plugin tree connector or stale plugin aliases | Returned catalog nodes match the settings catalog. |
| 10 | `metadata` | Shared plugin sync metadata | Metadata written before visual targets complete | Figma shared plugin data matches the TeamCity artifact. |

## Failure Rule

When a target fails, fix that target's component or TypeScript contract, merge
the fix to `main`, regenerate the official TeamCity artifact when model content
changes, and resume from the failed target. Do not repeat already-successful
targets unless the fix changes their source data or shared component contract.
