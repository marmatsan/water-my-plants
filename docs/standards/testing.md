---
title: Testing standard
type: standard
scope: repository
owner: quality
status: active
last-reviewed: 2026-07-20
review-cycle-days: 180
sources:
  - repo/gradle-plugins/unit-test
  - repo/gradle-plugins/bdd-test
  - app/src/test/resources/features
  - repo/verification-platform/domain/src/test/resources/com/marmatsan/verificationPlatform/domain/bdd
  - repo/figma-documentation-sync/plugin/src/test/resources/com/marmatsan/figmaDocumentationSync/plugin/bdd
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

- Kotlin unit and integration tests use Kotest and MockK. Root Android modules
  receive them through repository convention plugins; autonomous included
  builds declare the equivalent test dependencies locally.
- Tests use explicit `GIVEN`, `WHEN`, and `THEN` sections.
- Cucumber is reserved for executable business behavior and stable
  cross-boundary contracts. Feature files use domain language and must not
  encode implementation classes or technical inventories.
- `@wip` scenarios MUST NOT reach release-ready `main`.

## BDD And TDD Workflow

- Product and infrastructure behavior MUST be clarified with concrete examples
  before implementation when a change introduces or alters an observable rule.
- A `.feature` file SHOULD be created or updated before production code when
  the behavior is important enough to remain executable living documentation.
- Implementation details SHOULD be developed in short TDD cycles with the
  smallest deterministic Kotlin test that drives the design.
- Gherkin MUST NOT replace focused unit, adapter-contract, integration, Compose
  UI, or Gradle functional tests.
- Exact assertions SHOULD NOT be duplicated between Cucumber scenarios and
  lower-level tests. Keep representative behavior in Gherkin and edge cases in
  the narrowest suitable Kotlin test.

## Living Documentation

- Gherkin describes what behavior the system guarantees and why it matters.
- KDoc and generated Dokka describe the public Kotlin API, parameters,
  invariants, and technical usage that implement those guarantees.
- Every new or changed public Kotlin declaration MUST have useful KDoc in the
  same change. Public data models document property semantics; services,
  adapters, and tasks document inputs, results, side effects, invariants, and
  relevant failures.
- Private implementation details and inherited behavior MUST NOT receive
  comments that only restate their signatures. Document the reason or contract
  when it is not evident from the code.
- Module README files describe ownership, boundaries, dependencies, and where
  to find the executable behavior and API reference.
- Dokka text MUST NOT restate scenarios line by line. It SHOULD link a public
  entry point to its behavior contract when that relationship is useful.
- A module MAY adopt strict Dokka coverage incrementally. Once enabled, its
  `check` task MUST report undocumented public declarations and fail on Dokka
  warnings so documentation coverage cannot regress.
- Generated Dokka HTML is a build artifact and MUST NOT be committed.

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
module's equivalent task. Changes to public Kotlin APIs MUST run the owning
module's `dokkaGenerate` task; modules with strict coverage include it in
`check` automatically.

## Sources

- `repo/gradle-plugins/unit-test/`
- `repo/gradle-plugins/bdd-test/`
- `app/src/test/resources/features/`
- `repo/verification-platform/domain/src/test/resources/com/marmatsan/verificationPlatform/domain/bdd/`
- `repo/figma-documentation-sync/plugin/src/test/resources/com/marmatsan/figmaDocumentationSync/plugin/bdd/`
