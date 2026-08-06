# Verification Platform

`repo/verification-platform` owns the provider-neutral Kotlin contracts that
plan and execute repository verification. It decides which verification units
apply to a committed change and exposes the reviewed Gradle entry points used
locally and by CI adapters.

## Included Build Shape

This directory is an included Gradle build with three modules:

| Path      | Role                                                                                                                                           |
|-----------|------------------------------------------------------------------------------------------------------------------------------------------------|
| `domain/` | Provider-neutral plans, topology, module-impact rules, ports, and services. It has no Gradle, TeamCity, Git, filesystem, or HTTP dependencies. |
| `data/`   | Git, filesystem, Gradle-model, JSON, TeamCity REST, parameter, and service-message adapters that implement domain boundaries.                  |
| `plugin/` | Reusable Gradle tasks and the `com.marmatsan.verificationPlatform` configuration API.                                                          |

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
- The Gradle plugin registers generic planning, documentation, TeamCity, local
  version ownership, and module-boundary tasks. Its public DSL segregates
  repository policy into `ciPolicy`, `boundaries`, `typedErrorHandling`,
  `taskBindings`, and `teamCity` blocks so consumers configure only the
  capability they own.
- `VerificationPlatformPlugin` is the composition root and delegates task
  registration to internal registrars grouped by configurable, repository,
  CI, TeamCity, and lifecycle capabilities. A shared registration helper owns
  common task metadata while preserving lazy `TaskProvider` configuration.
  Adding a capability extends the relevant registrar instead of growing the
  composition root or exposing internal collaborators as public API.
- Verification Platform production code contains no Water My Plants module
  inventory or sibling build name. Water My Plants binds
  `checkKotlinStyle`, `checkDependencyCatalogArchitecture`, portable
  distribution consumers, and product policy in the root build.
- `taskBindings` uses direct included-build task references when no invocation
  inputs are needed. Distribution checks that must compose multiple reusable
  builds use `isolatedGradleBuildTask` with an explicit build directory and
  project-property map owned by the consuming root; Verification Platform does
  not infer sibling paths or dependency coordinates. Each isolated invocation
  also receives a task-specific Gradle user home under the root build directory,
  preventing the child process from timing out on cache locks held by the
  orchestrating Gradle process.
- This included build resolves its own compile/test toolchain from
  `repo/verification-platform/versions.properties`; it does not read the
  Water My Plants product catalog registry.
- Its library bundle aliases follow the shared dependency-catalog contract and
  end in `Bundle`. Build scripts therefore consume accessors such as
  `libs.bundles.kotestBundle`, `libs.bundles.cucumberBundle`, and
  `libs.bundles.ktlintBundle`; do not reintroduce aliases that hide their bundle
  identity.
- Its generated `plugins` catalog owns the versions of Kotlin JVM, Kotlin
  Serialization, and Dokka. `pluginManagement.plugins` declares only the
  `com.marmatsan.dependencyCatalog.tree` settings bootstrap; repeating project
  plugin defaults there would duplicate the versions already carried by the
  type-safe aliases.
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

All three modules enforce strict public and internal API documentation. Their
`check` tasks generate Dokka, report undocumented declarations, and fail on
Dokka warnings. New public or internal models, services, adapters, ports,
tasks, properties, and methods therefore add or update useful KDoc in the same
change.

The included-build root owns the common Dokka visibility, source links,
failure policy, JUnit Platform setup, sources JARs, and staging repository.
Module build scripts retain only capability-specific test settings and Maven
publication identity.

`DocumentationValidator` is a coordinator rather than a rule container. It
classifies typed documents and delegates metadata, heading, repository-link,
and change-coverage validation to independently testable collaborators. New
typed-document rules extend `TypedDocumentationRule`; they do not add another
reason for the coordinator to change. The public coordinator remains at
`domain.service.DocumentationValidator`, while its focused collaborators live
under `domain.service.documentation`.

Active specification artifacts under
`specs/<three-digit-id>-<kebab-case-name>/` are typed documentation. The
classifier accepts only `spec.md`, `plan.md`, and `checklist.md`, and the
heading validator applies the required section contract for each file.
Repository skills use `.agents/skills/<kebab-case-name>/SKILL.md`; their
frontmatter must provide a matching `name` and a `description` without
unsupported fields. Documentation paths preserve dot-prefixed roots such as
`.agents/` while removing only an explicit leading `./` or `/`, so hidden
repository directories remain addressable by discovery, link, and coverage
validation.

## Verification

```powershell
.\gradlew.bat :verification-platform:check
.\gradlew.bat :verification-platform:dokkaGenerate
.\gradlew.bat checkGitWorkflow
.\gradlew.bat checkDocumentation
.\gradlew.bat checkKotlinStyle
.\gradlew.bat formatKotlinStyle
.\gradlew.bat checkRepositoryDiff
.\gradlew.bat checkTeamCityDsl
.\gradlew.bat checkDependencyCatalogArchitecture checkIncludedBuildVersions
.\gradlew.bat checkModuleBoundaries
.\gradlew.bat checkTypedResultUsage
.\gradlew.bat verifyPortableDistribution
.\gradlew.bat generateCiPlan
.\gradlew.bat generateCiTopologyPreview -PciAvailableAgents=3
.\gradlew.bat prepareTeamCityCiPlan
```

`checkTypedResultUsage` is a reusable syntax boundary. Its
`typedErrorHandling` DSL selects the accepted fully qualified `Result` type
and each `productionSourceScope`; Verification Platform itself contains no
Water My Plants module inventory. The root configures app and reusable `repo/`
production scopes, while tests and generated sources remain outside this
syntax check. The domain validator and TeamCity queue contract expose typed
results; `domain` therefore publishes kotlin-result as `api`, while adapters
that only implement or collapse the contract use `implementation`.

Each autonomous consumer owns version `2.3.1` in its local
`versions.properties`. The root `boundaries.alignedVersion` rule compares only
included builds that declare `kotlinResultLibraryVersion`, so a non-consumer
does not acquire an unused dependency merely to satisfy version alignment.
Review and the
[typed error handling standard](../../docs/standards/error-handling.md) remain
responsible for semantic error ownership and exception boundaries.

`checkKotlinStyle` is a Water My Plants task binding to the reusable KtLint
adapter. It runs the KtLint 1.8.0 standard rules and the
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
