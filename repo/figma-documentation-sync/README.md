
# figma-documentation-sync

`figma-documentation-sync` is a reusable bridge between a Gradle project model and a
Figma documentation model. It is not application production code:
it generates a deterministic `design-model.json`, validates repository catalog
rules, and supports the Figma visual sync workflow used by CI.

## What It Does

The module answers one question: does the Figma dependency documentation still
represent the current repository?

It does that in two stages:

1. Gradle code reads repository sources and generates
   `build/reports/figma-sync/design-model.json`.
2. Figma sync tooling consumes that model, updates visual sections, and writes
   Figma shared plugin metadata containing `modelHash`, `writerHash`,
   `transportHash`, per-target model fingerprints, and per-scope writer
   fingerprints.

The generated hash is based on `schemaVersion` and stable `content`. Traceability
fields such as branch, Git SHA, and generation timestamp are written to the
model, but they do not affect `modelHash`.

## Included Build Shape

This directory is an included Gradle build with three portable Kotlin modules,
one CI-system adapter, and one portable TypeScript Figma
Plugin API boundary:

| Path | Role |
|------|------|
| `domain/` | Pure model, port, and visual-planning definitions for versions, catalogs, modules, CI, and writer execution. |
| `data/` | File, Gradle, catalog-port, JSON, MCP, and Figma API adapters that implement domain ports. |
| `plugin/` | Gradle plugin, tasks, checkers, dependency injection bindings, and model generation orchestration. |
| `teamcity-adapter/` | Optional Kotlin adapter that translates generated TeamCity YAML/XML and provides typed TeamCity CLI operations. |
| `tools/` | Thin TypeScript Figma Plugin API boundary plus optional preview packaging and adapter tests. |
| `docs/` | Runbooks, BDD notes, UML diagrams, and visual contract documentation. |

Dependency direction is intentional:

```text
consumer adapter -> plugin -> data -> domain
consumer adapter -> teamcity-adapter -> data -> domain
tools <- transient writer-project-config.json <- consumer adapter
```

`domain` must stay independent from Gradle, files, Figma clients, and plugin
composition. `plugin` wires portable Gradle tasks to domain ports through
`data`; `teamcity-adapter` owns the vendor-specific parser; a consumer adapter
applies the plugin and selects one repository's concrete catalog port, layout,
Figma document, and CI adapter.

Gradle catalog usage follows segregated read contracts. A shared source scanner
and parser own syntax discovery, while main-build, included-build, and
convention-plugin readers expose only the queries required by their consumers.
Adding another consumer scope composes those reusable parsing services instead
of expanding one repository-wide reader.

## API Documentation

The `domain`, `data`, `plugin`, and `teamcity-adapter` modules generate Dokka
HTML for public and internal Kotlin declarations. Every module `check` reports
undocumented declarations and fails on Dokka warnings, so ports, models,
adapters, tasks, and composition boundaries must keep useful KDoc current.
Generated HTML remains under each module's `build/dokka/` directory and is not
committed.

Generate the complete included-build reference with:

```powershell
.\gradlew.bat -p repo\figma-documentation-sync dokkaGenerate
```

Pure CI section planning is Kotlin-owned. `CiVisualPlanner` is a composition
service: independent `CiVisualSectionPlanner` strategies own each section and
produce a portable plan without adding section-specific branches to the
orchestrator. `CiVisualPlanJson` projects complete or target-scoped JSON, and the
`generateFigmaCiVisualPlan` Gradle task exposes that contract to consumers and
preview tooling. TypeScript requires the generated plan and only interacts with
Figma nodes. Kotlin
planner sources participate in target-scoped writer fingerprints, so a CI-only
planning change does not invalidate catalog or version targets.

Kotlin/JS was evaluated for measured runtime geometry and is intentionally not
part of the production toolchain. The Figma adapter remains TypeScript while
pure planning continues moving to Kotlin/JVM. See
[ADR-0003](../../docs/decisions/adr-0003-keep-figma-runtime-boundary-in-typescript.md)
for the measured trade-off and reconsideration criteria.

## Inputs

The model is generated from repository source files, not from Figma:

