---
title: Kotlin standard
type: standard
scope: product-modules
owner: android
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - gradle.properties
  - repo/gradle-plugins/android
  - repo/gradle-plugins/unit-test
---

# Kotlin Standard

## Source Design

- Code MUST use the formatter and compiler configuration supplied by the
  repository build. Do not introduce module-local formatting policy.
- Prefer immutable values and pure transformations. Mutable state MUST have a
  clear owner and lifecycle.
- Public names MUST express domain intent. Avoid generic names such as
  `Manager`, `Helper`, or `Utils` when a capability name is available.
- Production failures MUST use typed results or domain exceptions at module
  boundaries. Do not use `null` to hide an operational failure.
- Do not catch `Throwable`, cancellation exceptions, or broad exceptions
  without rethrowing cancellation and translating the remaining failure.

## Types And APIs

- Keep one primary public top-level type per file. Private helpers and tightly
  owned nested types may remain with their owner.
- Prefer sealed hierarchies for closed state or result sets.
- Default to `internal`; make an API public only for an identified consumer.
- Extension functions MUST remain close to the type or capability they extend.
  Unrelated catch-all extension files are prohibited.

## Coroutines

- Coroutines MUST use structured concurrency and an injected or caller-owned
  scope. `GlobalScope` is prohibited.
- Dispatchers MUST be supplied at infrastructure boundaries when deterministic
  tests require control.
- Flows exposed as state MUST have a single state owner. Callers should receive
  read-only `Flow` or `StateFlow` views.
- Cancellation MUST propagate across suspend boundaries.

## Verification

Run the focused module test task during implementation and `./gradlew check`
before merge.

## Sources

- `repo/gradle-plugins/android/`
- `repo/gradle-plugins/unit-test/`
