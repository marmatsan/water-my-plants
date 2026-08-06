---
title: Product architecture standard
type: standard
scope: repository
owner: architecture
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - build.gradle.kts
  - settings.gradle.kts
  - docs/decisions/adr-0015-use-room-and-proto-datastore-for-product-persistence.md
  - docs/reference/project-structure.md
  - docs/standards/error-handling.md
  - repo/gradle-plugins
  - repo/unit-testing
  - repo/dependency-catalog/catalog-api
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/boundary/CheckModuleBoundariesTask.kt
---

# Product Architecture Standard

## Purpose

Define dependency direction and ownership for production Android code while
keeping selected infrastructure behind explicit adapter boundaries.

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

## Feature Slices

Organize product behavior by capability before introducing technical layers.
A feature MAY expose `ui`, `domain`, and `data` modules when each module has a
durable consumer or dependency boundary. When those boundaries are not yet
demonstrated, keep the capability cohesive in one module and separate its
responsibilities with meaningful packages.

When a feature has all three boundaries, use this dependency direction:

```text
:app -> :feature:ui -> :feature:domain
:app -> :feature:data -> :feature:domain
```

`:app` selects the data adapter and assembles it with the feature UI. The domain
module MUST NOT depend on the UI, the data adapter, `:app`, or Android framework
types. A layer name alone does not justify a module; add the module only when its
independent API, consumers, or verification boundary makes the split durable.

## Dependency Direction

New domain behavior MUST depend on abstractions it owns, not concrete Android,
network, database, or Gradle implementations. Infrastructure adapters may
depend inward on those abstractions. Android UI may depend on domain-facing
interfaces but domain code MUST remain free of Android framework types.

Cross-layer types MUST be explicit. Transport DTOs, persistence entities, and
Compose state are not domain models and MUST NOT leak across their boundary.

## Ports, Adapters, And Boundary Models

A capability that needs external state or behavior MUST define the narrow port
required by its inward-facing consumer. A filesystem, database, network, or
Android implementation is an adapter that depends on that port and translates
its vendor contract at the boundary.

Adapters MUST map transport DTOs and persistence entities explicitly to domain
models. Mapping MUST preserve domain invariants and translate malformed or
unsupported external values into the consumer-owned typed error contract. Add
an additional data-source or DAO interface only when it isolates an independently
consumed capability, failure contract, or variation point; a second interface
that merely mirrors and forwards every repository operation is prohibited.

Product persistence follows
[ADR-0015](../decisions/adr-0015-use-room-and-proto-datastore-for-product-persistence.md):
Room owns relational product data and Proto DataStore owns small typed settings.
Do not use DataStore for relational entity collections or Room for a single
settings document.

Every production schema MUST have explicit ownership, evolution, migration,
failure handling, and recovery. A production migration MUST preserve user data.
Destructive reset is permitted only for explicitly disposable development or
test state and MUST NOT be the fallback for an unknown production schema.
Room entities, DAOs, DataStore serializers, and generated Protocol Buffers
messages remain adapter types and MUST NOT cross the domain boundary.

## Application Services And Use Cases

Represent a business intention with a focused use case or application service
when it enforces a domain rule, coordinates ports, or gives a consumer a stable
operation such as creating, observing, or deleting a plant. Prefer one concrete
operation with explicit input, success, and error types.

A use case that only forwards one call without adding a consumer boundary,
policy, or orchestration SHOULD be replaced by the narrow port or focused
concrete behavior. Shared marker interfaces, abstract `UseCase` base classes,
and generic bundles of unrelated use cases are prohibited unless multiple real
consumers demonstrate a substitutable contract.

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

## Typed Errors

Expected recoverable failures follow the repository-wide
[`error-handling.md`](error-handling.md) standard. Capabilities own their error
hierarchies and use the standard `com.github.michaelbull.result.Result`
container; they do not depend on a global error root or a repository-owned
replacement. Lifecycle state remains a separate capability contract.

A reusable module that exposes `Result` in its public ABI MUST declare
`kotlin-result` with Gradle `api`; an adapter or composition module that only
implements or collapses the contract MUST use `implementation`. Each consuming
included build owns its type-safe alias and version locally. Root verification
MAY align that version key across declaring builds, but MUST NOT require a
non-consumer to add the dependency.

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

## Package Cohesion

Package identity is part of the architecture and is review-blocking for every
new or materially changed Kotlin source, including checked-in code emitted by
a generator.

- The directory below a Kotlin source root MUST match the declared package
  exactly. Scaffold or template package names MUST NOT reach reviewed code.
- A package MUST represent one cohesive capability and one related family of
  reasons to change. Broad buckets such as `plugin`, `domain.model`,
  `domain.service`, or `projectConfig` MUST be split by capability once they
  would mix independently changing concerns.
- A module or source-set root package is reserved for its public entry point,
  composition root, or types that genuinely coordinate the whole module.
  Models, services, tasks, adapters, and configuration types belong in a
  capability package.
- Tests MUST mirror the package of the behavior they verify, unless they belong
  to an explicitly named test-only capability.
- A Kotlin file SHOULD contain one primary top-level type, use that type's
  PascalCase name, and match the filename. Tool-required exceptions MUST be
  documented next to the generator or source set.
- Generators that emit checked-in Kotlin MUST receive or derive a meaningful
  capability package and MUST write to its matching directory. Generated build
  outputs remain under `build/` or another ignored generated-output root.
- Existing tracked types SHOULD be relocated as file moves so Git history
  remains attributable; package cleanup is not a reason to recreate a type.

Review both physical package correctness and semantic cohesion. A path can
match its declaration while still hiding unrelated responsibilities in an
overly broad namespace.

Line count alone is not a SOLID rule. Review reasons to change, dependency
direction, contract size, substitutability, and extension points instead of
using arbitrary class-size thresholds.

## Verification

Run `./gradlew checkModuleBoundaries` after dependency-boundary changes and
`./gradlew check` before completion. Review each affected production type
against all five SOLID principles. Automated boundary checks support this
review but do not replace it. Apply the [Gradle](gradle.md) and
[repository-hygiene](repository-hygiene.md) standards when the change also
affects build behavior, generated output, temporary artifacts, or module
retirement.

## Sources

- `build.gradle.kts`
- `settings.gradle.kts`
- `docs/decisions/adr-0015-use-room-and-proto-datastore-for-product-persistence.md`
- `docs/reference/project-structure.md`
- `docs/standards/error-handling.md`
- `repo/gradle-plugins/`
- `repo/unit-testing/`
- `repo/dependency-catalog/catalog-api/`
- `repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/boundary/CheckModuleBoundariesTask.kt`
- [Gradle build standard](gradle.md)
- [Repository hygiene standard](repository-hygiene.md)
