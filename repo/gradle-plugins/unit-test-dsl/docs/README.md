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

## Composite Build Consumption

The `com.marmatsan.unitTest` convention adds the versionless composite
coordinate `com.marmatsan.repo:unit-test-dsl` to test dependencies. A
consumer repository that sources the convention plugin from an included build
must include `repo/gradle-plugins` twice for two distinct Gradle boundaries:

```kotlin
pluginManagement {
    includeBuild("repo/gradle-plugins")
}

includeBuild("repo/gradle-plugins")
```

The first inclusion resolves plugin IDs. The regular inclusion substitutes the
DSL library coordinate. It belongs only in a repository composition root;
reusable included builds depend on the versioned API coordinate and do not
include sibling builds by filesystem path. A standalone reusable build therefore
uses a staged or released DSL artifact instead of source substitution.

## Verification

```powershell
.\gradlew.bat -p repo/gradle-plugins :unit-test-dsl:check
```

The check includes the strict Dokka contract for public and internal API.
