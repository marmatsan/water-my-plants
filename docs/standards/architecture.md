---
title: Product architecture standard
type: standard
scope: product-modules
owner: architecture
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - settings.gradle.kts
  - docs/reference/project-structure.md
  - repo/gradle-plugins
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

## Verification

Run `./gradlew check` after dependency-boundary changes. Review
`settings.gradle.kts` and affected `build.gradle.kts` files together with the
module documentation.

## Sources

- `settings.gradle.kts`
- `docs/reference/project-structure.md`
- `repo/gradle-plugins/`
