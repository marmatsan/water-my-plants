# BDD Test Gradle Convention Plugin

## Plugin

```kotlin
plugins {
    id("com.marmatsan.bddTest")
}
```

## Purpose

Configures executable BDD tests that run Cucumber on the JUnit Platform.

Use this convention when a project owns `.feature` files and Kotlin step
definitions that should run as part of the Gradle `test` lifecycle.

## Behavior

- Configures every `Test` task to use JUnit Platform.
- Sets Cucumber scenario naming to `long`.
- Generates Cucumber `pretty`, HTML, and JSON reports under
  `build/reports/cucumber/`.
- Forwards `cucumber.filter.tags` from the command line when present.
- Forwards `cucumber.features` from the command line when present.
- Adds Cucumber Java 8, Cucumber JUnit Platform, JUnit Platform Suite, and JUnit
  launcher test dependencies.

## Requirements

Step definitions should use `io.cucumber.java8.En` style definitions unless the
project intentionally changes Cucumber style.

The feature files and glue packages are owned by the consuming module.

## Verification

For changes to this plugin, prefer focused gradle-plugins verification:

```powershell
.\gradlew.bat -p repo/gradle-plugins :bdd-test:check
```
