---
title: Name the portable infrastructure Figma Documentation Sync
type: adr
scope: repository
owner: figma-documentation-sync
status: accepted
last-reviewed: 2026-07-26
review-cycle-days: 365
sources:
  - repo/figma-documentation-sync/settings.gradle.kts
  - repo/figma-documentation-sync/gradle.properties
  - repo/figma-documentation-sync/plugin/build.gradle.kts
  - repo/project-config/figma-adapter/build.gradle.kts
  - repo/figma-documentation-sync/tools/package.json
  - repo/figma-documentation-sync/docs/reference/distribution-contract.md
---

# ADR-0005: Name the Portable Infrastructure Figma Documentation Sync

## Context

The original `figma-design-sync` name described the first design-model use
case. The infrastructure now models and verifies dependency catalogs, Gradle
modules, CI topology, repository versions, and other repository-derived Figma
documentation. It is a portable Kotlin Gradle plugin with a narrow TypeScript
Figma Plugin API writer, not a product-screen design synchronization engine.

No public release has been published yet. Keeping compatibility aliases before
the first release would permanently expose two names without protecting an
existing consumer.

## Decision

Name the subsystem and its portable distribution **Figma Documentation Sync**.
Use these public identities:

| Surface | Identity |
|---------|----------|
| Repository path and included build | `repo/figma-documentation-sync`, `figma-documentation-sync` |
| Kotlin package | `com.marmatsan.figmaDocumentationSync` |
| Portable Gradle plugin | `com.marmatsan.figmaDocumentationSync` |
| Reusable project-config adapter | `com.marmatsan.projectConfig.figma` |
| Maven group and core artifacts | `com.marmatsan.figma-documentation-sync:{domain,data,plugin}` |
| TypeScript package and executable | `@marmatsan/figma-documentation-sync-tools`, `figma-documentation-sync-build` |
| Gradle extension | `figmaDocumentationSync` |

Do not add aliases for the pre-publication identities.

Retain the architecture decisions captured by ADR-0002, ADR-0003, and
ADR-0004: consumers apply one Gradle plugin, the live Figma Plugin API boundary
stays in TypeScript, and Kotlin-generated JSON remains the only writer project
configuration input.

Operational and model vocabulary remains stable when it describes the
behavior rather than the distribution: `Figma Sync`, `FigmaDesignModel`,
`design-model.json`, `build/reports/figma-sync`, and the existing public task
names continue to mean the same thing.

## Consequences

- The public name now matches the broader repository-documentation contract.
- Future consumers receive one coherent identity across Gradle, Maven, npm,
  Kotlin, documentation, and source layout.
- Existing local integrations must migrate before the first release; the
  repository performs that migration atomically.
- Catalog target identities derived from the included-build name become
  `figmaDocumentationSync.libraries` and `figmaDocumentationSync.plugins`, so
  the official post-merge Figma Sync must publish the changed visual state.
- Historical ADRs keep their original wording and paths and are marked
  superseded rather than rewritten.

## Alternatives

- Keep `figma-design-sync`. Rejected because it implies product design sync and
  hides CI, dependency, module, and documentation responsibilities.
- Use a generic name such as `documentation-sync`. Rejected because Figma is a
  defining runtime, API, metadata, and publication boundary.
- Publish compatibility aliases. Rejected before the first release because no
  external compatibility contract exists yet.

## Supersession

Supersedes
[ADR-0002](adr-0002-distribute-figma-design-sync-as-gradle-plugin.md),
[ADR-0003](adr-0003-keep-figma-runtime-boundary-in-typescript.md), and
[ADR-0004](adr-0004-use-json-for-figma-writer-project-configuration.md) while
retaining their architectural choices under the new identity.
