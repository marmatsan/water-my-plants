---
title: Separate the product catalog from build-tool versions
type: adr
scope: repository
owner: architecture
status: accepted
last-reviewed: 2026-07-26
review-cycle-days: 365
sources:
  - repo/dependency-catalog/catalog-api/src/main/kotlin/com/marmatsan/dependencies/catalog/api/DependencyCatalogProvider.kt
  - repo/dependency-catalog/catalog-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/DependencyCatalogSettingsPlugin.kt
  - repo/water-my-plants-project-config/catalog/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/catalog/WaterMyPlantsCatalogProvider.kt
  - repo/water-my-plants-project-config/plugin/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/WaterMyPlantsFigmaWriterProjectConfig.kt
  - repo/verification-platform/domain/src/main/kotlin/com/marmatsan/verificationPlatform/domain/service/CiPlanFactory.kt
---

# ADR-0009: Separate the Product Catalog From Build-Tool Versions

## Context

The repository previously used one Water My Plants version registry to compile
unrelated included builds and published every discovered catalog tree to Figma.
That coupled independently evaluable tooling builds to the application catalog,
made the visual model describe repository implementation detail, and suggested
that every included build needed a repository-specific `*-catalog` module.

The catalog tree model is useful outside Water My Plants, but the application
dependency declarations, included-build toolchains, and Figma identities have
different owners and release reasons.

## Decision

- `catalog-api` owns the immutable model and `DependencyCatalogProvider` port.
  `catalog-core` is an optional reusable tree DSL for implementing that port.
- `catalog-gradle-plugin` publishes the settings plugin
  `com.marmatsan.dependencyCatalog`. Its terminal
  `dependencyCatalog.from(provider)` operation registers `libs` and `plugins`
  while Gradle evaluates settings.
- A consuming repository owns one provider for each product catalog it chooses
  to expose. It does not create a `*-catalog` module for every included build.
- Every included build owns a local `versions.properties` for the dependencies
  needed to compile and test that build. Cross-build reads of another build's
  registry are forbidden. Deliberate duplicated version values are acceptable;
  they express independent ownership rather than a shared runtime contract.
- `repo/water-my-plants-project-config/versions.properties` is the Water My
  Plants product catalog source. `repo/dependency-catalog/versions.properties`
  contains only the reusable build's compile/test versions.
- The Water My Plants Figma adapter publishes only
  `waterMyPlants.libraries` and `waterMyPlants.plugins` as catalog-tree visual
  targets. Included-build modules and dependency edges may remain in the model
  for architecture and usage analysis, but their tool catalogs and custom
  plugin inventories are not Figma catalog-tree targets.
- CI keeps one authoritative `TeamCity CI` status. The typed plan selects local
  version ownership and catalog architecture checks for catalog changes, and a
  staged standalone-consumer verification for portable publication changes.
  Canonical Figma publication remains a post-merge `main` workflow.

The dependency direction is:

```text
repository provider -> catalog-api (+ optional catalog-core)
catalog-gradle-plugin -> catalog-api
Gradle settings adapter -> catalog-gradle-plugin + repository provider
Water My Plants Figma adapter -> catalog-api + figma-documentation-sync port
Water My Plants catalog -> catalog-api + catalog-core
```

## Consequences

- Included builds can be evaluated, tested, and versioned without reading the
  Water My Plants catalog registry.
- Another repository can consume staged or released Maven artifacts and supply
  its own provider and versions file without including this source tree.
- A shared dependency version may intentionally appear in more than one local
  registry. Alignment is required only when an explicit compatibility or
  publication contract says so.
- Removing obsolete catalog targets from configuration does not delete legacy
  Figma sections automatically. Their one-time removal is part of the
  supervised post-merge visual migration before metadata is finalized.
- Public artifacts need coordinated Maven versions and a standalone consumer
  gate; external registry selection, credentials, signing, and npm publication
  remain separate release decisions.

## Alternatives

- One `*-catalog` module per included build was rejected because build-tool
  dependencies do not become product architecture merely by living in an
  included build.
- One global `versions.properties` was rejected because it creates settings-time
  coupling and prevents independent build consumption.
- Publishing every discovered repository catalog to Figma was rejected because
  it obscures the dependency tree used to produce the application.
- Copying the catalog DSL or Figma engine into adopter repositories was rejected
  in favor of versioned Gradle and Maven contracts.

## Supersession

None.
