---
title: Run executable BDD scenarios
type: guide
scope: app
owner: quality
status: active
last-reviewed: 2026-07-18
review-cycle-days: 180
sources:
  - app/src/test/resources/features
  - app/src/test/kotlin/com/marmatsan/water_my_plants/bdd
  - repo/gradle-plugins/bdd-test
---

# Run Executable BDD Scenarios

## Outcome

Execute all or a tagged subset of Cucumber JVM business scenarios and locate
their reports.

## Applicable Standards

Follow the [testing standard](../standards/testing.md). Cucumber scenarios
describe observable business behavior, not low-level implementation tests.

## Steps

Run the app BDD suite through its JVM unit-test task:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Run a tagged subset:

```powershell
.\gradlew.bat :app:testDebugUnitTest -Dcucumber.filter.tags="@smoke"
```

Feature files live under `app/src/test/resources/features/`. Step definitions
live under `app/src/test/kotlin/com/marmatsan/water_my_plants/bdd/steps/`, and
`RunCucumberTest.kt` owns the JUnit Platform suite configuration.

Use `Given` for context, `When` for the action, and `Then` for the observable
result. Prefer domain language and tags such as `@smoke` or `@regression`.
`@wip` scenarios must not reach release-ready `main`.

## Verification

Inspect:

```text
app/build/reports/cucumber/cucumber.html
app/build/reports/cucumber/cucumber.json
```

The Gradle task must succeed and report all selected scenarios.

## Related Documentation

- `app/docs/README.md`
- `repo/gradle-plugins/bdd-test/docs/README.md`
