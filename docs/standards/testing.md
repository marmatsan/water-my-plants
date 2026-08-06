---
title: Testing standard
type: standard
scope: repository
owner: quality
status: active
last-reviewed: 2026-08-06
review-cycle-days: 180
sources:
  - repo/gradle-plugins/unit-test
  - repo/unit-testing/unit-test-dsl
  - repo/gradle-plugins/bdd-test
  - repo/gradle-plugins/dokka-documentation
  - app/src/test/resources/features
  - repo/verification-platform/domain/src/test/resources/com/marmatsan/verificationPlatform/domain/bdd
  - repo/figma-documentation-sync/plugin/src/test/resources/com/marmatsan/figmaDocumentationSync/plugin/bdd
  - docs/standards/product-design.md
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
- Repository source verification runs from the root composite, which substitutes
  repository-owned test API coordinates. A reusable build executed outside that
  composite MUST resolve those coordinates from a staged or released repository;
  it MUST NOT include a sibling build by filesystem path.
- Kotlin tests that express Given-When-Then behavior MUST use the executable,
  typed `given { }.whenever { }.then { }` chain from `unit-test-dsl`. The
  `whenever` name avoids Kotlin's reserved `when` keyword.
- A `then` phase MAY consume only the action result or both the original
  fixture and action result. Assertions remain owned by Kotest; the DSL MUST
  remain independent of test engines and assertion libraries.
- Legacy Given-When-Then section comments are prohibited. Repository Kotlin
  style verification rejects them because comments cannot enforce phase order,
  type transfer, single evaluation, or exception propagation.
- Cucumber is reserved for executable business behavior and stable
  cross-boundary contracts. Feature files use domain language and must not
  encode implementation classes or technical inventories.
- `@wip` scenarios MUST NOT reach release-ready `main`.

## BDD And TDD Workflow

- User-visible behavior MUST begin with the OOUX objects, action consequences,
  and representative BDD examples required by the
  [product design standard](product-design.md). Figma owns this approved design
  intent before implementation; it does not claim that the behavior is shipped.
- Product and infrastructure behavior MUST be clarified with concrete examples
  before implementation when a change introduces or alters an observable rule.
- A `.feature` file SHOULD be created or updated before production code when
  the behavior is important enough to remain executable living documentation.
- An approved product-design example that expresses stable business behavior
  MUST be mirrored into the owning `.feature` file when implementation begins.
  The scenario uses the same object and action vocabulary as the OOUX contract.
- Implementation details SHOULD be developed in short TDD cycles with the
  smallest deterministic Kotlin test that drives the design.
- Gherkin MUST NOT replace focused unit, adapter-contract, integration, Compose
  UI, or Gradle functional tests.
- Exact assertions SHOULD NOT be duplicated between Cucumber scenarios and
  lower-level tests. Keep representative behavior in Gherkin and edge cases in
  the narrowest suitable Kotlin test.

## Living Documentation

- Gherkin describes what behavior the system guarantees and why it matters.
- OOUX and adjacent BDD examples in Figma describe approved future product
  intent. After implementation starts, repository Gherkin owns the executable
  representative behavior; changes to user-visible semantics are reconciled
  back into both artifacts.
- KDoc and generated Dokka describe the public and internal Kotlin API, parameters,
  invariants, and technical usage that implement those guarantees.
- KtLint owns mechanical Kotlin and KDoc formatting; it does not establish that
  API documentation is useful or complete. The KDoc and Dokka rules below own
  that semantic coverage.
- Every new or changed public or internal Kotlin declaration included in Dokka
  MUST have useful KDoc in the same change. Data models document property
  semantics; services, adapters, ports, strategies, and tasks document inputs,
  results, side effects, invariants, and relevant failures.
- Private implementation details and inherited behavior MUST NOT receive
  comments that only restate their signatures. Document the reason or contract
  when it is not evident from the code.
- Module README files describe ownership, boundaries, dependencies, and where
  to find the executable behavior and API reference.
- Dokka text MUST NOT restate scenarios line by line. It SHOULD link a documented
  entry point to its behavior contract when that relationship is useful.
- A module MAY adopt strict Dokka coverage incrementally. Once enabled, its
  Dokka source sets MUST include `Public` and `Internal` visibility, and its
  `check` task MUST report undocumented declarations and fail on Dokka warnings
  so documentation coverage cannot regress.
- An autonomous included build with strict modules MUST aggregate their `check`
  tasks in its root `check`. Full repository verification MUST aggregate those
  included-build checks so CI enforces the same documentation contract as a
  local module build.
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
module's equivalent task. Changes to public or internal Kotlin APIs MUST run
the owning module's `dokkaGenerate` task; modules with strict coverage include
it in `check` automatically.

## Sources

- `repo/gradle-plugins/unit-test/`
- `repo/unit-testing/unit-test-dsl/`
- `repo/gradle-plugins/bdd-test/`
- `repo/gradle-plugins/dokka-documentation/`
- `app/src/test/resources/features/`
- `repo/verification-platform/domain/src/test/resources/com/marmatsan/verificationPlatform/domain/bdd/`
- `repo/figma-documentation-sync/plugin/src/test/resources/com/marmatsan/figmaDocumentationSync/plugin/bdd/`
