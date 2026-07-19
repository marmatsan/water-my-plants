# Repository CI Planner

`repo/ci` owns the provider-neutral Kotlin contract that decides which
verification units apply to a committed repository change.

## Included Build Shape

This directory is an included Gradle build with three modules:

| Path | Role |
|------|------|
| `domain/` | Provider-neutral plans, topology, module-impact rules, ports, and services. It has no Gradle, TeamCity, Git, filesystem, or HTTP dependencies. |
| `data/` | Git, Gradle-model, JSON, TeamCity REST, parameter, and service-message adapters that implement domain boundaries. |
| `plugin/` | Gradle tasks and the `com.marmatsan.ci` composition root consumed by Water My Plants. |

The dependency direction is `plugin -> data -> domain`; `plugin` may also use
domain types while composing tasks. The included build keeps a root `check`
aggregator so existing consumers do not need to know its internal projects.

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
  `generateCiPlan`, `generateCiTopologyPreview`, `prepareTeamCityCiPlan`, and
  `runTeamCityInfrastructureHealth` in the Water My Plants root build.
- CI providers consume allow-listed unit identifiers and Gradle task names;
  they must never execute arbitrary commands read from the JSON report.

The generated contract is documented in
[`docs/reference/ci-verification-plan.md`](../../docs/reference/ci-verification-plan.md).

## Verification

```powershell
.\gradlew.bat :ci:domain:check :ci:data:check :ci:plugin:check
.\gradlew.bat generateCiPlan
.\gradlew.bat generateCiTopologyPreview -PciAvailableAgents=3
.\gradlew.bat prepareTeamCityCiPlan
```

`generateCiPlan` writes the provider-neutral JSON contract.
`generateCiTopologyPreview` projects its required units into agent lanes under
`build/reports/ci/ci-topology-preview.json`. The output is explicitly
`preview-only`: no TeamCity setting consumes it. One agent produces the current
single `verify` lane; two agents preview supplemental and Gradle lanes; three or
more agents additionally separate repository and tooling work before one
authoritative `ci-gate` lane.
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
