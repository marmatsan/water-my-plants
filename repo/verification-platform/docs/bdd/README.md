# Repository CI BDD

## Source Of Truth

The `.feature` files are the executable source of truth for stable CI planning
behavior. They describe which verification is required for a repository change
and how that work can be assigned to available agents.

KDoc and Dokka document the Kotlin API that implements these guarantees. They
must explain technical usage without copying the scenarios.

## Current Executable Features

| Behavior contract | Feature | Primary Kotlin API |
|---|---|---|
| Trunk-based branch naming | `../../domain/src/test/resources/com/marmatsan/verificationPlatform/domain/bdd/git-workflow.feature` | `GitBranchNameValidator` |
| Verification selection and fail-closed behavior | `../../domain/src/test/resources/com/marmatsan/verificationPlatform/domain/bdd/ci-verification-plan.feature` | `CiPlanFactory` |
| Single-agent and future multi-agent topology | `../../domain/src/test/resources/com/marmatsan/verificationPlatform/domain/bdd/ci-execution-topology.feature` | `CiTopologyPlanner` |

Step definitions live under
`../../domain/src/test/kotlin/com/marmatsan/verificationPlatform/domain/bdd/`.

`repo/verification-platform` mirrors the shared Cucumber/JUnit Platform convention in its own
included-build settings so `.\gradlew.bat -p repo/verification-platform ...` remains a supported,
standalone verification path.

## Test Strategy

- Cucumber owns representative, readable behavior contracts.
- Kotest owns algorithms, edge cases, invalid inputs, and focused regression
  tests.
- Data adapters keep boundary and serialization contract tests in Kotlin.
- Gradle tasks keep functional tests when their generated files or task
  behavior become part of the public contract.

Do not duplicate every scenario as a unit test. A behavior may have several
lower-level tests when those tests cover distinct edge cases or implementation
invariants.

## Running The Scenarios

Run all CI domain tests from the repository root:

```powershell
.\gradlew.bat :verification-platform:domain:test
```

Run only CI plan scenarios:

```powershell
.\gradlew.bat :verification-platform:domain:test -Dcucumber.filter.tags="@ci-plan"
```

The HTML and JSON reports are generated under
`domain/build/reports/cucumber/`.

## Dokka Relationship

Generate all CI API documentation with:

```powershell
.\gradlew.bat :verification-platform:dokkaGenerate
```

The module landing pages live under `domain/docs/dokka/`, `data/docs/dokka/`,
and `plugin/docs/dokka/`. Generated HTML is derived build output and must not be
committed.

The three CI modules enforce strict Dokka coverage through their `check` tasks.
This prevents public Kotlin APIs from being introduced without KDoc while
keeping scenario behavior in Gherkin rather than duplicating it in API prose.
