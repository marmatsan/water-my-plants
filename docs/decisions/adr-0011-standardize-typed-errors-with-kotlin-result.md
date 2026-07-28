---
title: Standardize typed errors with kotlin-result
type: adr
scope: repository
owner: architecture
status: accepted
last-reviewed: 2026-07-28
review-cycle-days: 365
sources:
  - docs/standards/error-handling.md
  - repo/verification-platform/versions.properties
  - repo/verification-platform/settings.gradle.kts
  - repo/verification-platform/domain/build.gradle.kts
  - repo/verification-platform/domain/src/main/kotlin/com/marmatsan/verificationPlatform/domain/service/errorhandling/TypedResultUsageValidator.kt
  - repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/task/errorhandling/CheckTypedResultUsageTask.kt
  - repo/figma-documentation-sync/domain/src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/port/figma/FigmaNodeContentSource.kt
  - repo/figma-documentation-sync/teamcity-adapter/src/main/kotlin/com/marmatsan/figmaDocumentationSync/teamcityAdapter/TeamCityRunStarter.kt
---

# ADR-0011: Standardize Typed Errors With kotlin-result

## Context

Expected failures need a public, transversal contract that preserves their
domain type and composes without exception-driven control flow. A repository-
owned `Result` implementation would also require maintaining variance,
combinators, coroutine cancellation behavior, binary compatibility, and
documentation that an established library already provides.

One global error hierarchy would couple unrelated capabilities. The shared
contract therefore needs to standardize the success-or-error container while
leaving error vocabularies with the domain that owns them.

## Decision

- Water My Plants uses `com.github.michaelbull.result.Result<Value, Error>` as
  the standard contract for expected, recoverable failures.
- Verification Platform, Figma Documentation Sync, and Water My Plants Project
  Configuration are current repository-tooling consumers. Their autonomous
  builds pin `com.michael-bull.kotlin-result:kotlin-result` at version `2.3.1`;
  their validation, Figma-node, and TeamCity queue boundaries return the public
  typed result contract where the caller can recover.
- Every autonomous included build that later consumes the library owns its
  dependency and version in that build's local catalog and
  `versions.properties`; it does not read another build's catalog. Root
  verification aligns the key across the builds that declare it, while
  non-consumers omit it.
- The Water My Plants product catalog does not declare kotlin-result until an
  application production module consumes it. This preserves the executable
  rule that the Figma product tree contains only production dependencies.
- Each capability owns a focused sealed error hierarchy when it has multiple
  meaningful failure variants. The repository does not define a global
  `RootError`, `AppError`, or wrapper `Result` type.
- Production code uses the library's `Ok`, `Err`, and composition operations
  directly. It must not hide the selected contract behind a type alias or
  recreate the library API in a repository-owned result class.
- A module declares `kotlin-result` only when its code uses the contract. A
  reusable library exposes the dependency with `api` only when `Result`
  appears in its public ABI; otherwise it uses `implementation`.
- `kotlin-result-coroutines` is added at version `2.3.1` only by modules that
  use its coroutine operations. Suspend adapters that convert thrown failures
  use its cancellation-safe catching operation.
- Exceptions remain appropriate for programmer errors, violated preconditions,
  and unrecoverable operational failures. Gradle tasks still throw to report a
  failed build. Boundary adapters map recoverable infrastructure failures into
  consumer-owned domain errors before returning inward.
- Lifecycle state is modeled separately from the terminal success-or-error
  outcome. A capability may own states such as loading or idle without adding
  them to `Result` or to a repository-wide status hierarchy.

The detailed rules and review contract live in
[`../standards/error-handling.md`](../standards/error-handling.md).

## Consequences

- Callers receive typed, exhaustive failure contracts and may compose them
  with the library's stable operators instead of nested conditionals.
- Domain error types remain independent even though they share one result
  container.
- The project avoids maintaining a duplicate railway-oriented programming
  surface and can use the library's tested coroutine integration.
- Public APIs that expose `Result` intentionally expose the selected library
  as part of their binary and source contract.
- Version upgrades are local catalog changes in each consuming autonomous
  build. A product-catalog upgrade additionally requires Figma synchronization.
- Static verification rejects incompatible `Result` imports, aliases, and
  custom contracts in the configured production source scopes, including
  reusable modules under `repo/`. Code
  review remains responsible for deciding whether a failure is expected and
  whether its error hierarchy belongs to the correct capability.

## Alternatives

- Kotlin's standard `kotlin.Result` was rejected because its error channel is
  fixed to `Throwable` and cannot express a capability-owned typed error
  hierarchy.
- Arrow `Either` was rejected for now because the project does not otherwise
  require Arrow's broader functional-programming surface. This decision may be
  revisited if that larger capability becomes a real requirement.
- A custom sealed `Result<Value, Error>` was rejected because it duplicates a
  mature API and transfers compatibility, combinator, and coroutine semantics
  to this repository.
- Exceptions for every failure were rejected because expected domain outcomes
  would be implicit, non-exhaustive, and harder to compose.

## Supersession

None.
