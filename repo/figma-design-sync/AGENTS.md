# figmaDesignSync Agent Instructions

This module generates the Figma design model and verifies the Figma sync state
used by CI.

## Architecture

- `figmaDesignSync` follows Clean Architecture. Preserve the dependency
  direction:
  - `domain` must not depend on `data`, `plugin`, Gradle APIs, or
    `java.io.File`.
  - `data` depends on `domain` and implements domain ports.
  - `teamcity-adapter` depends on `data` and `domain`; portable modules must
    not depend on it.
  - `plugin` is the Gradle adapter and composition root; it may know about
    `data` only for dependency injection bindings.
- Keep one top-level class, interface, object, or data class per Kotlin file.
  Nested types are allowed only when they are owned by and used only by the
  parent type, such as sealed result variants or private implementation
  helpers.
- Use `kotlin-inject` for classes created by `figmaDesignSyncComponent`.
- Keep Gradle-created types compatible with Gradle injection. Do not replace
  Gradle constructor injection annotations with
  `me.tatarka.inject.annotations.Inject` on extension/task/plugin types.
- `figmaDesignSyncComponent` is the composition root. Bind domain ports to
  `data/datasource` implementations there.

## Package Layout

- `domain/model/catalog`: catalog tree, node, entry, and version models.
- `domain/model/ci`: external topology, Windows service runtime, and generic
  effective CI configuration models used by CI documentation.
- `domain/model/figma`: Figma references used by domain requests.
- `domain/model/modules`: module dependency models included in the generated
  design model.
- `domain/model/impact`: portable Figma verification scopes, impacts, policy,
  and repository change-set models.
- `domain/model/sync`: portable identity shared by official preparation and
  verification jobs.
- `domain/model/artifact` and `domain/service/artifact`: pure official artifact
  identities and cross-file validation rules used by the MCP handoff.
- `domain/port/catalog`, `domain/port/modules`, and `domain/port/versions`:
  source ports used to build the generated design model.
- `domain/port/ci`: path-based sources and ports for external topology,
  Windows service runtime, and project-selected effective CI configuration.
- `domain/port/impact`: policy and Git change-set source boundaries used by
  Figma impact classification.
- `data/datasource/catalog`, `data/datasource/modules`, and
  `data/datasource/versions`: implementations of domain ports grouped by
  capability.
- `data/datasource/ci`: filesystem adapters for CI documentation sources.
- `data/ci/configuration`: portable provider contract and reflective provider
  selection used by project configuration.
- `data/datasource/impact`: JSON policy and Git adapters for change-impact
  classification.
- `data/figma/client`: Figma API client and client exceptions.
- `data/figma/dto`: serializable Figma API response and node DTOs.
- `data/figma/common`: shared Figma URL helpers.
- `data/figma/artifact`: filesystem and JSON readers for official TeamCity
  artifact sets.
- `data/figma/sync`: JSON adapters for the official preparation scope, runner
  identities, and visual sync plan.
- `data/gradle/catalog` and `data/gradle/modules`: readers for Gradle settings
  catalog declarations, included modules, and module dependencies.
- `data/dependencies/catalog`: portable adapters from the reusable
  `catalog-core` tree types and the `DependencyCatalogProvider` contract to
  domain catalog models. Concrete repository catalogs belong in
  `project-config`.
- `data/properties/versions`: readers for version properties files.
- `teamcity-adapter/configuration`: optional reader for TeamCity generated YAML
  and XML. Keep TeamCity-specific parsing out of portable modules.
- `data/yaml/ci`: YAML 1.2 readers for the versioned external topology and
  Windows service runtime.
- `plugin/generator`: design model JSON generation and hash calculation.
- `plugin/checker/versions`: Gradle-facing adapter that verifies repository
  version section and suffix naming before CI can merge catalog changes.
- `plugin/checker/catalog`: Gradle-facing adapter that rejects unused
  dependency catalog entries before CI can merge catalog changes.
- `plugin/checker/sync`: Gradle-facing adapter that compares generated model
  metadata with Figma shared plugin data.
- `plugin/checker/impact`: pure classifier for repository paths that can affect
  the Figma model or visual writer.
- `plugin/task/generate` and `plugin/task/sync`: Gradle task classes for model
  generation and sync verification.
- `plugin/task/impact`: portable Gradle task that writes
  `build/reports/figma-sync/change-impact.json`.
- `plugin/task/artifact`: Gradle adapter that validates an official artifact
  set and writes its typed handoff identity.
- `plugin/task/official`: Gradle adapters that prepare and validate the scope
  shared by the official TeamCity Figma Sync jobs.
- `plugin/di`: kotlin-inject component and bindings.
- `plugin/gradle`: Gradle plugin and extension classes.
- `project-config`: repository adapter that owns Water My Plants paths,
  dependency catalog provider, Figma identities, visual targets, CI provider
  selection, branch aliases, and optional CI commands. Portable modules must
  depend on adapter contracts, never on this concrete implementation.
