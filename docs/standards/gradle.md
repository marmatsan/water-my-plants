---
title: Gradle build standard
type: standard
scope: repository
owner: repository-tooling
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - build.gradle.kts
  - settings.gradle.kts
  - versions.properties
  - repo/gradle-plugins
  - repo/dependency-catalog
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/boundary/CheckIncludedBuildVersionsTask.kt
---

# Gradle Build Standard

## Purpose

Define how build scripts, convention plugins, catalogs, included builds, and
custom tasks express reusable repository build policy.

## Build Script Contract

- Every checked-in `build.gradle.kts` MUST declare
  `@file:Suppress("AvoidDuplicateDependencies")` as its first line. This
  suppresses the IDE inspection that reports version-catalog-backed
  dependencies as duplicate declarations even when Gradle resolves them
  correctly.
- Use a type-safe plugin catalog alias for external plugins declared in
  `build.gradle.kts`. A build whose `kotlin-dsl` classpath owns Kotlin MAY use
  the type-safe `kotlin("...")` accessor without a second plugin version
  request.
- Use type-safe `libs` accessors for external dependencies and Kotlin DSL
  accessors such as `` `maven-publish` `` for Gradle core plugins, which do not
  receive catalog aliases.
- Use type-safe project accessors for Gradle module dependencies.
- Apply repository convention plugins for shared Android, Compose, test, and
  documentation configuration. Keep a dependency in the consuming module when
  it is not required by every compatible convention-plugin consumer.

## Included-Build Ownership

- Each autonomous included build owns the repositories, versions, catalogs,
  compile dependencies, tests, and publication identity needed to build it
  independently.
- Each multi-project included build MUST centralize dependency repositories in
  `settings.gradle.kts` `dependencyResolutionManagement` and enforce
  `RepositoriesMode.FAIL_ON_PROJECT_REPOS`. Publication repositories remain in
  the owning publishing configuration.
- Configuration shared by every compatible subproject in one included build,
  such as JUnit Platform activation, sources JARs, common Dokka metadata, or a
  staging publication repository, SHOULD be declared once in the included-build
  root and activated lazily with `withPlugin`.
- Keep configuration module-local when root preloading would put an
  incompatible plugin version on a shared classpath, including a build that
  mixes versioned Kotlin JVM aliases with Gradle's embedded `kotlin-dsl`
  plugin.
- Reusable included builds consume sibling capabilities through published
  coordinates and composition-root substitution. They MUST remain independent
  from sibling filesystem paths and scripts.

## Plugin And Catalog Contract

- `java-gradle-plugin` modules use the Gradle API and TestKit dependencies
  supplied by that plugin. Do not redeclare `gradleApi()` or
  `gradleTestKit()`.
- A repository-owned plugin with a stable marker and consumer-owned catalog
  entry uses a type-safe alias at its direct `build.gradle.kts` consumption
  point.
- When `pluginManagement.includeBuild` supplies a plugin implementation, the
  root build leaves that versioned alias out of `apply false` preloading. Gradle
  exposes the included implementation on the shared classpath with an unknown
  version and cannot validate a later versioned request.
- A repository-owned plugin MAY use a versionless literal `id(...)` only at a
  settings/bootstrap boundary or when a settings plugin has already placed the
  same implementation JAR on the build-script classpath with an unknown
  version. `com.marmatsan.projectConfig.settings` is the current bootstrap
  exception; project plugins with generated aliases use those aliases.
- Reserve literal plugin IDs in `settings.gradle.kts`
  `pluginManagement.plugins` for bootstrap plugins that must resolve before
  their generated catalog exists. Let generated project-plugin aliases carry
  their versions without a redundant default.
- Catalogs built with `dependencyCatalogTree` declare every top-level entry
  through `root`. A root contains exactly one path segment. Relative `library`
  and `plugin` declarations MAY use compact dotted paths because the DSL
  expands each segment into a node.
- Within a plugin root, collapse every maximal linear namespace chain into a
  dotted path and retain a nested block where one node owns multiple plugin
  descendants.

## Custom Tasks

- Every custom Gradle task declares its cache contract with `@CacheableTask`,
  `@DisableCachingByDefault`, or `@UntrackedTask`.
- A validation task with no reusable output SHOULD disable caching and state a
  concrete reason.
- Task inputs, outputs, services, and environment dependencies MUST be explicit
  enough for Gradle and CI to reproduce the behavior.

## Disallowed Alternatives

| Do not use | Use instead | Reason |
|------------|-------------|--------|
| Hard-coded external versions in module build scripts | The owning generated catalog and `versions.properties` | Keeps versions owned and verifiable. |
| Dependency-resolution repositories repeated in modules | Included-build `dependencyResolutionManagement` | Prevents repository drift. |
| Filesystem imports from a sibling reusable build | Published coordinates and composition-root substitution | Preserves independent distribution. |
| `afterEvaluate` for ordinary plugin configuration | Lazy typed Gradle APIs and `withPlugin` | Preserves configuration-cache-compatible ordering. |
| Product-specific branches in reusable plugins | Consumer configuration or a focused adapter | Keeps reusable builds product-neutral. |

## Exceptions

A bootstrap exception must identify why the type-safe catalog or plugin marker
does not yet exist at that phase. A reusable-build independence exception
requires an accepted ADR with a distribution and migration plan.

## Verification

- Run the focused included-build `check` while iterating.
- Run staged or standalone-consumer verification after changing publication or
  source-substitution behavior.
- Run `./gradlew checkIncludedBuildVersions` after version-ownership changes.
- Run `./gradlew checkModuleBoundaries` after composition or dependency changes.
- Run `./gradlew check` before completion.

## Sources

- `build.gradle.kts`
- `settings.gradle.kts`
- `versions.properties`
- `repo/gradle-plugins/`
- `repo/dependency-catalog/`
- `repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/boundary/CheckIncludedBuildVersionsTask.kt`