| Input | Purpose |
|-------|---------|
| `repo/water-my-plants-project-config/versions.properties` | Ordered product version sections rendered in Figma and validated by CI. |
| `repo/water-my-plants-project-config/catalog/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/catalog/LibraryTrees.kt` | Source of truth for the production library catalog tree. |
| `repo/water-my-plants-project-config/catalog/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/catalog/PluginTrees.kt` | Source of truth for the production plugin catalog tree. |
| Root `settings.gradle.kts` | Main project module discovery. |
| Included-build `settings.gradle.kts` files | Included-build module discovery and optional usage metadata. Their local tool catalogs are not Water My Plants visual targets. |
| Gradle build files | Module dependency edges and applied plugin usage. |
| `docs/ci/external-topology.yaml` | Versioned external systems, access boundaries, and directed connections. |
| `docs/ci/windows-runtime.yaml` | Versioned Windows services, startup modes, and service identities for the local CI runtime. |
| `.teamcity/target/generated-configs` | Water My Plants effective CI configuration. `TeamCityCiConfigurationProvider` translates its generated YAML/XML into the portable pipeline model. |
| `repo/water-my-plants-project-config/water-my-plants/change-impact-policy.json` | Water My Plants path policy used to classify whether a change can affect the model or visual writer. |

The Water My Plants included-build sources are configured by the
`com.marmatsan.waterMyPlantsProjectConfig` project adapter:

| Included build | Model name | Purpose |
|----------------|------------|---------|
| `repo/dependency-catalog` | `dependencyCatalog` | Describes the reusable catalog API, core, and Gradle adapter; it does not publish a product catalog tree. |
| `repo/figma-documentation-sync` | `figmaDocumentationSync` | Describes tooling modules and dependency edges; it does not publish a catalog tree to Figma. |
| `repo/gradle-plugins` | `gradlePlugins` | Describes convention-plugin modules and usage; it does not publish a catalog tree to Figma. |
| `repo/verification-platform` | `verificationPlatform` | Describes provider-neutral verification modules; it does not publish a catalog tree to Figma. |
| `repo/water-my-plants-project-config` | `waterMyPlantsProjectConfig` | Describes the product catalog and composition modules; its provider supplies the two production trees below. |

The only catalog-tree visual targets in the Water My Plants adapter are
`waterMyPlants.libraries` and `waterMyPlants.plugins`. They describe the
dependency catalog used to build and test the application. Included-build
`versions.properties` files are private build-tool inputs and are not rendered
as application dependency trees.

The portable plugin id is `com.marmatsan.figmaDocumentationSync`. It intentionally has
no Water My Plants defaults. See
[`../water-my-plants-project-config/README.md`](../water-my-plants-project-config/README.md)
for the adapter contract required by another repository.

## Distribution Readiness

Consumers will apply one versioned Gradle plugin rather than addressing the
internal projects. The staged publication contains the plugin marker,
`figma-documentation-sync-gradle-plugin`, transitive domain and data artifacts, the
and the optional `teamcity-adapter`. Dependency Catalog is distributed and
verified independently. Water My Plants product configuration is deliberately
excluded.

The TypeScript writer is prepared as
`@marmatsan/figma-documentation-sync-tools`. Its build executable injects a
Kotlin-generated, repository-owned JSON projection, so the published package
does not own Figma node ids or repository paths. Maven and npm versions must
remain aligned.

Validate the complete staged distribution without publishing externally:

```powershell
.\gradlew.bat :figma-documentation-sync:verifyStagedPublication

Push-Location repo\figma-documentation-sync\tools
$env:npm_config_cache = "..\..\..\build\npm-cache"
npm pack --dry-run
Remove-Item Env:npm_config_cache
Pop-Location
```

The standalone fixture resolves both public plugins from generated Maven files,
owns its provider and `versions.properties`, and does not use `includeBuild`.
See the
[adoption guide](docs/guides/adopting-figma-documentation-sync.md),
[distribution contract](docs/reference/distribution-contract.md), and
[publication runbook](docs/runbooks/publishing-release.md).

## Output Contract

`generateFigmaDesignModel` writes:

```text
build/reports/figma-sync/design-model.json
```

The stable `content` object contains:

| Key | Meaning |
|-----|---------|
| `versions` | Flat map of version keys to values. |
| `versionSections` | Ordered version sections used by the Figma versions page. |
| `catalogs` | Library, plugin, custom Gradle plugin, and convention plugin trees. |
| `modules` | Repository module paths discovered from the root project and included builds. |
| `moduleDependencies` | Module dependency edges grouped by source build. |
| `ci` | Optional CI topology, runtime, and generated CI configuration selected by the project adapter. |