- `tools`: portable TypeScript writer and MCP transport. Project-specific
  constants are selected through `@figma-design-sync/project-config` and must
  not be added under `tools/src` or `tools/scripts`.

## Gradle Tasks

- `generateFigmaDesignModel`: generates
  `build/reports/figma-sync/design-model.json`.
- `classifyFigmaChangeImpact`: classifies the Git diff using
  the policy selected by the project-config adapter. Water My Plants owns it at
  `repo/figma-design-sync/project-config/water-my-plants/change-impact-policy.json`.
  Keep the classifier in Kotlin and do not duplicate its rules in TeamCity scripts.
- `prepareOfficialFigmaSync`: cleans stale reports, classifies the change,
  conditionally runs the configured CI adapter and generates the official model, then
  builds the MCP runner artifacts and `sync-scope.json`.
- `verifyOfficialFigmaSync`: validates the downloaded scope identity and
  invokes the Kotlin trunk checker only for `full-verification`.
- `checkFigmaCatalogUsage`: fails when dependency catalogs declare library or
  plugin entries that are not used by a module, convention plugin, or tool
  configuration. This task is wired into the root `check` lifecycle.
- `checkFigmaVersionNaming`: fails when
  `repo/dependency-catalog/versions.properties` does not use the Figma version
  naming contract. This task is wired into the root `check` lifecycle.
- `checkCiExternalTopologyFreshness`: emits a non-blocking warning after the
  validation window in `docs/ci/external-topology.yaml` expires. This task is
  wired into the root `check` lifecycle and skips when the project adapter
  disables CI documentation.
- `checkCiWindowsRuntimeFreshness`: emits a non-blocking warning after the
  validation window in `docs/ci/windows-runtime.yaml` expires. This task is
  wired into the root `check` lifecycle and skips when the project adapter
  disables CI documentation.
- `checkFigmaTrunkSync`: compares the generated model hash with Figma shared
  plugin data.
- `validateOfficialFigmaArtifactSet`: validates the downloaded main model,
  scope, plan, and runner manifests before MCP-operated publication.
- `prepareTeamCityFigmaSyncHandoff`: Water My Plants project adapter that
  prepares a validated local handoff from a TeamCity build id or existing
  artifact directory. Keep its TeamCity CLI boundary in `teamcity-adapter`.
- `uploadOfficialFigmaPayload`: Water My Plants project adapter that accepts
  only a successful main TeamCity build and a single-use
  `mcp.figma.com/mcp/upload/.../submit` URL, then verifies and uploads the
  manifest-declared PNG through Kotlin.
- `rerunTeamCityFigmaSync`: Water My Plants project adapter that obtains
  credentials through a port, exchanges Cloudflare service auth, and reuses or
  queues the official `main` pipeline. Keep TeamCity CLI reads and cookie-free
  REST writes in `teamcity-adapter`, and keep secret-store selection outside
  portable modules.
- Treat `figmaDesignSync` as a CI-owned verification step. Developers may run it
  locally for diagnosis, but CI is the source of truth before merging into
  `main`.
- Module dependency extraction reads Gradle dependencies from `project(":...")`
  and type-safe project accessors such as `projects.core.ui` or
  `projects.domain`. Resolve type-safe accessors against real module paths so
  camel-case accessors preserve kebab-case names; for example,
  `projects.catalogCore` represents `:catalog-core`.

## Figma Automation

- The Figma sync namespace is `water_my_plants_sync`. Figma shared plugin data
  namespaces must not contain hyphens.
- The Figma write step is MCP-operated.
- Detailed Figma automation runbooks live in `docs/runbooks/`, except UML
  publication runbooks, which live under `docs/uml/` next to the diagrams and
  helper tools. Agents must follow:
  - `docs/runbooks/trunk-sync.md` when validating or publishing the Figma trunk
    sync state.
  - `docs/runbooks/official-artifact-visual-sync.md` when deciding whether a
    `design-model.json` may be used for an official or branch-local visual sync.
  - `docs/runbooks/mcp-chunk-transport.md` when staging TeamCity artifacts and
    generated MCP scripts through Figma shared plugin data.
  - `docs/reference/target-scopes.md` when choosing the smallest sync target for
    a Figma section.
  - `docs/reference/visual-sync-contract.md` when changing or validating Figma
    visual sync behavior, especially catalog tree nodes, connectors, layout, and
    locking.
  - `docs/runbooks/troubleshooting.md` when diagnosing failed or visually
    incorrect Figma sync runs.
  - `docs/uml/figma-import.md` when publishing PlantUML-generated UML diagrams
    to Figma.
