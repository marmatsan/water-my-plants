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
- Adds the repository-owned typed unit-test DSL to the test classpath.

## Requirements

Kotlin tests that express Given-When-Then behavior use the executable
`given { }.whenever { }.then { }` chain. Assertions remain in Kotest; the DSL
only controls typed phase execution and data transfer.

Use Cucumber scenarios through `com.marmatsan.bddTest`, not this unit-test
convention.

The Water My Plants composition root resolves the convention plugin through
`pluginManagement.includeBuild` and substitutes the DSL library through a
regular `includeBuild` of `repo/gradle-plugins`. The second inclusion is
required because Gradle plugin resolution does not also provide composite
substitution for ordinary library coordinates.

Reusable included builds declare only the DSL coordinate in their local test
catalog. They do not include a sibling build: the repository composition root
performs source substitution, while a standalone consumer resolves a staged or
released DSL artifact.

## Verification

For changes to this plugin, prefer focused gradle-plugins verification:

```powershell
.\gradlew.bat -p repo/gradle-plugins :unit-test:check
```
