---
title: Testing standard
type: standard
scope: repository
owner: quality
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - repo/gradle-plugins/unit-test
  - repo/gradle-plugins/bdd-test
  - app/src/test/resources/features
---

# Testing Standard

## Test Ownership

- Domain behavior MUST have deterministic unit tests without Android, network,
  filesystem, clock, or dispatcher dependencies unless those are the subject
  of the test.
- Adapters MUST test translation at their boundary, including failures.
- UI tests SHOULD cover user-observable state and interaction rather than
  internal composable structure.
- A regression fix MUST add the smallest test that fails before the fix and
  passes after it.

## Tooling

- Kotlin unit and integration tests use Kotest and MockK through the repository
  convention plugins.
- Tests use explicit `GIVEN`, `WHEN`, and `THEN` sections.
- Cucumber is reserved for executable business behavior. Feature files use
  domain language and must not encode implementation paths or class names.
- `@wip` scenarios MUST NOT reach release-ready `main`.

## Reliability

- Tests MUST control clocks, randomness, dispatchers, and external IO when they
  affect the assertion.
- Arbitrary sleeps are prohibited. Await an observable condition with a bounded
  timeout.
- Mocks SHOULD represent external collaborators, not every class in the unit.
- Tests MUST be independent of execution order and developer-machine state.

## Verification

Run the smallest affected test task while iterating and `./gradlew check` before
merge. BDD changes additionally run `:app:testDebugUnitTest` or the owning
module's equivalent task.

## Sources

- `repo/gradle-plugins/unit-test/`
- `repo/gradle-plugins/bdd-test/`
- `app/src/test/resources/features/`
