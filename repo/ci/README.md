# Repository CI Planner

`repo/ci` owns the provider-neutral Kotlin contract that decides which
verification units apply to a committed repository change.

## Boundaries

- Domain models and classification do not depend on TeamCity, GitHub Actions,
  Gradle APIs, PowerShell, or Figma Design Sync.
- The Git adapter resolves the committed diff against `origin/main`.
- The TeamCity adapter converts the plan to escaped, allow-listed build
  parameters; it does not add provider concerns to the domain model.
- The Gradle plugin is the current composition root and registers
  `generateCiPlan` and `prepareTeamCityCiPlan` in the Water My Plants root
  build.
- CI providers consume allow-listed unit identifiers and Gradle task names;
  they must never execute arbitrary commands read from the JSON report.

The generated contract is documented in
[`docs/reference/ci-verification-plan.md`](../../docs/reference/ci-verification-plan.md).

## Verification

```powershell
.\gradlew.bat :ci:check
.\gradlew.bat generateCiPlan
.\gradlew.bat prepareTeamCityCiPlan
```

`generateCiPlan` writes the provider-neutral JSON contract.
`prepareTeamCityCiPlan` additionally emits TeamCity service messages for the
reviewed parameter allow-list. TeamCity uses those parameters to run visible
sequential steps while the repository has one build agent.
