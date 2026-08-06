---
title: Project Config consumer contract
type: reference
scope: repo/project-config
owner: repository-tooling
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - repo/project-config/plugin/src/main/kotlin/com/marmatsan/projectConfig/settings/ProjectConfigSettingsExtension.kt
  - repo/project-config/plugin/src/main/kotlin/com/marmatsan/projectConfig/settings/ProjectConfigSettingsPlugin.kt
  - repo/project-config/plugin/src/main/kotlin/com/marmatsan/projectConfig/project/ProjectConfigGradlePlugin.kt
  - repo/project-config/figma-adapter/src/main/kotlin/com/marmatsan/projectConfig/figma/ProjectConfigFigmaGradlePlugin.kt
  - repo/project-config/samples/health-consumer/settings.gradle.kts
  - repo/project-config/samples/health-consumer/build.gradle.kts
---

# Project Config Consumer Contract

## Purpose

This reference defines how a Gradle repository supplies its own dependency
catalog trees to the reusable `project-config` included build.

## Contract

The consumer resolves and applies `com.marmatsan.projectConfig.settings` from
its root `settings.gradle.kts`. It declares the tree inline after applying the
plugin:

```kotlin
projectConfig {
    versionsFile.set(file("versions.properties"))

    dependencyCatalog {
        libraries {
            root("io") {
                library("ktor") {
                    artifact(
                        artifact = "ktor-client-core",
                        version = version("ktorLibraryVersion")
                    )
                }
            }
        }
        plugins {
            root("org") {
                plugin("jetbrains.kotlin") {
                    plugin(
                        id = "jvm",
                        version = version("kotlinVersion")
                    )
                }
            }
        }
    }
}
```

`versionsFile` defaults to the root `versions.properties`. The consumer owns
that file and every key requested through `version("key")`. A version key is an
input used to resolve artifact and plugin aliases; it is not guaranteed to be a
separate public Gradle version alias. Consumers read the resolved version from
the corresponding library or plugin alias.

The generated catalog names default to `libs` and `plugins`. A consumer may set
`librariesCatalogName` and `pluginsCatalogName` before `dependencyCatalog` is
materialized.

The root `build.gradle.kts` applies `com.marmatsan.projectConfig`. This project
plugin requires the matching Settings plugin and fails during configuration
with an actionable message when the Settings composition is absent.

Repositories that publish dependency trees through Figma apply
`com.marmatsan.projectConfig.figma` instead. This optional adapter applies the
base project plugin plus Figma Documentation Sync, maps the consumer-owned
catalog through public models, enriches it with module and convention-plugin
usage, and supplies `dependencyCatalogTreesJson`. Consumers do not implement or
name a reflective catalog provider.

## Distribution

A source-independent consumer resolves three publication repositories: Project
Config for its plugin markers and implementations, Dependency Catalog for the
Settings DSL, and Figma Documentation Sync when the optional Figma adapter is
applied. The staged contract passes them as
`projectConfigPublicationRepository`,
`dependencyCatalogPublicationRepository`, and
`figmaDocumentationSyncPublicationRepository` respectively. The Health sample
is the executable reference for repository and plugin-management ordering.

Source development may substitute the builds through
`dependencyCatalogSourceBuild`, `figmaDocumentationSyncSourceBuild`, and
`unitTestingSourceBuild`. Those properties are build inputs for developing
Project Config; consumers must not depend on their filesystem paths.

Figma's portable implementation artifacts use the module-name coordinates
`com.marmatsan.figma-documentation-sync:domain`, `:data`, and `:plugin`. Keeping
those coordinates aligned with the included-build project names makes the same
dependencies resolvable from a staged Maven repository or source substitution.

## Failure behavior

- Missing plugin versions or publication repositories fail during Gradle plugin
  resolution before the consumer catalog is configured.
- Applying `com.marmatsan.projectConfig` without the Settings plugin fails with
  an actionable configuration error.
- Figma catalog tasks require exactly one primary representation. The reusable
  adapter supplies `dependencyCatalogTreesJson`; consumers must not also set the
  legacy `dependencyCatalogProviderClassName` input.
- A missing consumer version key fails while the Settings catalog tree is
  materialized and names the unresolved property.

## Invariants

- Library and plugin trees remain inline in the consumer's root
  `settings.gradle.kts` and obey the root/segment rules from the architecture
  standard.
- `repo/project-config/versions.properties` compiles `project-config` itself;
  it is not the consumer's product version registry.
- The reusable build contains no consumer tree, product identity, Figma node,
  TeamCity build id, or consumer repository path.
- A consumer does not need a product-specific `*-project-config` included build.
- The staged Health fixture has no source `includeBuild` and verifies the
  published Settings, project, and Figma-adapter plugin markers plus
  configuration-cache compatibility.

## Sources

- [`../../samples/health-consumer/settings.gradle.kts`](../../samples/health-consumer/settings.gradle.kts)
- [`../../samples/health-consumer/build.gradle.kts`](../../samples/health-consumer/build.gradle.kts)
- [`../../../dependency-catalog/README.md`](../../../dependency-catalog/README.md)
- [`../../../../docs/standards/architecture.md`](../../../../docs/standards/architecture.md)
