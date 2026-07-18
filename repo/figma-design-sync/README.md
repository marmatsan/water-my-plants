
# figma-design-sync

`figma-design-sync` is the repository-owned bridge between the Gradle project
model and the Figma documentation model. It is not application production code:
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
   `transportHash`, and per-target fingerprints.

The generated hash is based on `schemaVersion` and stable `content`. Traceability
fields such as branch, Git SHA, and generation timestamp are written to the
model, but they do not affect `modelHash`.

## Included Build Shape

This directory is an included Gradle build with three Kotlin modules and one
TypeScript tooling package:

| Path | Role |
|------|------|
| `domain/` | Pure model and port definitions for versions, catalogs, modules, and module dependency edges. |
| `data/` | File, Gradle, dependency-catalog, and Figma API adapters that implement domain ports. |
| `plugin/` | Gradle plugin, tasks, checkers, dependency injection bindings, and model generation orchestration. |
| `tools/` | TypeScript MCP/Figma scripts and visual sync tests that consume `design-model.json`. |
| `docs/` | Runbooks, BDD notes, UML diagrams, and visual contract documentation. |

Dependency direction is intentional:

```text
domain <- data <- plugin
```

`domain` must stay independent from Gradle, files, Figma clients, and plugin
composition. `plugin` is the adapter layer that wires Gradle tasks to domain
ports through `data` implementations.

## Inputs

The model is generated from repository source files, not from Figma:

| Input | Purpose |
|-------|---------|
| `repo/dependency-catalog/versions.properties` | Ordered version sections rendered in Figma and validated by CI. |
| `repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt` | Source of truth for main project library catalog trees. |
| `repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt` | Source of truth for main project plugin catalog trees. |
| Root `settings.gradle.kts` | Main project module discovery. |
| Included-build `settings.gradle.kts` files | Included-build catalog and module discovery. |
| Gradle build files | Module dependency edges and applied plugin usage. |
| `docs/ci/external-topology.yaml` | Versioned external systems, access boundaries, and directed connections. |
| `docs/ci/windows-runtime.yaml` | Versioned Windows services, startup modes, and service identities for the local CI runtime. |
| `.teamcity/target/generated-configs` | Effective pipelines, jobs, triggers, artifacts, checks, and VCS roots generated from `.teamcity/settings.kts`. |
| `repo/figma-design-sync/change-impact-policy.json` | Path policy used to classify whether a change can affect the model or visual writer. |

The default included-build sources are configured by the `figmaDesignSync`
Gradle extension:

| Included build | Model name | Purpose |
|----------------|------------|---------|
| `repo/dependency-catalog` | `dependencyCatalog` | Hosts the reusable `catalog-core` DSL and the concrete `water-my-plants-catalog` definition. |
| `repo/figma-design-sync` | `figmaDesignSync` | Describes this tooling build's own dependencies. |
| `repo/gradle-plugins` | `gradlePlugins` | Describes repository Gradle plugin modules and convention plugins. |

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
| `ci` | External CI topology, Windows service runtime, and effective TeamCity configuration. |

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
```

Task responsibilities:

| Task | Responsibility |
|------|----------------|
| `classifyFigmaChangeImpact` | Writes the Git-derived verification scope and affected visual targets to `build/reports/figma-sync/change-impact.json`. |
| `checkFigmaVersionNaming` | Fails when version keys do not follow the Figma naming contract. |
| `checkFigmaCatalogUsage` | Fails when catalog entries are declared but unused according to the repository usage contract. |
| `checkCiExternalTopologyFreshness` | Emits a non-blocking warning when the external topology has not been manually validated within its configured window. |
| `checkCiWindowsRuntimeFreshness` | Emits a non-blocking warning when the Windows service runtime has not been manually validated within its configured window. |
| `generateFigmaDesignModel` | Generates the official JSON artifact inside TeamCity `Figma Sync` on `main`; do not run it as a local publication path. |
| `checkFigmaTrunkSync` | Compares the generated `modelHash` with Figma shared plugin metadata inside the official pipeline. |

The change-impact classifier is implemented in Kotlin and is portable across
Windows, macOS, and Linux. See
[`docs/reference/change-impact-classification.md`](docs/reference/change-impact-classification.md)
for its policy, precedence, and output contract.

`checkFigmaVersionNaming`, `checkFigmaCatalogUsage`,
`checkCiExternalTopologyFreshness`, and `checkCiWindowsRuntimeFreshness` are
wired into the root Gradle `check` lifecycle, so the TeamCity `Verify` step
runs them through:

```powershell
.\gradlew.bat check
```

## Figma Sync Flow

The strict flow is:

1. Merge code changes through a pull request after TeamCity CI passes.
2. Let TeamCity generate the effective configuration from `.teamcity/settings.kts`.
3. Let the same job generate the official `design-model.json` from those effective files.
4. Use the official artifact as the visual sync input.
5. Run the official MCP runner without a target so `preflight` and every visual
   target execute in contractual order without writing metadata.
6. Validate all managed sections, then run the separate `metadata` target.
7. Verify `checkFigmaTrunkSync` so Figma metadata matches `main`.

Do not create official design-model metadata from a feature branch. Branch-local
visual iteration may reuse an official `main` artifact for layout debugging, but
it must use `--allow-partial=true` for focused diagnostics and must not publish
trunk metadata. A partial run never completes the official synchronization.

The official MCP runner uses PNG payload transport by default: the generated
runner writes `10-official-sync-payload.png`, that image is uploaded to Figma,
and the staging runner extracts and validates the model plus MCP script before
storing the script as plain text in temporary shared plugin data. Avoiding a
second Base64 encoding keeps the staging entry below Figma's per-entry limit.
Each visual target then runs in a separate, lexically ordered MCP call. Catalog
targets are further split by declared root and end with a cleanup-only call.
This keeps the complete synchronization mandatory without exceeding the MCP
timeout with one monolithic call. Chunked staging remains a fallback for
oversized or blocked asset uploads.

TeamCity also publishes a `visual-sync-plan.json`. It selects `none`, `partial`,
or `full` from the official model, compiled writer, and per-target fingerprints.
Execution checkpoints allow the supported MCP operator to resume at the first
unfinished unit without repeating successful targets. See
`docs/runbooks/visual-sync-efficiency.md` for the identity and recovery rules.

## Human Workflow

For code changes in this module:

```powershell
.\gradlew.bat :figma-design-sync:domain:check :figma-design-sync:data:check :figma-design-sync:plugin:check
```

For visual tooling changes:

```powershell
cd repo\figma-design-sync\tools
npm test
npm run build
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
| `docs/runbooks/trunk-sync.md` | Running the official trunk sync workflow. |
| `docs/runbooks/official-artifact-visual-sync.md` | Deciding whether a `design-model.json` is official enough for sync. |
| `docs/runbooks/visual-sync-efficiency.md` | Executing the smallest safe target set and resuming from checkpoints. |
| `docs/reference/target-scopes.md` | Updating the smallest possible Figma section. |
| `docs/runbooks/troubleshooting.md` | Diagnosing broken sync output or metadata mismatches. |
