# Water My Plants

## Project documentation

- [Project structure](docs/project-structure.md)
- [Main branch protection](docs/ci/main-branch-protection.md)

## BDD with Cucumber

This project uses Cucumber JVM for executable BDD scenarios in local JVM tests.

Run the BDD scenarios with the app unit test task:

```bash
./gradlew :app:testDebugUnitTest
```

Run a tagged subset:

```bash
./gradlew :app:testDebugUnitTest -Dcucumber.filter.tags="@smoke"
```

Cucumber feature files live under:

```text
app/src/test/resources/features/
```

Step definitions live under:

```text
app/src/test/kotlin/com/marmatsan/water_my_plants/bdd/steps/
```

The Cucumber JUnit Platform suite is:

```text
app/src/test/kotlin/com/marmatsan/water_my_plants/bdd/RunCucumberTest.kt
```

Generated reports:

```text
app/build/reports/cucumber/cucumber.html
app/build/reports/cucumber/cucumber.json
```

CI should execute `:app:testDebugUnitTest` and publish `app/build/reports/cucumber/` as build artifacts.

### BDD conventions

- Use Cucumber for executable business scenarios, not for low-level implementation tests.
- Keep scenarios focused on observable behavior.
- Write `Given` steps for context, `When` steps for the action, and `Then` steps for the expected outcome.
- Prefer domain language in feature files.
- Keep step definitions under the module BDD package so the suite glue can discover them.
- Tag scenarios that should run in specific suites, for example `@smoke`, `@regression`, or `@wip`.
- Do not commit `@wip` scenarios as part of release-ready work.
