---
title: Coordinate repository Gradle plugin releases
type: adr
scope: repository
owner: repository-tooling
status: accepted
last-reviewed: 2026-07-29
review-cycle-days: 365
sources:
  - repo/gradle-plugins/build.gradle.kts
  - repo/gradle-plugins/versions.properties
  - versions.properties
  - settings.gradle.kts
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/boundary/CheckIncludedBuildVersionsTask.kt
  - repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/gradle/catalog/GradleMainCatalogUsageReader.kt
---

# ADR-0012: Coordinate Repository Gradle Plugin Releases

## Context

`repo/gradle-plugins` publishes several convention plugins from one included
build. They share build configuration, dependency helpers, publication gates,
and compatibility assumptions, so independent version keys would imply release
boundaries that do not exist. The product catalog previously named their common
version `gradleConventionPluginVersion`, which described an implementation
category rather than the published release unit.

Figma also displayed only that version reference and its custom-plugin
inventories derived usage from literal plugin ids. Type-safe catalog aliases
and plugins applied to the root project therefore appeared unused even when
the executable Gradle build applied them.

## Decision

- Every plugin published by `repo/gradle-plugins` belongs to one coordinated
  release train named `gradlePluginsVersion`.
- The producer and each consumer keep their own local `versions.properties`.
  They use the same key because they participate in one compatibility contract,
  while root verification compares the values without allowing either build to
  read the other's registry.
- A plural `PluginsVersion` suffix denotes a coordinated plugin release train.
  Independently versioned plugins retain the singular `PluginVersion` suffix.
- The Water My Plants plugin catalog renders the source version name for
  `gradlePluginsVersion`; the internal resolved value and sharing policy remain
  model inputs rather than additional visual labels. The portable writer
  contains no Water My Plants-specific branch.
- The separate Gradle convention plugin and Gradle plugin inventories remain
  Figma documentation targets. Their usage model combines applied literal ids
  and type-safe plugin aliases, includes the root module as `:`, and ignores
  declarations marked `apply false`.
- Figma remains derived documentation. The reader, model, tests, and alignment
  check are authoritative; visual nodes are not corrected manually around a
  stale model.

## Consequences

- One release updates every convention plugin marker together and consumers
  cannot silently select incompatible versions from that build.
- Local version ownership remains intact, so reusable included builds stay
  independently evaluable and do not gain filesystem coupling.
- Figma explains why several plugin ids share one version while accurately
  showing application modules and root tooling that apply repository plugins.
- Splitting one plugin into an independent release later requires a new key,
  publication contract, compatibility gate, and a superseding decision.

## Alternatives

- One version key per convention plugin was rejected because the producer does
  not publish or verify those plugins independently.
- Reading the producer's `versions.properties` from the product catalog was
  rejected because it would violate included-build ownership and portability.
- Keeping only the version alias in Figma was rejected because it obscures the
  resolved value and shared release policy.
- Repairing usage labels directly in Figma was rejected because the next
  canonical sync would reproduce the incorrect source model.

## Supersession

This decision refines ADR-0009. Its statement that custom plugin inventories
are not Figma targets no longer applies; included-build dependency catalogs
remain excluded from the Water My Plants production catalog visualization.
