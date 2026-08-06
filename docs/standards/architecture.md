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
  - docs/reference/project-structure.md
  - docs/standards/error-handling.md
  - repo/gradle-plugins
  - repo/unit-testing
  - repo/dependency-catalog/catalog-api
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/boundary/CheckModuleBoundariesTask.kt
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

## Gradle Build Scripts

- Every checked-in `build.gradle.kts` MUST declare
  `@file:Suppress("AvoidDuplicateDependencies")` as its first line. This
  suppresses the IDE inspection that reports version-catalog-backed
  dependencies as duplicate declarations even when Gradle resolves them
  correctly.
- External plugins declared in `build.gradle.kts` MUST use a type-safe plugin
  catalog alias. A build whose `kotlin-dsl` classpath owns Kotlin MAY use the
  type-safe `kotlin("...")` accessor without a second plugin version request.
  External dependencies MUST use type-safe `libs` accessors.
- Gradle core plugins MUST use their Kotlin DSL accessors, such as
  `` `maven-publish` ``, because Gradle does not generate catalog aliases for
  core plugins.
- Each multi-project included build MUST centralize dependency repositories in
  its `settings.gradle.kts` `dependencyResolutionManagement` block and enforce
  `RepositoriesMode.FAIL_ON_PROJECT_REPOS`. Module `build.gradle.kts` files
  MUST NOT repeat dependency-resolution repositories. Publication repositories
  remain owned by the included build's publishing configuration.
- Configuration shared by every compatible subproject in one included build,
  such as JUnit Platform activation, sources JARs, common Dokka metadata, or a
  staging publication repository, SHOULD be declared once in that included
  build's root `build.gradle.kts` and activated lazily with `withPlugin`.
  Configuration MUST remain module-local when root preloading would put an
  incompatible plugin version on a shared classpath, as can happen when a
  build mixes versioned Kotlin JVM aliases with Gradle's embedded
  `kotlin-dsl` plugin. Reusable included builds MUST NOT deduplicate by
  importing scripts or conventions from sibling builds through filesystem
  paths.
- `java-gradle-plugin` modules MUST use the Gradle API and TestKit dependencies
  supplied by that plugin. They MUST NOT redeclare `gradleApi()` or
  `gradleTestKit()` in their dependency blocks.
- Repository-owned plugins with a stable marker and a consumer-owned catalog
  entry MUST use a type-safe alias at their direct `build.gradle.kts`
  consumption point. When `pluginManagement.includeBuild` supplies the source
  implementation, the root build MUST NOT preload those versioned aliases with
  `apply false` before subprojects consume them: Gradle exposes an included
  plugin on the shared classpath with an unknown version and cannot validate a
  later versioned catalog request.
- A repository-owned plugin MAY use a versionless literal `id(...)` only at a
  settings/bootstrap boundary or when a settings plugin has already placed the
  same implementation JAR on the build-script classpath with an unknown
  version. Water My Plants therefore keeps
  `com.marmatsan.projectConfig.settings` in `settings.gradle.kts` as an
  explicit bootstrap exception. Root project plugins with generated catalog
  aliases, including `com.marmatsan.projectConfig.figma`, use those type-safe
  aliases.
- Literal plugin IDs in `settings.gradle.kts` `pluginManagement` declarations
  are reserved for settings/bootstrap plugins that must resolve before their
  generated catalog exists. Project-plugin defaults are redundant when the
  generated plugin alias already carries its version and MUST NOT be repeated
  in `pluginManagement.plugins`.
- Repository-owned catalogs built with `dependencyCatalogTree` MUST declare
  every top-level declaration through `root`; top-level `library` and `plugin`
  leaves are prohibited. A root MUST contain exactly one path segment. Relative
  `library` and `plugin` declarations MAY use dotted compact paths because the
  DSL expands every segment into a distinct node. Within a plugin root, dotted
  paths MUST collapse every maximal linear namespace chain; retain a nested
  block only where one node owns multiple plugin descendants.
- Every custom Gradle task MUST declare its cache contract explicitly with
  `@CacheableTask`, `@DisableCachingByDefault`, or `@UntrackedTask`. Validation
  tasks with no reusable output SHOULD disable caching with a concrete reason.

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

## Module Retirement

A migration that replaces, renames, merges, or removes a module is not complete
while the retired module path still exists in the checkout.

- Remove the retired module from Gradle composition, project dependencies,
  catalogs, plugin registrations, CI configuration, and current documentation.
- Preserve accepted ADR references when they are historical evidence; those
  references do not keep the retired path alive.
- Before deleting the directory, verify its resolved absolute path is the exact
  retired module inside the repository and inspect tracked, untracked, and
  ignored contents so user-owned work is not removed accidentally.
- Delete both tracked sources and ignored build state owned by the retired
  module, including its `.gradle/`, `.kotlin/`, and `build/` directories. The
  migration owner performs this cleanup because an inactive build no longer
  contributes its own `clean` tasks to the active Gradle graph.
- Verify that the retired path no longer exists, repository status is clean
  apart from the intended migration, and the replacement build passes its
  focused checks plus the repository boundary checks.

`cleanTemporaryArtifacts` removes temporary outputs owned by the active
repository graph. It MUST NOT infer and recursively delete unknown directories
under `repo/`; retirement cleanup always targets an explicitly verified path.

Line count alone is not a SOLID rule. Review reasons to change, dependency
direction, contract size, substitutability, and extension points instead of
using arbitrary class-size thresholds.

## Verification

Run `./gradlew checkModuleBoundaries checkIncludedBuildVersions` after
dependency-boundary changes, `./gradlew check` before completion, and
`./gradlew cleanTemporaryArtifacts` after temporary outputs reach their final
consumer. A module retirement additionally verifies that its former directory
is absent. Review each affected production type against all five SOLID
principles. Automated boundary checks support this review but do not replace
it.

## Sources

- `build.gradle.kts`
- `settings.gradle.kts`
- `docs/reference/project-structure.md`
- `docs/standards/error-handling.md`
- `repo/gradle-plugins/`
- `repo/unit-testing/`
- `repo/dependency-catalog/catalog-api/`
- `repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/boundary/CheckModuleBoundariesTask.kt`
