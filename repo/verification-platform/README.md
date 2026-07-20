# Verification Platform

`repo/verification-platform` owns the provider-neutral Kotlin contracts that
plan and execute repository verification. It decides which verification units
apply to a committed change and exposes the reviewed Gradle entry points used
locally and by CI adapters.

## Included Build Shape

This directory is an included Gradle build with three modules:

| Path | Role |
|------|------|
| `domain/` | Provider-neutral plans, topology, module-impact rules, ports, and services. It has no Gradle, TeamCity, Git, filesystem, or HTTP dependencies. |
| `data/` | Git, filesystem, Gradle-model, JSON, TeamCity REST, parameter, and service-message adapters that implement domain boundaries. |
| `plugin/` | Gradle verification tasks and the `com.marmatsan.verificationPlatform` composition root consumed by Water My Plants. |

The dependency direction is `plugin -> data -> domain`; `plugin` may also use
domain types while composing tasks. The included build keeps a root `check`
aggregator so existing consumers do not need to know its internal projects.

## Boundaries

- Domain models and classification do not depend on TeamCity, GitHub Actions,
  Gradle APIs, PowerShell, or Figma Documentation Sync.
- Git adapters resolve the current branch and committed diff against
  `origin/main` without depending on a CI provider.
- The Gradle adapter snapshots executable root-project modules and declared
  project dependencies after project evaluation. The domain computes changed
  modules and transitive reverse dependents without Gradle APIs.
- The TeamCity adapter converts the plan to escaped, allow-listed build
  parameters and validates every emitted Gradle task name; it does not add
  provider concerns to the domain model.
- The Gradle plugin is the current composition root and registers
  `generateCiPlan`, `checkGitWorkflow`, `checkDocumentation`,
  `checkKotlinStyle`, `formatKotlinStyle`,
  `checkRepositoryDiff`, `checkTeamCityDsl`, `generateCiTopologyPreview`,
  `prepareTeamCityCiPlan`, and `runTeamCityInfrastructureHealth` in the Water
  My Plants root build.
- CI providers consume allow-listed unit identifiers and Gradle task names;
  they must never execute arbitrary commands read from the JSON report.

The generated contract is documented in
[`docs/reference/ci-verification-plan.md`](../../docs/reference/ci-verification-plan.md).

## Executable Behavior And API Documentation

The domain BDD suite describes the stable verification-selection and
agent-topology behavior in language independent from Kotlin implementation
details. Its feature files, step definitions, and behavior-to-API map are
documented in [`docs/bdd/README.md`](docs/bdd/README.md).

Dokka complements those scenarios with generated Kotlin API documentation for
`domain`, `data`, and `plugin`. Gherkin remains the source of truth for what CI
guarantees; KDoc and Dokka explain the types and entry points that provide the
guarantee. Generated HTML remains under each module's `build/dokka/` directory
and is not committed.

All three modules enforce strict public-API documentation. Their `check` tasks
generate Dokka, report undocumented public declarations, and fail on Dokka
warnings. New public models, services, adapters, tasks, properties, and methods
therefore add or update KDoc in the same change.

## Verification

```powershell
.\gradlew.bat :verification-platform:domain:check :verification-platform:data:check :verification-platform:plugin:check
.\gradlew.bat :verification-platform:dokkaGenerate
.\gradlew.bat checkGitWorkflow
.\gradlew.bat checkDocumentation
.\gradlew.bat checkKotlinStyle
.\gradlew.bat formatKotlinStyle
.\gradlew.bat checkRepositoryDiff
.\gradlew.bat checkTeamCityDsl
.\gradlew.bat generateCiPlan
.\gradlew.bat generateCiTopologyPreview -PciAvailableAgents=3
.\gradlew.bat prepareTeamCityCiPlan
```

`checkKotlinStyle` runs the KtLint 1.8.0 standard rules and the
repository-owned argument rule over every `.kt` and `.kts` file. It is wired
into the root `check` lifecycle so future source files are checked locally and
in CI. The root `.editorconfig` is the executable configuration source.

The repository rule requires vertical declaration parameters, vertical calls
with multiple arguments, and vertical named arguments even when used alone.
Short function-type and lambda signatures may remain inline within the
120-character repository line limit. For `.kt` files, it also resolves
unambiguous functions and constructors declared in the same source file and
requires every supported argument to use its Kotlin parameter name.
`formatKotlinStyle` applies all autocorrectable standard and repository rules;
run it twice and require the second run to change zero files. Java APIs,
function values, individual `vararg` elements, receiver or cross-file calls,
and Kotlin Script DSL APIs remain positional when the compiler rejects names;
compiler validation and review cover cases that require semantic resolution.
Non-autocorrectable findings still fail `checkKotlinStyle` and require a source
change.

The standard `function-signature` rule is disabled because the repository rule
owns declaration layout; the standard `indent` rule owns exact indentation.
This prevents competing autocorrections. KtLint remains a mechanical style
gate. KDoc and strict Dokka coverage independently own public API meaning and
documentation completeness.

The programmatic KtLint engine must load `.editorconfig` through
`EditorConfigDefaults` with the property types exposed by all active rule
providers. Do not add numeric signature-threshold properties as raw defaults:
KtLint 1.8 can otherwise expose their values as strings and fail when a
standard rule reads them as integers. Changes to rule ownership or typed
configuration require a formatting regression test plus two consecutive
`formatKotlinStyle` runs; the second run must report zero changed files.

`checkTeamCityDsl` also rejects generated Pipeline YAML that encodes
`commit-status-publisher` as a job feature. Its YAML schema does not allow that
feature type. The task also verifies that the generated classic `CI Gate` is a
composite build with the only CI VCS trigger, a snapshot dependency on the
modern `CI` Pipeline, and the versioned `TeamCity CI` status publisher.
`checkGitWorkflow` validates local symbolic `HEAD` or TeamCity's logical branch
name against the trunk-based branch contract. Provider-managed pull request
refs are accepted because TeamCity also validates the corresponding source
branch build.
`generateCiPlan` is deliberately untracked and always writes the
provider-neutral JSON contract from the current committed `HEAD`.
`generateCiTopologyPreview` projects its required units into agent lanes under
`build/reports/ci/ci-topology-preview.json`. The output is explicitly
`preview-only`: no TeamCity setting consumes it. One agent produces the current
single `verify` lane; two agents preview supplemental and Gradle lanes; three or
more agents additionally separate repository and tooling work before one
authoritative `ci-gate` lane.
`prepareTeamCityCiPlan` additionally emits TeamCity service messages for the
reviewed parameter allow-list. `ci.plan.gradleTasks` contains the ordered,
de-duplicated task names for every required verification unit. TeamCity invokes
that list once while the repository has one build agent; it does not implement
documentation, diff, DSL, or module-selection policy. Safe module-only changes
select affected module `check` tasks plus `checkFigmaCatalogUsage`; invalid
graphs, unknown paths, and tooling changes retain root `check`.

`runTeamCityInfrastructureHealth` is the Kotlin queueing boundary used by the
Windows startup adapter. It accepts HTTPS TeamCity origins or the local HTTP
loopback origin, never follows redirects with the Bearer token, and validates
the build type plus branch before issuing the REST request. SecretStore access
and Windows Task Scheduler remain thin PowerShell adapters outside this module.