The portable plugin leaves `ciDocumentationEnabled` disabled. Water My Plants
enables it in `project-config` with the JSON key `teamCity` and the Kotlin
`TeamCityCiConfigurationProvider`. A new repository can omit CI entirely or
select another `CiConfigurationProvider` without changing `domain`, `data`, or
`plugin`; it needs no TeamCity, CI topology YAML, or PowerShell.

Figma visual code must treat this JSON as the source of truth. Manual visual
changes in Figma are acceptable only when they are component contract changes;
data shown in Figma must come from the generated model.

## Gradle Tasks

Run these from the repository root:

```powershell
.\gradlew.bat checkFigmaVersionNaming
.\gradlew.bat checkFigmaCatalogUsage
.\gradlew.bat checkCiWindowsRuntimeFreshness
.\gradlew.bat classifyFigmaChangeImpact
.\gradlew.bat validateCanonicalFigmaArtifactSet `
    -PfigmaArtifactDirectory=<artifact-directory>
.\gradlew.bat uploadCanonicalFigmaPayload `
    -PfigmaTeamCityBuildId=<job-run-id> `
    -PfigmaMcpUploadUrl=<single-use-upload-url>
```

Task responsibilities:

| Task | Responsibility |
|------|----------------|
| `classifyFigmaChangeImpact` | Writes the Git-derived verification scope and affected visual targets to `build/reports/figma-sync/change-impact.json`. |
| `prepareCanonicalFigmaSync` | Cleans stale reports, classifies the main revision, conditionally generates the model and MCP runner artifacts, and writes `sync-scope.json`. |
| `probeFigmaMcp` | Probes endpoint capabilities through the official Kotlin MCP SDK client. |
| `runFigmaMcp` | Inspects, checkpoints, or executes a canonical runner through the Kotlin MCP adapter. |
| `materializeFigmaSyncCiConfiguration` | Runs the optional CI adapter command before a full model generation; it is skipped when CI documentation is disabled or no command is configured. |
| `verifyCanonicalFigmaSync` | Validates the downloaded scope identity and runs the trunk metadata check only for `full-verification`. |
| `validateCanonicalFigmaArtifactSet` | Validates that the downloaded model, scope, plan, and runner manifests share one canonical `main` identity before the MCP handoff. |
| `prepareTeamCityFigmaSyncHandoff` | Water My Plants Kotlin adapter that downloads or opens canonical TeamCity artifacts, validates them, and writes `figma-sync-handoff.json`. |
| `uploadCanonicalFigmaPayload` | Water My Plants Kotlin adapter that downloads one successful main TeamCity artifact, verifies its manifest-declared PNG, and uploads it only to an allow-listed single-use Figma MCP URL. |
| `rerunTeamCityFigmaSync` | Water My Plants Kotlin adapter that authenticates through Cloudflare, reuses or queues the canonical TeamCity pipeline, and optionally waits for success. |
| `checkFigmaVersionNaming` | Fails when version keys do not follow the Figma naming contract. |
| `checkFigmaCatalogUsage` | Fails when catalog entries are declared but unused according to the repository usage contract. |
| `checkCiExternalTopologyFreshness` | Emits a non-blocking warning when the external topology has not been manually validated within its configured window. |
| `checkCiWindowsRuntimeFreshness` | Emits a non-blocking warning when the Windows service runtime has not been manually validated within its configured window. |
| `generateFigmaDesignModel` | Generates the canonical JSON artifact inside TeamCity `Figma Sync` on `main`; do not run it as a local publication path. |
| `checkFigmaTrunkSync` | Compares the generated `modelHash` with Figma shared plugin metadata inside the canonical pipeline. |

TeamCity may expose canonical synchronization as sequential steps by passing
`-PfigmaCanonicalTeamCityPhasedExecution=true`. In that mode it invokes
classification first, model materialization second, runner and visual-plan
construction third, then scope validation before the metadata check. The flag
removes only the dependency edges that would repeat an earlier TeamCity step;
each phase still consumes the files produced in the same job workspace and
fails closed when a prerequisite is absent. Local and third-party consumers
must keep using the dependency-complete `prepareCanonicalFigmaSync` and
`verifyCanonicalFigmaSync` entry points without this adapter flag.

The change-impact classifier is implemented in Kotlin and is portable across
Windows, macOS, and Linux. See
[`docs/reference/change-impact-classification.md`](docs/reference/change-impact-classification.md)
for its policy, precedence, and output contract.

`checkFigmaVersionNaming` and `checkFigmaCatalogUsage` are wired into the root
Gradle `check` lifecycle. The two CI freshness checks are also wired but skip
themselves unless the project adapter enables CI documentation. Water My Plants
runs them through:

```powershell
.\gradlew.bat check
```

## Figma Sync Flow

The strict flow is:

1. Merge code changes through a pull request after TeamCity CI passes.
2. Let TeamCity generate the effective configuration from `.teamcity/settings.kts`.
3. Let the same job generate the canonical `design-model.json` from those effective files.
4. Use the canonical artifact as the visual sync input.
5. Run the canonical visual manifest with `runFigmaMcp` so `preflight` and every
   planned target execute in contractual order without writing metadata.
6. Validate all managed sections, then run the separate `metadata` target.
7. Verify `checkFigmaTrunkSync` so Figma metadata matches `main`.

Do not create canonical design-model metadata from a feature branch. Branch-local
visual iteration may reuse a canonical `main` artifact for layout debugging,
but it must use only the artifact's atomic visual units and must not publish
trunk metadata. A partial run never completes the canonical synchronization.

The canonical MCP runner uses PNG payload transport by default: the generated
runner writes `10-canonical-sync-payload.png`, that image is uploaded to Figma,
and the staging runner extracts and validates the model plus MCP script before
storing the script as plain text in temporary shared plugin data. Avoiding a
second Base64 encoding keeps the staging entry below Figma's per-entry limit.
The repository-specific `uploadCanonicalFigmaPayload` task keeps this transfer
Kotlin-first: it validates the successful main TeamCity build, cross-file
artifact identity, payload length and SHA-256, PNG signature, and exact
`https://mcp.figma.com/mcp/upload/<id>/submit?scaleMode=FILL` destination before
sending any bytes. The single-use URL is internal task state and is never
written to the handoff summary or task output.
Each visual target then runs in a separate, lexically ordered MCP call. Catalog
targets are further split by declared root and end with a cleanup-only call.
This keeps the complete synchronization mandatory without exceeding the MCP
timeout with one monolithic call. Chunked staging remains a fallback for
oversized or blocked asset uploads.

