---
title: Figma Design Sync distribution contract
type: reference
scope: repo/figma-design-sync
owner: figma-design-sync
status: active
last-reviewed: 2026-07-19
review-cycle-days: 180
sources:
  - repo/figma-design-sync/gradle.properties
  - repo/figma-design-sync/build.gradle.kts
  - repo/figma-design-sync/plugin/build.gradle.kts
  - repo/figma-design-sync/domain/src/main/kotlin/com/marmatsan/figmaDesignSync/domain/model/writer/FigmaWriterProjectConfig.kt
  - repo/figma-design-sync/data/src/main/kotlin/com/marmatsan/figmaDesignSync/data/json/writer/FigmaWriterProjectConfigJson.kt
  - repo/figma-design-sync/tools/package.json
  - repo/figma-design-sync/tools/fixtures/contracts/writer-runtime-contract.json
  - repo/figma-design-sync/samples/standalone-consumer
---

# Figma Design Sync Distribution Contract

## Purpose

This reference defines the artifacts, versions, public entry points, and
release gates used to consume Figma Design Sync without source modules or an
included build.

## Contract

The public Gradle entry point is:

```text
plugin id: com.marmatsan.figmaDesignSync
```

The Maven publication set is:

| Coordinate | Visibility | Responsibility |
|------------|------------|----------------|
| `com.marmatsan.figma-design-sync:figma-design-sync-gradle-plugin` | Public entry point | Gradle plugin implementation and tasks. |
| `com.marmatsan.figma-design-sync:figma-design-sync-domain` | Transitive implementation | Portable models and ports. |
| `com.marmatsan.figma-design-sync:figma-design-sync-data` | Transitive implementation | Portable filesystem, Gradle, catalog, and Figma adapters. |
| `com.marmatsan.repo:catalog-core` | Transitive supporting API | Reusable dependency catalog model used by catalog providers. |
| `com.marmatsan.figma-design-sync:figma-design-sync-teamcity-adapter` | Optional | TeamCity parser and typed CLI boundary. |

Gradle also publishes the standard plugin marker coordinate generated for
`com.marmatsan.figmaDesignSync`. Consumers use the plugin id rather than the
implementation coordinate directly.

The portable writer package is:

```text
@marmatsan/figma-design-sync-tools
```

Its `figma-design-sync-build` executable accepts:

| Argument | Required | Meaning |
|----------|----------|---------|
| `--project-config-json=PATH` | One project-config input | Schema-versioned JSON projection of the typed Kotlin writer configuration. |
| `--output-dir=PATH` | No | Materialized tool workspace; defaults to the current directory. |

`FIGMA_DESIGN_SYNC_PROJECT_CONFIG` may provide the JSON path instead of the
command-line argument; an explicit argument takes precedence. The
portable `FigmaWriterProjectConfig` model and `FigmaWriterProjectConfigJson`
adapter define JSON schema version `1`. Repository adapters own the model
values; generated JSON is a transient build input rather than a reviewed
source file.

The materialized directory contains the compiled Figma Plugin API writer, the
preview-only runner generator, portable writer sources used for
fingerprints, and visual fixtures. `figmaDesignSync.toolsDirectory` points to
this directory. Official runner generation, capability probing, MCP transport,
and checkpoints are Kotlin services delivered by the Maven plugin and its
transitive `domain` and `data` artifacts.

The language-neutral
`tools/fixtures/contracts/writer-runtime-contract.json` fixture defines the
observable runner manifest, target, transport, hash, and execution-scope
contract. The Kotlin runner tests own its executable coverage; the retired
TypeScript implementation remains out of the published package.

The project config owns both
`REPOSITORY_ROOT_RELATIVE_TO_TOOLS` and
`CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY`. This prevents the portable
package from assuming the Water My Plants directory structure.

`repo/figma-design-sync/gradle.properties` owns the staged Maven version.
`tools/package.json` must contain the same version. The
`verifyPublicationVersionAlignment` task rejects drift.

## Invariants

- Consumers apply one Gradle plugin; they do not include or address the
  internal `domain`, `data`, or `plugin` projects.
- `project-config` is never part of the portable publication set.
- Repository identities enter the writer through a transient project-config
  projection; generated JSON is not published as a source artifact.
- The TeamCity adapter is never a transitive dependency of the portable
  Gradle plugin.
- Maven artifacts, the plugin marker, `catalog-core`, and the npm package use
  one release version.
- The npm package remains `private` until an explicit release authorizes the
  selected registry and namespace.
- A release must pass `verifyStagedPublication`, which resolves the plugin from
  Maven files and applies it from `samples/standalone-consumer` without an
  included build.
- Publication credentials and signing material never enter versioned files.

## Sources

- [`../../build.gradle.kts`](../../build.gradle.kts)
- [`../../plugin/build.gradle.kts`](../../plugin/build.gradle.kts)
- [`../../tools/package.json`](../../tools/package.json)
- [`../../tools/bin/build.mjs`](../../tools/bin/build.mjs)
- [`../../domain/src/main/kotlin/com/marmatsan/figmaDesignSync/domain/model/writer/FigmaWriterProjectConfig.kt`](../../domain/src/main/kotlin/com/marmatsan/figmaDesignSync/domain/model/writer/FigmaWriterProjectConfig.kt)
- [`../../data/src/main/kotlin/com/marmatsan/figmaDesignSync/data/json/writer/FigmaWriterProjectConfigJson.kt`](../../data/src/main/kotlin/com/marmatsan/figmaDesignSync/data/json/writer/FigmaWriterProjectConfigJson.kt)
- [`../../tools/fixtures/contracts/writer-runtime-contract.json`](../../tools/fixtures/contracts/writer-runtime-contract.json)
- [`../../samples/standalone-consumer`](../../samples/standalone-consumer)
- [`../../project-config/src/main/kotlin/com/marmatsan/figmaDesignSync/projectConfig/WaterMyPlantsFigmaWriterProjectConfig.kt`](../../project-config/src/main/kotlin/com/marmatsan/figmaDesignSync/projectConfig/WaterMyPlantsFigmaWriterProjectConfig.kt)
