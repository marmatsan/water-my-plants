---
title: Product architecture standard
type: standard
scope: repository
owner: architecture
status: active
last-reviewed: 2026-07-27
review-cycle-days: 180
sources:
  - build.gradle.kts
  - settings.gradle.kts
  - docs/reference/project-structure.md
  - repo/gradle-plugins
  - repo/dependency-catalog/catalog-api
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/CheckModuleBoundariesTask.kt
---

# Product Architecture Standard

## Purpose

Define dependency direction and ownership for production Android code without
committing the project to infrastructure that has not yet been selected.

## Module Boundaries

- `:app` MUST remain the application and composition boundary. It may assemble
  feature and core modules but SHOULD NOT own reusable feature behavior.
- `:core:*` modules MUST contain capabilities shared by multiple features and
  MUST NOT depend on `:app` or a feature module.
- Feature modules such as `:onboarding:*` MUST NOT depend on `:app` or another
  feature. Shared behavior moves to an appropriately scoped `:core:*` module.
- Product modules MUST NOT depend on included builds under `repo/` as runtime
  libraries. They consume repository tooling only through Gradle plugins and
  generated catalogs.
- Gradle module dependencies MUST use type-safe project accessors.

The existing greeting classes in `:app` are scaffold code, not an architectural
precedent for future production features.

## Dependency Direction

New domain behavior MUST depend on abstractions it owns, not concrete Android,
network, database, or Gradle implementations. Infrastructure adapters may
depend inward on those abstractions. Android UI may depend on domain-facing
interfaces but domain code MUST remain free of Android framework types.

Cross-layer types MUST be explicit. Transport DTOs, persistence entities, and
Compose state are not domain models and MUST NOT leak across their boundary.

## Public API

- Declarations SHOULD be `internal` unless another module consumes them.
- A module's public API MUST represent its capability, not its package layout.
- Cyclic module dependencies are prohibited.
- New architectural layers, shared modules, or cross-feature dependencies MUST
  be justified by an ADR before implementation.

## Composition

Constructor injection is the default. Composition roots may select concrete
implementations; domain and UI classes MUST NOT use service locators or read a
global dependency container.

## SOLID Design

SOLID is a review-blocking design contract for all newly implemented or
materially changed code. The type-level rules below apply to production code
and test support that contains reusable behavior, across product modules,
reusable builds, Gradle plugins, adapters, and composition code.

- **Single Responsibility Principle:** a type MUST have one cohesive reason to
  change. Reading state, mapping models, enriching data, registering tasks,
  configuring an extension, and executing runtime behavior are separate
  responsibilities unless the type is an immutable value that represents one
  contract. A composition root MAY know concrete implementations, but it MUST
  delegate their configuration and behavior to focused collaborators.
- **Open/Closed Principle:** reusable behavior MUST be extended through a
  stable port, strategy, provider, or configuration model instead of adding
  product-specific branches to reusable modules. Do not create an abstraction
  without a real consumer boundary or variation point.
- **Liskov Substitution Principle:** every implementation MUST preserve its
  interface's inputs, outputs, failure semantics, and invariants. An
  implementation MUST NOT require stronger preconditions or provide weaker
  guarantees than its port. Shared contract tests SHOULD cover multiple
  implementations when more than one exists.
- **Interface Segregation Principle:** a port MUST be owned by its consumer and
  expose only the operations that consumer needs. Split resolved, aliased,
  read, write, verification, and operational capabilities when they change or
  are consumed independently.
- **Dependency Inversion Principle:** domain and reusable orchestration MUST
  depend on inward-facing abstractions. Filesystem, Gradle, network, TeamCity,
  Figma, Android, and product-specific implementations depend on those ports
  and are selected only by a composition root.

Module independence does not mean that Gradle scripts contain no module
coordinates. It means source behavior does not know consumer products or
sibling implementations, dependencies point toward stable APIs, and concrete
adapter wiring is confined to an explicit composition root.

## Temporary Artifact Ownership

- Code that creates a temporary file or directory MUST own its lifecycle and
  remove it after its final consumer finishes.
- A producer MUST clean partially written temporary artifacts when it fails.
- An artifact that intentionally crosses process or operational phases MAY
  survive its producer, but its final consumer or documented completion step
  MUST delete it.
- Repository-local temporary artifacts MUST live below `tmp/`, a module
  `build/` directory, or another explicitly ignored generated-output root.
- Tests MUST register temporary resources with a lifecycle-aware fixture or
  delete them from `finally`; successful assertions alone are not cleanup.
- Run `./gradlew cleanTemporaryArtifacts` after supervised repository work that
  creates root-level temporary or generated tooling artifacts.

Line count alone is not a SOLID rule. Review reasons to change, dependency
direction, contract size, substitutability, and extension points instead of
using arbitrary class-size thresholds.

## Verification

Run `./gradlew checkModuleBoundaries checkIncludedBuildVersions` after
dependency-boundary changes, `./gradlew check` before completion, and
`./gradlew cleanTemporaryArtifacts` after temporary outputs reach their final
consumer. Review each affected production type against all five SOLID
principles. Automated boundary checks support this review but do not replace
it.

## Sources

- `build.gradle.kts`
- `settings.gradle.kts`
- `docs/reference/project-structure.md`
- `repo/gradle-plugins/`
- `repo/dependency-catalog/catalog-api/`
- `repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/CheckModuleBoundariesTask.kt`
