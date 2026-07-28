---
title: Typed error handling standard
type: standard
scope: repository
owner: architecture
status: active
last-reviewed: 2026-07-28
review-cycle-days: 180
sources:
  - docs/decisions/adr-0011-standardize-typed-errors-with-kotlin-result.md
  - repo/verification-platform/versions.properties
  - repo/verification-platform/settings.gradle.kts
  - repo/verification-platform/domain/build.gradle.kts
  - repo/verification-platform/domain/src/main/kotlin/com/marmatsan/verificationPlatform/domain/service/errorhandling/TypedResultUsageValidator.kt
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/errorhandling/CheckTypedResultUsageTask.kt
---

# Typed Error Handling Standard

## Purpose

Define the repository-wide contract for expected failures, exception
boundaries, lifecycle state, and use of `kotlin-result`.

## Rules

- Code that can complete with an expected, recoverable failure MUST expose
  `com.github.michaelbull.result.Result<Value, Error>` at the relevant domain
  boundary.
- A capability MUST own the error type consumed by its callers. Use a focused
  sealed interface or sealed class when exhaustive variants are valuable; do
  not create a global `RootError`, `AppError`, or catch-all domain error.
- Production code MUST use `Ok` and `Err` from `kotlin-result` directly. It
  MUST NOT declare a competing error-handling `Result`, import another
  `Result`, alias the standard `Result`, or wrap it in a second generic result
  abstraction.
- Transform success values with `map`, error values with `mapError`, sequence
  dependent operations with `andThen`, and collapse both tracks with `fold` or
  `mapBoth`. Use `onSuccess` and `onFailure` only for side effects; do not use
  observation callbacks as the primary transformation mechanism.
- Modules MUST declare `kotlin-result` only when they use it. A reusable module
  MUST use Gradle `api` when `Result` is present in its public ABI and
  `implementation` otherwise.
- Reusable included builds MUST keep any kotlin-result version in their own
  catalog and `versions.properties`. They MUST NOT reach into another build's
  catalog; version alignment is reviewed when the dependency is introduced or
  upgraded.
- The Water My Plants product catalog MUST add kotlin-result only with its first
  production consumer. An approved standard alone is not catalog usage and
  MUST NOT expand the production dependency tree published to Figma.
- Modules MUST add `kotlin-result-coroutines` only when they use its coroutine
  integration. Suspend adapters that catch and convert failures MUST use
  `runSuspendCatching` so coroutine cancellation is rethrown rather than
  converted into a domain error.
- Exceptions MUST represent programmer errors, violated preconditions, or
  failures that the current boundary cannot recover from. A Gradle verification
  task MUST throw when its contract fails so Gradle and CI receive the correct
  failure state.
- Infrastructure adapters SHOULD catch only the failures they can translate.
  They MUST map recoverable infrastructure details into consumer-owned errors
  and MUST NOT leak transport, persistence, Gradle, or vendor exception types
  into a domain API.
- Loading, idle, refreshing, or progress are lifecycle states, not `Result`
  errors. A capability MUST model them separately and SHOULD promote a status
  abstraction to a shared module only after multiple consumers demonstrate the
  same semantic contract.
- Repository extensions MAY add project-specific operations only when a real
  repeated use case is not already expressed clearly by kotlin-result. Such an
  extension MUST preserve library semantics, live in a capability package, and
  have focused tests and KDoc.

A typical boundary keeps its error vocabulary local:

```kotlin
sealed interface LoadPlantError {
    data object NotFound : LoadPlantError

    data object Unavailable : LoadPlantError
}

fun loadPlant(id: PlantId): Result<Plant, LoadPlantError>
```

## Exceptions

An exception to this standard requires an accepted ADR that identifies the
consumer contract, failure semantics, dependency impact, and migration path.
A library whose own API returns another result type may be adapted at the
infrastructure boundary; that external type must not become the inward-facing
domain contract.

## Verification

- Run `./gradlew checkTypedResultUsage` after adding or changing production
  error-handling contracts. The root `check` lifecycle and TeamCity CI already
  depend on this task.
- Run the affected module tests, `./gradlew checkDocumentation`, and
  `./gradlew check` before completion.
- Review must verify error ownership, exception-to-error translation,
  cancellation behavior, Gradle `api` versus `implementation`, and all five
  SOLID principles. The static task detects incompatible syntax; it does not
  replace semantic review.

## Sources

- [`ADR-0011`](../decisions/adr-0011-standardize-typed-errors-with-kotlin-result.md)
- `repo/verification-platform/versions.properties`
- `repo/verification-platform/settings.gradle.kts`
- `repo/verification-platform/domain/build.gradle.kts`
- `repo/verification-platform/domain/src/main/kotlin/com/marmatsan/verificationPlatform/domain/service/errorhandling/TypedResultUsageValidator.kt`
- `repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/errorhandling/CheckTypedResultUsageTask.kt`