TeamCity also publishes a `visual-sync-plan.json`. It selects `none`, `partial`,
or `full` from the canonical model, compiled writer, per-target model
fingerprints, and per-scope writer fingerprints. A target-specific writer
change reruns only that target family plus preflight; shared or unmapped writer
code still fails closed to a full sync.
Execution checkpoints allow the supported MCP operator to resume at the first
unfinished unit without repeating successful targets. See
`docs/runbooks/visual-sync-efficiency.md` for the identity and recovery rules.

## Human Workflow

Kotlin sources and build scripts in this included build follow the repository
[Kotlin standard](../../docs/standards/kotlin.md). The repository-wide KtLint
migration changed mechanical layout only; it did not change the design-model,
visual-writer, fingerprint, or synchronization contracts. The CI visual model
does use the renamed repository task `checkKotlinStyle`, which remains covered
by its planner test and the versioned visual contract.

For code changes in this module:

```powershell
.\gradlew.bat :figma-documentation-sync:check
.\gradlew.bat :water-my-plants-project-config:check
```

For visual tooling changes:

```powershell
.\gradlew.bat testFigmaDocumentationSyncTools buildFigmaDocumentationSyncTools
```

For dependency catalog changes that affect Figma:

```powershell
.\gradlew.bat checkFigmaVersionNaming checkFigmaCatalogUsage
```

After merge, use the `design-model.json` generated by TeamCity `Figma Sync` on
`main`; do not replace it with a locally generated or branch artifact.

## Where To Read Next

Use `docs/README.md` as the documentation index.

High-signal entry points:

| Document | Use When |
|----------|----------|
| `docs/standards/dependency-version-naming.md` | Adding or renaming dependency version keys. |
| `docs/reference/visual-sync-contract.md` | Changing component bindings, catalog trees, connectors, layout, or locking. |
| `docs/runbooks/trunk-sync.md` | Running the canonical trunk sync workflow. |
| `docs/runbooks/canonical-artifact-visual-sync.md` | Deciding whether a `design-model.json` is canonical enough for sync. |
| `docs/runbooks/visual-sync-efficiency.md` | Executing the smallest safe target set and resuming from checkpoints. |
| `docs/reference/target-scopes.md` | Updating the smallest possible Figma section. |
| `docs/runbooks/troubleshooting.md` | Diagnosing broken sync output or metadata mismatches. |
