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

## Invariants

- Library and plugin trees remain inline in the consumer's root
  `settings.gradle.kts` and obey the root/segment rules from the architecture
  standard.
- `repo/project-config/versions.properties` compiles `project-config` itself;
  it is not the consumer's product version registry.
- The reusable build contains no consumer tree, product identity, Figma node,
  TeamCity build id, or consumer repository path.
- A consumer does not need a product-specific `*-project-config` included build.
- The staged Health fixture has no source `includeBuild` and verifies both the
  published plugin markers and configuration-cache reuse.

## Sources

- [`../../samples/health-consumer/settings.gradle.kts`](../../samples/health-consumer/settings.gradle.kts)
- [`../../samples/health-consumer/build.gradle.kts`](../../samples/health-consumer/build.gradle.kts)
- [`../../../dependency-catalog/README.md`](../../../dependency-catalog/README.md)
- [`../../../../docs/standards/architecture.md`](../../../../docs/standards/architecture.md)
