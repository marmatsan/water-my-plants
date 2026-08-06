---
title: Isolate product composition from reusable builds
type: adr
scope: repository
owner: architecture
status: superseded
last-reviewed: 2026-07-27
review-cycle-days: 365
sources:
  - settings.gradle.kts
  - repo/figma-documentation-sync/settings.gradle.kts
  - repo/water-my-plants-project-config/settings.gradle.kts
  - repo/water-my-plants-project-config/plugin/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/figma/configuration/WaterMyPlantsFigmaWriterProjectConfig.kt
  - repo/water-my-plants-project-config/plugin/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/gradle/WaterMyPlantsProjectConfigPlugin.kt
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/extension/VerificationPlatformExtension.kt
---

# ADR-0010: Isolate Product Composition From Reusable Builds

## Context

The repository tooling is split into Gradle included builds so Dependency
Catalog, Figma Documentation Sync, Gradle convention plugins, and Verification
Platform can evolve and be consumed independently. A build is not independent
when its production code or settings know a sibling build's filesystem path,
concrete provider, task name, or Water My Plants identity.

Consuming a versioned API does not violate independence. Some component must
still select concrete implementations and connect them to the application, so
complete ignorance is neither possible nor desirable at the composition
boundary.

## Decision

- Reusable included builds may depend only on documented API artifacts. They
  must not import another build's product implementation, include a sibling by
  relative path, or embed the sibling's repository path or task identity.
- A reusable build MAY accept an externally supplied source-build location for
  composite substitution of a versioned plugin. The reusable build owns only
  the property contract; the product composition root owns and injects the
  concrete path. Without the override, the build resolves the published
  coordinate.
- `repo/water-my-plants-project-config` is the single product composition
  build. It is the only included build allowed to know Water My Plants catalog
  providers, Figma identities, TeamCity adapters, included-build names, and
  repository paths.
- The root `settings.gradle.kts` and the product composition build are the only
  composite assemblers. Reusable builds resolve versioned coordinates; the
  composition boundary substitutes local included-build publications during
  repository development.
- Dependency Catalog exposes a narrow provider/model API independently from
  its tree-building DSL and Gradle adapter.
- Figma Documentation Sync owns the catalog input port used by its engine. A
  product adapter maps Dependency Catalog's public model into the Figma-owned
  model; the reusable Figma build does not import Dependency Catalog types.
- Gradle convention plugins do not select a product catalog. The Water My
  Plants settings plugin performs that selection in the product composition
  build.
- Verification Platform receives verification units, paths, version registries,
  and included-build task bindings through its public Gradle extension. Its
  domain and adapters contain no Water My Plants repository inventory.
- CI enforces an allow-listed dependency matrix and rejects relative sibling
  includes, product identities in reusable production code, and unapproved
  cross-build artifacts.

The allowed dependency direction is:

```text
water-my-plants root
        |
        v
water-my-plants-project-config
        |-- dependency-catalog API + Water My Plants implementation
        |-- figma-documentation-sync API
        |-- gradle convention-plugin APIs
        `-- verification-platform API

dependency-catalog implementation -> dependency-catalog API
figma-documentation-sync implementation -> figma-documentation-sync domain API
gradle-plugins implementation -> gradle-plugins internal APIs
verification-platform implementation -> verification-platform domain API
```

No reusable included build has a lateral dependency on another reusable
included build.

## Consequences

- Reusable builds can be checked out and built against staged or released API
  coordinates without assuming the Water My Plants directory layout.
- Product wiring remains explicit and testable instead of being hidden inside
  otherwise portable modules.
- The product composition owns exact external Figma identities such as node ids,
  variable collection names, and component names. When a repository path or
  Figma template changes, migrate the existing external resource in place when
  identity must be preserved and update the typed composition contract; do not
  embed the replacement identity in the reusable writer.
- The composition build intentionally has more dependencies than other builds;
  that concentration is the boundary rather than a violation.
- Direct standalone builds require a staged or released repository containing
  their API dependencies. Root composite development continues to use local
  dependency substitution.
- Existing tests that verify an API implementation move with the owning module
  instead of reaching across builds.
- Adding a new reusable build requires declaring its public APIs and updating
  the allow-listed boundary contract, not adding product knowledge to existing
  builds.

## Alternatives

- Keeping Water My Plants adapters inside each reusable build was rejected
  because it makes those builds product-specific and spreads repository-layout
  knowledge across multiple composition roots.
- Removing every dependency between modules was rejected because consumers
  must know the APIs they compile against and some root must select concrete
  implementations.
- Sharing sources through relative `includeBuild("../...")` declarations was
  rejected because it prevents independent checkout and distribution.
- Using reflection strings without a public provider interface was rejected
  because it hides compile-time coupling without creating an enforceable
  contract.

## Supersession

Superseded by
[ADR-0013](adr-0013-use-reusable-project-config.md), which retains the dependency
direction while replacing the product-specific composition build with a
reusable project-config build. This decision refined the dependency direction
recorded by ADR-0009.
