---
title: Figma Design Sync distribution contract
type: reference
scope: repo/figma-design-sync
owner: figma-design-sync
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - repo/figma-design-sync/gradle.properties
  - repo/figma-design-sync/build.gradle.kts
  - repo/figma-design-sync/plugin/build.gradle.kts
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
| `--project-config=PATH` | Yes | TypeScript module exporting the repository's Figma identities and visual target configuration. |
| `--output-dir=PATH` | No | Materialized tool workspace; defaults to the current directory. |

The materialized directory contains the compiled writer, checkpoint executor,
runner generator, portable writer sources used for fingerprints, and visual
fixtures. `figmaDesignSync.toolsDirectory` points to this directory.

The language-neutral
`tools/fixtures/contracts/writer-runtime-contract.json` fixture defines the
observable runner manifest, target, transport, hash, and execution-scope
contract. Replacement implementations, including gradual Kotlin migrations,
must satisfy this executable baseline before the corresponding TypeScript
implementation is removed.

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
- [`../../tools/fixtures/contracts/writer-runtime-contract.json`](../../tools/fixtures/contracts/writer-runtime-contract.json)
- [`../../samples/standalone-consumer`](../../samples/standalone-consumer)
- [`../../project-config/water-my-plants/figma-config.ts`](../../project-config/water-my-plants/figma-config.ts)
