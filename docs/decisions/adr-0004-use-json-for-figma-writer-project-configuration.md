---
title: Use JSON for Figma writer project configuration
type: adr
scope: repository
owner: figma-design-sync
status: accepted
last-reviewed: 2026-07-19
review-cycle-days: 365
sources:
  - repo/figma-design-sync/domain/src/main/kotlin/com/marmatsan/figmaDesignSync/domain/model/writer/FigmaWriterProjectConfig.kt
  - repo/figma-design-sync/data/src/main/kotlin/com/marmatsan/figmaDesignSync/data/json/writer/FigmaWriterProjectConfigJson.kt
  - repo/figma-design-sync/tools/bin/build.mjs
  - repo/figma-design-sync/project-config/src/main/kotlin/com/marmatsan/figmaDesignSync/projectConfig/WaterMyPlantsFigmaWriterProjectConfig.kt
  - docs/decisions/adr-0002-distribute-figma-design-sync-as-gradle-plugin.md
---

# ADR-0004: Use JSON for Figma Writer Project Configuration

## Context

The portable writer originally accepted a repository-owned TypeScript module.
During the Kotlin-first migration, Water My Plants also introduced a typed
`FigmaWriterProjectConfig` and projected it to schema-versioned JSON. Keeping
both representations required a parity test and left two editable sources for
Figma identities, component properties, target scopes, and repository paths.

The Figma Plugin API boundary still needs JavaScript values at bundle time, but
that requirement does not require repository configuration to be authored in
TypeScript. The build can materialize a virtual module from language-neutral
JSON while Kotlin remains the only source of project-specific values.

## Decision

Use schema-versioned JSON as the only public project-configuration input of
`figma-design-sync-build`. Repository adapters own a typed Kotlin
`FigmaWriterProjectConfig` and generate the transient JSON through
`FigmaWriterProjectConfigJson`.

The npm package accepts the JSON path through `--project-config-json` or the
`FIGMA_DESIGN_SYNC_PROJECT_CONFIG` environment variable. Its esbuild adapter
materializes the internal TypeScript module at bundle time. Water My Plants
exposes Kotlin-configured Gradle tasks for building and testing the TypeScript
boundary, so local and CI workflows do not maintain a second configuration
source.

## Consequences

- Water My Plants has one editable writer configuration, in Kotlin.
- Consumers can create the JSON projection without importing TypeScript
  project code or copying this repository's identities.
- The JSON schema becomes a release contract and must remain versioned and
  validated.
- The TypeScript package still owns a small build adapter because esbuild must
  inject configuration into Figma runtime bundles.
- Tests run through the repository adapter's Gradle task so they receive the
  same Kotlin-generated configuration as production builds.

## Alternatives

- Keep Kotlin and TypeScript configs with parity testing. Rejected because the
  test detects drift after duplicating every value rather than eliminating the
  duplicate source.
- Publish the Water My Plants TypeScript config with the package. Rejected
  because it couples a portable artifact to one Figma file and repository.
- Generate TypeScript source into the repository. Rejected because generated
  source adds no contract beyond JSON and creates another artifact to manage.

## Supersession

Partially supersedes [ADR-0002](adr-0002-distribute-figma-design-sync-as-gradle-plugin.md):
the distribution remains a Gradle plugin plus npm writer package, but the
writer no longer accepts a repository-owned TypeScript configuration module.
