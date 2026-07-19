---
title: Adopt Figma Documentation Sync in a Gradle repository
type: guide
scope: repo/figma-documentation-sync
owner: figma-documentation-sync
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - repo/figma-documentation-sync/plugin/src/main/kotlin/com/marmatsan/figmaDocumentationSync/plugin/gradle/FigmaDocumentationSyncGradlePlugin.kt
  - repo/figma-documentation-sync/plugin/src/main/kotlin/com/marmatsan/figmaDocumentationSync/plugin/gradle/FigmaCatalogChecksExtension.kt
  - repo/figma-documentation-sync/domain/src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/model/writer/FigmaWriterProjectConfig.kt
  - repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/json/writer/FigmaWriterProjectConfigJson.kt
  - repo/figma-documentation-sync/tools/bin/build.mjs
  - repo/figma-documentation-sync/samples/standalone-consumer
---

# Adopt Figma Documentation Sync in a Gradle Repository

## Outcome

A Gradle repository consumes the versioned `com.marmatsan.figmaDocumentationSync`
plugin without including this source build. The repository owns its Figma
identities and catalog adapters, may select an optional CI adapter, and builds
the portable writer with a transient JSON projection of its typed Kotlin
configuration.

Until an external repository is selected, use the staged publication produced
by `verifyStagedPublication`. Do not copy `domain`, `data`, or `plugin` into a
consumer repository.

## Applicable Standards

- Follow [ADR-0005](../../../../docs/decisions/adr-0005-name-figma-documentation-sync.md)
  for the public artifact boundary.
- Follow [the documentation standard](../../../../docs/documentation.md) for
  the consumer repository's project adapter documentation.
- Keep project identities outside the portable modules as required by
  [`project-config/README.md`](../../project-config/README.md).

## Steps

1. Add the release Maven repository to `pluginManagement` and select one
   version for every Figma Documentation Sync artifact:

   ```kotlin
   pluginManagement {
       repositories {
           maven("https://packages.example.com/figma-documentation-sync")
           gradlePluginPortal()
           mavenCentral()
       }
       plugins {
           id("com.marmatsan.figmaDocumentationSync") version "<version>"
       }
   }
   ```

2. Create a repository-owned Gradle adapter. It applies the portable plugin,
   implements `DependencyCatalogProvider`, and configures the
   `figmaDocumentationSync` extension. Keep Figma node ids, paths, catalog names, and
   CI commands in that adapter.

   ```kotlin
   plugins {
       id("com.marmatsan.figmaDocumentationSync")
   }

   figmaDocumentationSync {
       metadataNamespace.set("example_project_sync")
       designModelMetadataNodeUrl.set(
           "https://www.figma.com/design/<file-key>/<file>?node-id=<page-node>"
       )
       primaryCatalogModelName.set("exampleProject")
       dependencyCatalogProviderClassName.set(
           "com.example.figma.ExampleDependencyCatalogProvider"
       )
       versionsFile.set(layout.projectDirectory.file("gradle/versions.properties"))
       toolsDirectory.set(layout.buildDirectory.dir("figma-documentation-sync-tools"))
   }
   ```

3. Create a repository-owned `FigmaWriterProjectConfig`, encode it with
   `FigmaWriterProjectConfigJson`, and register
   `WriteFigmaWriterProjectConfigTask`. Wire its output into
   `PrepareOfficialFigmaSyncTask.writerProjectConfigFile`,
   `RunFigmaMcpTask.writerProjectConfigFile`, and
   `ProbeFigmaMcpTask.writerProjectConfigFile`. The model supplies
   repository paths, Figma component identities, visual targets, and the
   relative repository root used for writer fingerprints. Keep the JSON under
   `build/`; do not version it.

4. Add `@marmatsan/figma-documentation-sync-tools` at the same version and materialize
   the configured writer from that transient JSON.

   ```powershell
   npm install --save-dev @marmatsan/figma-documentation-sync-tools@<version>
   npx figma-documentation-sync-build `
       --project-config-json=build\generated\figma-documentation-sync\writer-project-config.json `
       --output-dir=build\figma-documentation-sync-tools
   ```

5. Configure included builds through `figmaDocumentationSync.includedBuilds` only when
   they contribute catalogs, modules, or convention plugins to the generated
   model.

6. For TeamCity, add the optional
   `com.marmatsan.figma-documentation-sync:figma-documentation-sync-teamcity-adapter:<version>`
   dependency to the repository adapter and select
   `TeamCityCiConfigurationProvider`. Other repositories may supply a sibling
   `CiConfigurationProvider` or leave CI documentation disabled.

7. Add the relevant verification tasks to CI. Treat the model generated on the
   default branch as the only official publication input.

## Verification

Run the standalone staged-consumer test before adopting a release:

```powershell
.\gradlew.bat :figma-documentation-sync:verifyStagedPublication
```

In the consumer repository, verify plugin application and its focused checks:

```powershell
.\gradlew.bat tasks --group verification
.\gradlew.bat checkFigmaVersionNaming checkFigmaCatalogUsage
```

Build the configured writer and run its tests before enabling an official
Figma write. Do not generate or publish official metadata from a feature
branch.

## Related Documentation

- [Distribution contract](../reference/distribution-contract.md)
- [Publication runbook](../runbooks/publishing-release.md)
- [Project adapter contract](../../project-config/README.md)
- [Official trunk sync](../runbooks/trunk-sync.md)