- When adding or updating a `.Header` component for a Figma documentation
  section, make the header describe the section it belongs to. Fill the header
  content with:
  - `Header`: the section/module/diagram name the reader is looking at.
  - `Link`: relevant repository files that live in GitHub, with each file
    hyperlink applied to its GitHub `main` branch URL, for example
    `https://github.com/marmatsan/water-my-plants/blob/main/<path>`. If the
    `Link` field lists multiple files, apply hyperlinks to the individual
    filename ranges and leave separators unlinked.
  - `Definition`: a concise explanation of what this section documents and why
    it matters.

## UML Publication

- Required local SVG tooling is documented in `docs/uml/figma-import.md`.
  Check Java, PlantUML, Graphviz `dot`, PowerShell, and optional Inkscape before
  diagnosing render or import failures.
- Import PlantUML SVGs through the generated SVG directly.
- Do not publish UML diagrams to Figma as PNG/image fills. PNG exports may be
  used only for diagnosis; final UML publication must use
  `figma.createNodeFromSvg(svg)`.
- Publish diagrams inside a parent documentation section, not as loose top-level
  sections. The parent section owns the `.Header`, uses
  `md/sys/color/surface` as fill, has corner radius `28`, and is named after the
  scope that groups the included `.puml` diagrams.
- The individual diagram section is nested inside that parent section and is
  named after the `.puml` file, for example `model-generation-flow.puml`.
- Flatten the Figma wrapper structure so only the imported `Group` node remains
  inside the diagram section, then center that `Group`.
- The final imported `Group` must be a Figma `GROUP`, not a `FRAME`. If
  `createNodeFromSvg()` leaves `FRAME:Group`, group its children into
  `GROUP:Group` and remove the wrapper frame.
- Do not leave nested imported layers also named `Group`. A diagram section
  should have exactly one layer named `Group`; rename internal groups to
  meaningful names such as `Title`, `Legend`, or the PlantUML entity/link id.
- Before importing PlantUML SVGs into Figma with `createNodeFromSvg()`, sanitize
  them with `docs/uml/tools/sanitize-svg-for-figma.ps1`.
- Keep sanitized SVGs as temporary `tmp/uml/figmaDesignSync/` artifacts.
- Treat UML publication as a `main` CI requirement. Any `.puml` diagram added
  or changed in this module must live in the Figma UML documentation page after
  the branch is merged to `main`; do not consider the merge complete while the
  corresponding Figma section is missing or stale.
- Do not remove PlantUML SVG `lengthAdjust`, `textLength`, or
  `stroke-dasharray` attributes during normal sanitization. They preserve text
  fit and dashed sequence-diagram semantics in Figma.
- After importing PlantUML SVGs into Figma, verify text alignment visually.
  Figma can recalculate SVG text widths differently from PlantUML.
- After importing PlantUML SVGs into Figma, verify structurally that the section
  has no descendant with an `IMAGE` fill. A valid import should contain SVG
  `VECTOR` and `TEXT` descendants created by `createNodeFromSvg()`.
- After importing sequence diagrams into Figma, verify dashed lifelines and
  return arrows visually. If Figma flattened them into solid vectors, restore
  `dashPattern` on the imported nodes.
- If a PlantUML sequence lifeline is missing after import, prefer cloning an
  existing imported lifeline group and copying its exact `strokes`,
  `strokeWeight`, `dashPattern`, and opacity. Newly created Figma `LINE` nodes
  can render with different bounds, z-order, or stroke weight than the imported
  SVG vectors.

## BDD

- The executable BDD source of truth lives in
  `plugin/src/test/resources/com/marmatsan/figmaDesignSync/plugin/bdd/`.
- Step definitions live in
  `plugin/src/test/kotlin/com/marmatsan/figmaDesignSync/plugin/bdd/`.
- Use `io.cucumber.java8.En` style definitions. Do not add
  `io.cucumber.java` annotations unless the project intentionally changes
  style.
- Keep `.feature` files focused on observable behavior. Put concrete files,
  adapters, ports, and generated artifacts in `docs/bdd/README.md` or module
  architecture docs unless a scenario is explicitly about that path.
- Feature diagrams are derived documentation. Keep the `.feature` file as the
  executable source of truth.

## Testing

- Keep focused tests around model generation, hashing, and sync verification.
- Keep Figma/API/file-system details in `data` or task/checker tests.
- Useful verification command:

```powershell
.\gradlew.bat :figma-design-sync:domain:check `
    :figma-design-sync:data:check `
    :figma-design-sync:teamcity-adapter:check `
    :figma-design-sync:plugin:check `
    :figma-design-sync:project-config:check
```

- Useful root-project diagnostic command:

```powershell
.\gradlew.bat checkFigmaVersionNaming checkFigmaCatalogUsage
```

- Do not generate `design-model.json` locally or from a feature branch. Only
  TeamCity `Figma Sync` on `main` may produce the official model artifact.
