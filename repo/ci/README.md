# Repository CI Planner

`repo/ci` owns the provider-neutral Kotlin contract that decides which
verification units apply to a committed repository change.

## Boundaries

- Domain models and classification do not depend on TeamCity, GitHub Actions,
  Gradle APIs, PowerShell, or Figma Design Sync.
- The Git adapter resolves the committed diff against `origin/main`.
- The Gradle adapter snapshots executable root-project modules and declared
  project dependencies after project evaluation. The domain computes changed
  modules and transitive reverse dependents without Gradle APIs.
- The TeamCity adapter converts the plan to escaped, allow-listed build
  parameters and validates every emitted Gradle task name; it does not add
  provider concerns to the domain model.
- The Gradle plugin is the current composition root and registers
  `generateCiPlan`, `prepareTeamCityCiPlan`, and
  `runTeamCityInfrastructureHealth` in the Water My Plants root build.
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
sequential steps while the repository has one build agent. Safe module-only
changes select affected module `check` tasks plus `checkFigmaCatalogUsage`;
invalid graphs, unknown paths, and tooling changes retain root `check`.

`runTeamCityInfrastructureHealth` is the Kotlin queueing boundary used by the
Windows startup adapter. It accepts HTTPS TeamCity origins or the local HTTP
loopback origin, never follows redirects with the Bearer token, and validates
the build type plus branch before issuing the REST request. SecretStore access
and Windows Task Scheduler remain thin PowerShell adapters outside this module.
