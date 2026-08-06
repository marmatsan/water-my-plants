---
title: Adopt reusable repository tooling
type: guide
scope: repository-tooling
owner: architecture
status: active
last-reviewed: 2026-07-28
review-cycle-days: 180
sources:
  - repo/dependency-catalog/samples/standalone-consumer
  - repo/figma-documentation-sync/samples/standalone-consumer
  - repo/gradle-plugins/samples/standalone-consumer
  - repo/unit-testing/samples/standalone-consumer
  - repo/verification-platform/samples/standalone-consumer
---

# Adopt Reusable Repository Tooling

## Outcome

Consume only the Water My Plants repository-tooling capabilities a new Gradle
project needs, without importing Water My Plants product paths, versions, Figma
identities, or TeamCity policy.

## Applicable Standards

Follow the [architecture standard](../standards/architecture.md) for dependency
direction and type-safe build scripts. A consuming repository owns its version
catalog and its composition adapter.

## Choose A Consumption Mode

During source development, include each selected build from its checkout:

```kotlin
pluginManagement {
    includeBuild("../water-my-plants/repo/dependency-catalog")
    includeBuild("../water-my-plants/repo/gradle-plugins")
}

includeBuild("../water-my-plants/repo/unit-testing")
```

For a released integration, publish the selected builds to a Maven repository
and declare their plugin markers and libraries in the consumer's catalogs. The
repository does not yet select or operate a public registry, so an adopter must
provide that repository or build the artifacts from source.

## Select Capabilities

| Build | Public contract |
|-------|-----------------|
| `dependency-catalog` | `com.marmatsan.dependencyCatalog`, `com.marmatsan.dependencyCatalog.tree`; `com.marmatsan.repo:catalog-api`, `catalog-core`, `catalog-gradle-plugin`, and `catalog-tree-gradle-plugin` |
| `figma-documentation-sync` | `com.marmatsan.figmaDocumentationSync`; portable domain, data, plugin, and optional TeamCity adapter artifacts |
| `gradle-plugins` | `com.marmatsan.android`, `bddTest`, `compose`, `dokkaDocumentation`, `protobuf`, and `unitTest` |
| `unit-testing` | `com.marmatsan.repo:unit-test-dsl` |
| `verification-platform` | `com.marmatsan.verificationPlatform`; domain, data, and plugin artifacts |
| `project-config` | `com.marmatsan.projectConfig.settings`, `com.marmatsan.projectConfig`, and optional `com.marmatsan.projectConfig.figma` |

Apply `project-config` and keep product-specific catalogs, Figma node IDs,
TeamCity configuration, and change-impact policy in the consuming repository's
root configuration and data files.

## Own Versions And Catalogs

Create a consumer-owned `versions.properties`, TOML catalog, or settings-based
catalog. Register external plugins as catalog plugins and external libraries as
catalog libraries, then use `alias(plugins...)` and `libs...` in
`build.gradle.kts`. Each reusable build keeps its own `versions.properties`
only for building and testing itself; those files do not choose versions for a
consumer.

Apply repository-owned plugin aliases only where they are consumed. When their
implementation comes from `pluginManagement.includeBuild`, do not also declare
the same aliases with `apply false` in the root build: Gradle can place the
source plugin on the shared classpath without version metadata, preventing a
subproject from validating its versioned alias request. Settings plugins that
must run before catalogs exist remain literal bootstrap declarations.

When adopting `com.marmatsan.dependencyCatalog.tree`, use that settings plugin
to build both catalogs from the consumer-owned `versions.properties`. Keep the
plugin producer's own bootstrap catalog independent; a build must not require
the plugin it is currently compiling in order to evaluate its settings.

If `com.marmatsan.unitTest` is applied, the consumer must expose
`com.marmatsan.repo:unit-test-dsl` in a separate `testLibs` catalog. The
convention plugin asks for that consumer-owned alias instead of embedding a
sibling module path or version, and the test API stays outside `libs` and the
production catalog tree.

## Add Product Adapters

Implement consumer-owned ports for product dependency catalogs, Figma identity
and target selection, CI-provider translation, and repository path policy only
when those capabilities are selected. Keep this wiring in the new project's
composition build so reusable tooling remains independent of both projects.

## Verification

Each build proves source-independent consumption with:

```powershell
.\gradlew.bat -p repo/dependency-catalog verifyStagedPublication
.\gradlew.bat -p repo/figma-documentation-sync verifyStagedPublication
.\gradlew.bat -p repo/gradle-plugins verifyStagedPublication
.\gradlew.bat -p repo/unit-testing verifyStagedPublication
.\gradlew.bat -p repo/verification-platform verifyStagedPublication
```

Water My Plants aggregates those proofs in
`.\gradlew.bat verifyPortableDistribution`. A new repository should create the
same kind of consumer fixture for only the capabilities it adopts.

## Related Documentation

- [Project structure](../reference/project-structure.md)
- [Dependency Catalog adoption](../../repo/dependency-catalog/docs/guides/adopt-dependency-catalog.md)
- [Adopt Figma Documentation Sync](../../repo/figma-documentation-sync/docs/guides/adopting-figma-documentation-sync.md)
- [Testing standard](../standards/testing.md)
