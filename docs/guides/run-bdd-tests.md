---
title: Run executable BDD scenarios
type: guide
scope: repository
owner: quality
status: active
last-reviewed: 2026-07-20
review-cycle-days: 180
sources:
  - app/src/test/resources/features
  - repo/verification-platform/domain/src/test/resources/com/marmatsan/verificationPlatform/domain/bdd
  - repo/figma-documentation-sync/plugin/src/test/resources/com/marmatsan/figmaDocumentationSync/plugin/bdd
  - app/src/test/kotlin/com/marmatsan/water_my_plants/bdd
  - repo/gradle-plugins/bdd-test
---

# Run Executable BDD Scenarios

## Outcome

Execute all or a tagged subset of the repository's Cucumber JVM behavior
contracts and locate their reports.

## Applicable Standards

Follow the [testing standard](../standards/testing.md). Cucumber scenarios
describe observable product behavior or stable cross-boundary contracts, not
low-level implementation tests.

## Steps

Run the app BDD suite through its JVM unit-test task:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Run the provider-neutral CI planning behavior:

```powershell
.\gradlew.bat :verification-platform:domain:test
```

Run the Figma Documentation Sync behavior:

```powershell
.\gradlew.bat :figma-documentation-sync:plugin:test
```

Run a tagged subset:

```powershell
.\gradlew.bat :verification-platform:domain:test -Dcucumber.filter.tags="@ci-plan"
```

Each owning module keeps its feature files under `src/test/resources/` and its
step definitions under `src/test/kotlin/`. `RunCucumberTest.kt` owns the JUnit
Platform suite configuration for that module.

Use `Given` for context, `When` for the action, and `Then` for the observable
result. Prefer domain language and tags such as `@smoke` or `@regression`.
`@wip` scenarios must not reach release-ready `main`.

## Verification

Inspect the report for the owning module:

```text
app/build/reports/cucumber/cucumber.html
app/build/reports/cucumber/cucumber.json
repo/verification-platform/domain/build/reports/cucumber/cucumber.html
repo/verification-platform/domain/build/reports/cucumber/cucumber.json
repo/figma-documentation-sync/plugin/build/reports/cucumber/cucumber.html
repo/figma-documentation-sync/plugin/build/reports/cucumber/cucumber.json
```

The Gradle task must succeed and report all selected scenarios.

## Related Documentation

- `app/docs/README.md`
- `repo/verification-platform/docs/bdd/README.md`
- `repo/figma-documentation-sync/docs/bdd/README.md`
- `repo/gradle-plugins/bdd-test/docs/README.md`
