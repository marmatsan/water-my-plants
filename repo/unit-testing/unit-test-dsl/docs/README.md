# Typed Unit Test DSL

## Purpose

Provides the repository-owned, assertion-framework-agnostic Kotlin API used to
express unit-test scenarios as a typed `given { }.whenever { }.then { }` chain.

## Boundary

- The module depends only on Kotlin in production.
- Kotest remains the repository assertion and test engine, but is not exposed by
  the DSL API.
- Each phase evaluates exactly once and transfers its typed result to the next
  phase.
- Exceptions are propagated without wrapping.
- `whenever` is used because `when` is a Kotlin keyword.
- The module is reusable from any Kotlin test suite; it does not depend on
  Water My Plants production modules or catalogs.

Tests that need the fixture and the action result select the contextual `then`
overload:

```kotlin
given {
    Fixture()
}.whenever { fixture ->
    subject.execute(fixture)
}.then { fixture, result ->
    result shouldBe fixture.expected
}
```

## Consumption

The `com.marmatsan.unitTest` convention adds the versionless composite
coordinate `com.marmatsan.repo:unit-test-dsl` to test dependencies. A
consumer repository that sources the convention plugin from an included build
composes the library and plugin builds at its root:

```kotlin
pluginManagement {
    includeBuild("repo/gradle-plugins")
}

includeBuild("repo/unit-testing")
```

The first inclusion resolves plugin IDs. The regular inclusion substitutes the
DSL library coordinate from its own autonomous build. These inclusions belong
only in a repository composition root;
reusable included builds depend on the versioned API coordinate and do not
include sibling builds by filesystem path. A standalone reusable build therefore
uses a staged or released DSL artifact instead of source substitution.

External consumers resolve `com.marmatsan.repo:unit-test-dsl:<version>` from
the configured Maven repository. `verifyStagedPublication` proves that path
without any source composite.

## Verification

```powershell
.\gradlew.bat -p repo/unit-testing check verifyStagedPublication
```

The check includes the strict Dokka contract for public and internal API.
