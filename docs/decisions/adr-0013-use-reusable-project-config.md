---
title: Use reusable project configuration
type: adr
scope: repository
owner: repository-tooling
status: accepted
last-reviewed: 2026-08-06
review-cycle-days: 365
sources:
  - settings.gradle.kts
  - build.gradle.kts
  - repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/catalog/definition/DependencyCatalogDefinition.kt
  - repo/project-config/settings.gradle.kts
  - repo/project-config/figma-adapter/src/main/kotlin/com/marmatsan/projectConfig/figma/ProjectConfigFigmaRegistration.kt
---

# ADR-0013: Use Reusable Project Configuration

## Context

`repo/water-my-plants-project-config` isolates product composition from reusable
repository tooling, but it also compiles Water My Plants catalog trees, Figma
identities, repository paths, and CI selections into a product-specific
included build. A second product would therefore need another near-identical
`*-project-config` build even though most configuration and orchestration
behavior is reusable.

The product catalog declaration has an additional constraint: Gradle needs
resolved version values while Figma documentation needs stable version-property
aliases. Both views must come from one tree declaration so they cannot drift.

## Decision

- Replace the product-specific composition build with one reusable included
  build named `repo/project-config`.
- `project-config` MUST contain no product name, Figma node id, repository path,
  TeamCity build id, catalog contents, or other consumer identity.
- The consuming repository supplies product-specific configuration through
  type-safe Kotlin DSL in its root `settings.gradle.kts` and `build.gradle.kts`.
- Library and plugin trees remain inline in `settings.gradle.kts`. This
  migration does not introduce TOML, JSON, applied scripts, or another external
  catalog-tree format.
- Dependency Catalog owns a reusable catalog-definition contract. One
  declaration materializes both the resolved Gradle catalog and the
  version-aliased documentation catalog.
- Existing consumer-owned data files such as `versions.properties` and the
  change-impact policy remain data inputs. The reusable `project-config` build
  owns a separate `versions.properties` only for compiling and testing itself.
- Settings and project plugins expose the reusable composition entry points.
  Cross-capability adapters depend only on public APIs, and operational Figma or
  TeamCity behavior remains owned by those capabilities rather than by one
  product.
- A source-independent fixture with a different product identity, initially
  `Health`, verifies that the reusable build contains no hidden Water My Plants
  assumptions.
- Water My Plants applies the reusable Settings plugin and optional Figma
  adapter directly. The former `water-my-plants-project-config` build and its
  plugin ids are removed after the standalone fixture passes its publication
  and configuration-cache contracts.

## Consequences

- New products configure the same included build instead of creating another
  product-specific module.
- Product trees remain type-safe, navigable, documented Kotlin code, at the cost
  of allowing the root settings script to grow.
- The reusable build gains a public DSL and staged-publication compatibility
  contract that require semantic-versioning discipline.
- Water My Plants source paths, Figma source links, plugin inventory, boundary
  rules, and documentation now point to root consumer configuration even though
  the dependency tree content remains identical.
- The architecture avoids a generic megaplugin by keeping catalog declaration,
  cross-capability mapping, Figma operations, and TeamCity operations in
  focused collaborators and modules.

## Alternatives

- Keep one `*-project-config` build per product. Rejected because it duplicates
  composition behavior and makes reuse depend on copying an example build.
- Move catalog trees to separate TOML, JSON, YAML, or applied Kotlin scripts.
  Rejected for this migration because the existing Kotlin tree DSL, KDoc,
  validation, and IDE navigation are part of the selected contract.
- Put product configuration inside the reusable build behind conditionals.
  Rejected because it reverses the dependency direction and would require a new
  product branch in shared code.
- Remove the composition boundary and configure every reusable plugin directly.
  Rejected because it would distribute cross-capability wiring and repository
  identity across root scripts without one validated contract.

## Supersession

This decision supersedes the product-specific composition-build selection in
[ADR-0010](adr-0010-isolate-product-composition-from-reusable-builds.md). Its
rules prohibiting lateral dependencies and product knowledge in reusable builds
remain active. This decision also refines, without superseding, the local
version-ownership rules in
[ADR-0009](adr-0009-separate-product-catalog-from-build-tool-versions.md).
