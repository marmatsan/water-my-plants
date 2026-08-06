---
title: Adopt Figma Documentation Sync in a Gradle repository
type: guide
scope: repo/figma-documentation-sync
owner: figma-documentation-sync
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - repo/figma-documentation-sync/plugin/src/main/kotlin/com/marmatsan/figmaDocumentationSync/plugin/gradle/FigmaDocumentationSyncGradlePlugin.kt
  - repo/figma-documentation-sync/plugin/src/main/kotlin/com/marmatsan/figmaDocumentationSync/plugin/gradle/FigmaCatalogChecksExtension.kt
  - repo/figma-documentation-sync/teamcity-operations/src/main/kotlin/com/marmatsan/figmaDocumentationSync/teamcity/operations/gradle/FigmaTeamCityOperationsExtension.kt
  - repo/figma-documentation-sync/domain/src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/model/writer/FigmaWriterProjectConfig.kt
  - repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/json/writer/FigmaWriterProjectConfigJson.kt
  - repo/project-config/figma-adapter/src/main/kotlin/com/marmatsan/projectConfig/figma/ProjectConfigFigmaGradlePlugin.kt
  - repo/figma-documentation-sync/tools/bin/build.mjs
  - repo/figma-documentation-sync/samples/standalone-consumer
---

# Adopt Figma Documentation Sync in a Gradle Repository

## Outcome

A Gradle repository consumes versioned `project-config` and Figma Documentation
Sync plugins without including either source build. The repository owns its
dependency trees and Figma identities, may select an optional CI adapter, and
builds the portable writer with a transient JSON projection of its typed Kotlin
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
  [the project-config consumer contract](../../../project-config/docs/reference/consumer-contract.md).

## Steps

1. Add the release Maven repositories to `pluginManagement` and select the
   project-config Settings and Figma adapter plugins:

   ```kotlin
   pluginManagement {
       repositories {
           maven("https://packages.example.com/figma-documentation-sync")
           gradlePluginPortal()
           mavenCentral()
       }
       plugins {
           id("com.marmatsan.projectConfig.settings") version "<project-config-version>"
           id("com.marmatsan.projectConfig.figma") version "<project-config-version>"
       }
   }
   ```

2. Create a repository-owned `versions.properties`, apply the reusable Settings
   plugin, and declare the product catalog tree inline in root
   `settings.gradle.kts`:

   ```kotlin
   plugins {
       id("com.marmatsan.projectConfig.settings")
   }

   projectConfig {
       versionsFile.set(file("versions.properties"))
       dependencyCatalog {
           libraries {
               root("com") {
                   library("example") {
                       artifact(
                           artifact = "client",
                           version = version("exampleClientLibraryVersion")
                       )
                   }
               }
           }
           plugins {
               root("org") {
                   plugin(
                       id = "example",
                       version = version("examplePluginVersion")
                   )
               }
           }
       }
   }
   ```

3. Apply the optional reusable adapter in root `build.gradle.kts`. It applies
   the base project-config and Figma plugins, enriches both catalog trees with
   usage, and supplies their serialized task input without a reflective
   provider:

   ```kotlin
   plugins {
       id("com.marmatsan.projectConfig.figma")
   }
   ```

4. Configure `figmaDocumentationSync` with consumer-owned identities, paths,
   catalog names, and CI inputs. Do not set
   `dependencyCatalogProviderClassName`; the project-config adapter supplies
   `dependencyCatalogTreesJson`:

   ```kotlin
   figmaDocumentationSync {
       metadataNamespace.set("example_project_sync")
       designModelMetadataNodeUrl.set(
           "https://www.figma.com/design/<file-key>/<file>?node-id=<page-node>"
       )
       primaryCatalogModelName.set("exampleProject")
       versionsFile.set(layout.projectDirectory.file("versions.properties"))
       toolsDirectory.set(layout.buildDirectory.dir("figma-documentation-sync-tools"))
   }
   ```

5. Create a repository-owned `FigmaWriterProjectConfig`, encode it with
   `FigmaWriterProjectConfigJson`, and assign the result to
   `figmaDocumentationSync.writerProjectConfigJson`. The portable plugin writes
   the transient file and wires it into canonical, visual-plan, MCP execution,
   and probe tasks. The model supplies
   repository paths, Figma component identities, visual targets, and the
   relative repository root used for writer fingerprints. Keep the JSON under
   `build/`; do not version it.

6. Add `@marmatsan/figma-documentation-sync-tools` at the same version and materialize
   the configured writer from that transient JSON.

   ```powershell
   npm install --save-dev @marmatsan/figma-documentation-sync-tools@<version>
   npx figma-documentation-sync-build `
       --project-config-json=build\generated\figma-documentation-sync\writer-project-config.json `
       --output-dir=build\figma-documentation-sync-tools
   ```

7. Configure included builds through `figmaDocumentationSync.includedBuilds`
   only when they contribute modules or usage metadata to the generated model.
   Keep `publishesCatalogs` false unless the host explicitly chooses that
   included build as product documentation. Water My Plants publishes only its
   production `libraries` and `plugins` trees.

8. For TeamCity CI documentation, add the optional
   `com.marmatsan.figma-documentation-sync:figma-documentation-sync-teamcity-adapter:<version>`
   dependency and select `TeamCityCiConfigurationProvider`. To expose the
   supervised handoff, upload, and rerun tasks, also apply
   `com.marmatsan.figmaDocumentationSync.teamcityOperations` and configure
   `figmaTeamCityOperations` with the consumer's build configuration, branch,
   accepted aliases, artifact-producing job name, and HTTPS origin. Other
   repositories may supply a sibling `CiConfigurationProvider` or omit both
   TeamCity capabilities.

9. Add the relevant verification tasks to CI. Treat the model generated on the
   default branch as the only canonical publication input.

## Verification

Run the standalone staged-consumer test before adopting a release:

```powershell
.\gradlew.bat -p repo\figma-documentation-sync verifyStagedPublication
```

In the consumer repository, verify plugin application and its focused checks:

```powershell
.\gradlew.bat tasks --group verification
.\gradlew.bat checkFigmaVersionNaming checkFigmaCatalogUsage
```

Also verify the catalog settings plugin and staged distribution contract before
selecting a release:

```powershell
.\gradlew.bat -p repo\dependency-catalog :catalog-gradle-plugin:test verifyStagedPublication
.\gradlew.bat -p repo\figma-documentation-sync verifyStagedPublication
```

Build the configured writer and run its tests before enabling a canonical
Figma write. Do not generate or publish canonical metadata from a feature
branch.

## Related Documentation

- [Distribution contract](../reference/distribution-contract.md)
- [Publication runbook](../runbooks/publishing-release.md)
- [Project Config consumer contract](../../../project-config/docs/reference/consumer-contract.md)
- [Canonical trunk sync](../runbooks/trunk-sync.md)
- [Reusable dependency catalog adoption](../../../dependency-catalog/docs/guides/adopt-dependency-catalog.md)
