# Unit Test Gradle Convention Plugin

## Plugin

```kotlin
plugins {
    id("com.marmatsan.unitTest")
}
```

## Purpose

Configures shared JVM unit-test defaults for modules that run regular automated
tests.

Use this convention when a module should use Kotest, MockK, and the JUnit
Platform for unit tests.

## Behavior

- Configures every `Test` task to use JUnit Platform.
- Enables dynamic agent loading for test JVMs.
- Adds Kotest runner and assertion dependencies.
- Adds MockK.
- Adds the JUnit Platform launcher runtime dependency.

## Requirements

Tests should use explicit `GIVEN`, `WHEN`, and `THEN` comment sections according
to the build-logic testing conventions.

Use Cucumber scenarios through `com.marmatsan.bddTest`, not this unit-test
convention.

## Verification

For changes to this plugin, prefer focused build-logic verification:

```powershell
.\gradlew.bat -p build-logic :unitTest:check
```
