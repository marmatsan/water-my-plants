---
title: Figma visual sync contract
type: reference
scope: repo/figma-documentation-sync
owner: figma-documentation-sync
status: active
last-reviewed: 2026-07-27
review-cycle-days: 90
sources:
  - repo/figma-documentation-sync/tools/src
  - repo/figma-documentation-sync/tools/tests
  - repo/figma-documentation-sync/domain/src/main/kotlin/com/marmatsan/figmaDocumentationSync/domain/service/visual
  - repo/figma-documentation-sync/data/src/main/kotlin/com/marmatsan/figmaDocumentationSync/data/json/visual
  - repo/figma-documentation-sync/plugin/src/main/kotlin/com/marmatsan/figmaDocumentationSync/plugin/task/visual
  - docs/decisions/adr-0008-use-canonical-for-authoritative-figma-sync.md
---

# Figma Visual Sync Contract

## Purpose

This document captures the stable visual contract used by the MCP sync tooling.
The execution flow lives in [trunk-sync.md](../runbooks/trunk-sync.md); this file describes
what the generated Figma state must look like after the sync.

## Terminology

Per [ADR-0008](../../../../docs/decisions/adr-0008-use-canonical-for-authoritative-figma-sync.md),
`canonical` identifies the authoritative `main` publication and its reusable
baseline. The term `official` is reserved for vendor-provided technology and
assets; it is not a Figma Sync execution mode.

## Tooling Boundary

Visual planning is split by capability. Pure, deterministic plans live in
Kotlin under `domain/service/visual`; JSON adaptation lives in
`data/json/visual`. TypeScript under `tools/src` owns only orchestration that
runs inside the Figma Plugin API and the concrete node, component, variable,
text, and connector adapters.

The tool source follows the same dependency direction as the Gradle sync code:

- Kotlin `domain` contains portable models and pure visual rules.
- Kotlin `data` translates language-neutral JSON into those models and emits
  target-scoped plan JSON.
- TypeScript `domain` retains only the language-neutral visual-plan boundary
  types required by the Figma adapter.
- `ports` contains gateway contracts.
- `usecases` coordinates sync behavior through those contracts.
- `figma` contains the Figma MCP API adapters.
- `app` wires concrete gateways for the generated MCP entrypoint.

The generated JavaScript bundle is a temporary MCP runtime artifact and must not
be committed.

Preview runners may pass `sectionNodeOverrides` so a catalog target mutates a
sandbox section instead of the configured canonical section. Canonical sync runs
must use the configured section ids from
`WaterMyPlantsFigmaWriterProjectConfig.kt` unless a documented manual repair
explicitly overrides one target.

The `preflight` target validates the visual contract without mutating Figma.
Run it after staging the canonical model and before visual targets when component
contracts changed. It checks the metadata page, version variable collection,
version section frames, `.tree node` component properties, `.artifact` and
`.artifacts bundle` template properties, `.tool artifact usage`, `.usage chip`
variants, configured catalog sections, catalog target model shape, and root
filters. A successful preflight must return `mutatedNodeIds: []`.

For library catalog items, preflight validates `.artifact`, `.artifacts bundle`,
and `.tool artifact usage` templates from the target section when present,
falling back to the base `.tree node` component only when the section has no
template. This matches the writer behavior: new nodes clone compatible section
instances before falling back to the base component variant. When an instance
property hides a required nested template from instance traversal, preflight
validates that template through the instance's main component. This fallback is
read-only and must not toggle component properties merely to expose hidden
slots.

Catalog runners may pass `catalogRootFilters` to sync only one or more
top-level roots inside a catalog target, for example `androidx` in
`waterMyPlants.libraries` or `com` in `waterMyPlants.plugins`. Root filters are
runtime scope only: they narrow an already-authorized `main` design model and
must not be used to create an alternate branch-local model.

When `catalogRootFilters` is present, the mutation scope is the selected root
subtree, not the whole visual section. The sync may update expected nodes,
create missing expected nodes, and remove stale nodes reachable from the
selected roots through managed `treeConnectorEdge` metadata. It must leave
sibling roots outside the filter untouched, including their stale nodes and
connectors. The exception is an empty top-level root section that no longer
exists in the complete target model; that stale empty section may be removed
even during a partial root sync so visual cleanup is not blocked by repair
granularity.

Partial root sync is a repair/execution granularity, not a completion signal.
The canonical metadata (`gitSha`, `modelHash`, `writerHash`, `transportHash`,
model target fingerprints, writer scope fingerprints, and writer fingerprint
schema version) must be written only after all scopes required by the
TeamCity-generated visual plan have completed successfully. An ad hoc partial
runner does not authorize metadata.

## CI Documentation Visual Sync

The Kotlin `CiVisualPlanner` reads only `content.ci` from a `design-model.json`
through `CiVisualPlanJson`. The planner composes one `CiVisualSectionPlanner`
strategy per visual target, so a section can be extended or replaced without
changing the plan orchestrator; each strategy preserves the same portable
section contract. The canonical runner generator embeds one
target-scoped `ciVisualPlan` in `SYNC_OPTIONS`; the TypeScript Figma gateway
consumes that plan and never decides CI section structure. Preview and
compatibility runners must receive the same contract through
`--ci-visual-plan=PATH`. Generate complete or target-scoped preview input with
the portable `generateFigmaCiVisualPlan` Gradle task.

The TypeScript use case fails before reaching Figma when a requested `ci.*`
target has no Kotlin-generated plan. This is an architectural boundary, not a
fallback condition: planning rules, environment mapping, source selection, and
CI graph construction belong to Kotlin; measured node geometry, fonts,
components, variables, connectors, and mutations remain in the Figma adapter.

The Figma gateway creates or updates one parent section named
`Continuous Integration and Documentation Automation` on Figma page `63153:2876`
and exposes six independently runnable targets:

- `ci.overview`;
- `ci.pullRequestIntegration`;
- `ci.postMergeDesignDocumentation`;
- `ci.jobTasks`;
- `ci.infrastructureAndAccess`;
- `ci.windowsRuntime`.

Adding, renaming, or removing a CI target is an aggregate contract change.
Update the Kotlin writer project config, the language-neutral
`writer-runtime-contract.json` fixture, the TypeScript preflight target list,
and `change-impact-policy.json` in the same change. Otherwise the generated
writer can understand the target while runtime contract checks or incremental
scope selection still omit it.

`ci.pullRequestIntegration` and `ci.postMergeDesignDocumentation` render jobs
with empty `phases` and `outcomes` arrays so their primary flows stay compact. `ci.jobTasks`
owns the expanded job nodes for both pipelines, places them in one grid row,
and has no connectors. Expanded nodes use the same compositional API as every
other node: `.ci node` -> `.ci phase` -> `.ci step`, plus sibling
`.ci outcome` instances.

The plan uses schema version `4`. Every visual entity contains typed `phases`
and `outcomes` arrays, including empty arrays when it has no executable detail.
Each phase owns a typed `steps` array. TypeScript rejects incompatible schemas,
missing arrays, invalid roles or kinds, and capacity overflow before any Figma
call. Preview packaging applies the same structural validation.

Each visual entity starts with an instance of `.ci node` (`64670:3088`). The
writer selects the matching mode from the `ci/cd` variable collection:
`Actor`, `System`, `Git reference`, `Pipeline`, `Job`, `Artifact`, `Check`, or
`Gate`. The collection's `type_label` string supplies the visible type label
for each mode. The writer binds the exposed `name`, `description`, `source`,
and `execution plan heading` text properties. It controls `show execution plan`
from the presence of typed phases, `show outcome` from the presence of typed
outcomes, and `show source` and `show runtime` from actual model content. The
component does not expose `steps`, `show steps`, or any other
compatibility-only execution property.
The `source` TextNode must bind its characters to the public `source` property.
The writer resolves that component-property reference, verifies that it is
unique, and applies the canonical GitHub `main` file URL to the complete text
range. Layer names are presentation details and are not hyperlink selectors.

The reserved component hierarchy is:

| Owner | Direct slot container | Component | Exposed slots |
|-------|-----------------------|-----------|---------------|
| `.ci node` | `execution plan` | `.ci phase` (`64668:2944`) | `phase 01` through `phase 08` |
| `.ci phase` | `steps` | `.ci step` (`64665:2991`) | `step 01` through `step 08` |
| `.ci node` | `outcome` | `.ci outcome` (`64669:3118`) | `outcome 01` through `outcome 04` |

The corresponding limits are 8 phases per node, 8 steps per phase, and 4
outcomes per node. Masters keep every slot visible; generated instances reveal
populated slots and hide the remainder. The writer resolves the parent's exposed
instances, while preflight requires each slot to be a direct child of its named
container. Neither path creates ad hoc siblings or depends on unstable nested
sublayer IDs.

The writer resolves a slot family only when its owning block is visible:
non-empty `phases` enable phase traversal, non-empty `outcomes` enable outcome
traversal, and a phase with non-empty `steps` enables step traversal. Figma can
return no exposed instances after the corresponding container property is set
to false, so empty typed arrays must not trigger slot validation or mutation.

The `.ci phase` master exposes `show steps=true`, and its direct `steps` frame
binds `visible` to that BOOLEAN property. The writer derives the instance value
from `phase.steps.length > 0`. This container-level visibility is required in
addition to hiding unused slots because a visible Auto Layout frame with no
visible children can retain its master height instead of collapsing.

Masters also keep every property-backed field and optional section visible in
every variant. Their visibility booleans default to `true` so the component
documents its complete public surface. The writer sets visibility explicitly on
generated instances from model data; it must not rely on hidden master defaults.

`.ci phase` requires `order`, `title`, `technical id`, `description`,
`show technical id`, `show description`, and `show steps`. `.ci step` requires
the same text and field visibility properties plus `tasks`, `condition`, `show
condition`, and exact `role` variants `action`, `decision`, and `group`. `.ci
outcome` uses the step-shaped display properties with exact `kind` variants
`artifact` and `check`. There is no `level` property: hierarchy is encoded by
component ownership. Preflight validates all properties, visibility bindings,
variants, slot names, exposure flags, and main component identities before
mutation.

The three `.ci step` variants use compact horizontal Auto Layout, a leading
order badge, the Gradle icon, one flexible content column, and no outer stroke.
The condition row is borderless. A TeamCity build step maps to `.ci phase`;
Gradle tasks and selection boundaries map to `.ci step`; published artifacts
and checks map to `.ci outcome`.

The `role=group` variant owns exactly one `tasks` text layer bound to the public
`tasks` property. It uses `LEFT` alignment and `HEIGHT` text resize. The writer
splits the exact identifiers stored in `technicalId` at the documented middle
dot separator and writes one literal `• ` bullet per line. The master default
contains at least two bulleted lines. Do not replace those literal bullets with
native Figma list range styling: overriding a component `TEXT` value resets the
range formatting and removes the visual list from generated instances.

`.ci phase` is a transparent section, not a filled card around its child rows.
Its header uses `md/sys/color/primary-container` and 8 px padding. Its
transparent `steps` frame uses 16 px left padding and 8 px row spacing.
`.ci step` uses `md/sys/color/surface-container-highest` with 4 px padding, and
`.ci outcome` uses `md/sys/color/secondary-container` with 6 px padding.
Preserve these token and spacing boundaries so TeamCity phases, Gradle detail,
and published results remain distinguishable without nested card surfaces.

The pull request plan collapses `ci.plan.gradleTasks` into a decision plus the
groups `Always`, `According to changes`, and `Full verification`. Exact task
identifiers remain in `technicalId`, but mutually exclusive selections and
root-check dependencies no longer appear as repeated linear executions. Literal
commands and arguments remain available through the node's `source` link to
GitHub `main`.

Every managed `.ci node group` contains only its top-level `.ci node` instance.
The node orders summary first, the `execution plan` frame second, the `outcome`
frame third, and optional runtime and source details last. The execution frame
owns its heading and exposed phase slots, the `outcome` frame owns its exposed
outcome slots, and each phase's `steps` frame owns its exposed step slots.
`show execution plan` collapses that complete frame when no phases exist,
`show outcome` collapses the complete result frame when no outcomes exist, and
`show optional details` collapses the details container when neither child is
visible. The `.ci node` root owns exactly one visible 2 px inside stroke bound
to `md/sys/color/outline`. The summary and outer details frames stretch to the
width established by the visible execution content, so compact nodes remain
narrow and task nodes receive a matching full-width header. The outer details
frame owns the runtime divider but no additional stroke; the nested `details
content` frame preserves the compact reading width. Action descriptions remain
in the typed model but are hidden visually; phase, decision, group, and outcome
descriptions remain visible.

Native Figma CI connectors are cloned from the locked
`simple-line_arrow / neutral` connector at node `64835:3289` because the MCP
runtime does not expose
`figma.createConnector()`. The visual preflight resolves that exact node and
requires its configured name, elbowed line type, arrow-lines end cap, and
native text before any CI section is mutated. The locked template is positioned
over the CI component documentation section but remains a page-level node,
outside every managed CI target, because standalone connector endpoints cannot
be parented to that section by the MCP runtime. This prevents target cleanup
from deleting the template. Its identity is independent from the
`simple-solid_arrow` template used by catalog trees.

Each clone remains a child of the target section, is inserted behind its node
groups, and stores the connection label in the connector's native text. The
writer preserves the template font and uses `Inter Medium` only when the clone
does not expose a usable font. `.ci connector label` groups, background
rectangles, and independently positioned label text are not part of the
supported contract.

The Kotlin plan assigns every connection a semantic kind. The writer applies
the following direct stroke colors; generated connectors do not bind these
colors to Figma variables. The `CI connector colors` collection and the legend
in the component documentation area mirror this palette for readers, but they
are not runtime dependencies of the sync.

| Kind | Meaning | Direct stroke |
|------|---------|---------------|
| `control` | Trigger, command, scheduling, gate, or rerun flow | `#0067C0` |
| `data` | Source, artifact, model, metadata, or visual payload | `#7A3E9D` |
| `status` | Check, result, or status publication | `#2E7D32` |
| `attention` | Mismatch or operator action required | `#C62828` |
| `neutral` | Reserved contextual relation with no stronger semantic kind | `#6B7280` |

Color never replaces the native connection label. Keep both the label and kind
stable in the plan so the visual meaning survives every granular or canonical
rerun.

`ConnectorText` does not expose a usable width in the Plugin API. Before row
layout, the writer measures each label with a temporary `TextNode` using the
same font and font size, removes that node immediately, and reserves the
measured width plus horizontal clearance. Do not read `connector.text.width`:
an undefined measurement collapses the gap and can send an elbowed connector
thousands of pixels outside its row.

The reserved width applies to adjacent nodes in the same visual row for both
`horizontal` and `grid` sections. A grid must not fall back to the default
column gap when a native connector label is wider; Figma may otherwise route
the elbow around the complete section instead of through the intended gap.

`Overview`, `Pull Request Integration`, and `Post-merge Design Documentation`
use a left-to-right flow. `Infrastructure and Access` keeps its two-dimensional
topology grid. `Windows Service Runtime` is a connector-free grid whose service
nodes share one row. Horizontal flows connect from the side anchors of their node
groups and vertically align node centers. Their inter-node gap grows when
necessary so the native connector text fits on the horizontal route.
Figma can apply exposed boolean visibility properties after `setProperties`
returns, which changes instance height after hidden blocks collapse. The writer
must wait until managed node-group dimensions are stable before laying out a
row, bind connector endpoints only after that layout, and then wait for
connector geometry to stabilize before resizing the section. Calculating the
route from the initial component dimensions can top-align mixed-height nodes
and send connectors through their content.
The `.ci node` master groups its eight phase slots into two responsive rows of
four. Each reserved `.ci phase` instance uses vertical Hug sizing. After the
writer populates and hides phase slots, it also hides any row with no visible
slot; otherwise an empty nested Auto Layout row can preserve the master size
inside a component instance even though every child slot is hidden.

Figma may replace exposed nested-instance proxies after a component property
mutation, while `exposedInstances` can omit slots that have become hidden. The
writer therefore captures each slot's owning row before changing phase or step
properties, then derives final row visibility from the ordered phase count in
the model. Responsive layout decisions must not inspect a pre-mutation slot
proxy or try to rediscover hidden slots afterward.

Disconnected horizontal flows are stacked as separate rows and each row starts
at the same left edge. Post-merge job order is derived from declared artifact
publication and job dependencies, never from the order of jobs in the generated
TeamCity model.
Connections within one logical row use side anchors. Connections whose model
positions occupy different logical rows use vertical anchors: `BOTTOM` to
`TOP` for a downward relation and `TOP` to `BOTTOM` for an upward relation.
Routing must use those model positions rather than comparing rendered centers:
a tall target can overlap both rendered row bands and otherwise make Figma send
the elbow through an intermediate node.
This routes the elbow through inter-row whitespace instead of across an
intermediate node while the connector keeps its native label centered. Return
connections within one row use bottom anchors and route below the row. In the
topology grid, parallel opposite vertical connections route around opposing
left and right sides. Neither relation uses the central vertical lane, so their
native labels cannot collapse into the same inter-row band.
Distinct connections that share the same endpoints must remain visually
distinct. Route horizontal parallel connections above and below their nodes;
route opposite vertical connections around opposing sides.

The selected side is a routing lane, not a request to reuse the side's central
magnet. After managed node groups reach their final size and position, the
writer creates one transparent 2 px `.ci connector port` group for every
connection endpoint. Each port remains on the owning node outline, and the
native connector binds to that port instead of binding directly to the node
group. For endpoints that share a node side, order ports by the rendered center
of the opposite node along that side's axis; use the connection id as the
stable tie-breaker. Spread the ordered ports evenly between the two corners.
This keeps converging, diverging, and parallel routes distinguishable while
preserving the selected top, right, bottom, or left lane.

Ports are generated writer state, not Figma components and not Kotlin plan
coordinates. Keep them visible to the Plugin API but without fills or strokes,
mark them as managed `connector-port` nodes, and place them behind the native
connectors and `.ci node` groups. A granular rerun removes native connectors
first, then their ports, then node groups. Do not bind a new connector before
layout is stable or reuse one port for multiple endpoints: either shortcut
reintroduces ambiguous entry and exit points.

The writer reserves the measured native connector-label width plus explicit
clearance on both sides. A connector whose route reaches a node outline may do
so, but its label must not visually touch or cover the node.
The icon shown in a `.ci node` header is exactly one nested `.ci icon` instance
from component set `64361:716`. Its `environment` variant is configured directly
on the nested instance because Figma does not promote that property to the
parent `.ci node` component. Supported environments are `github`, `teamcity`,
`cloudflare`, `figma`, `codex`, `browser`, `terminal`, `operator`, `json`, and
`gradle`. Following the
[Gradle branding guidelines](https://iu34kfdu.gradle.com/brand/), the Gradle
variant uses the standalone elephant at its official `#02303A` color. Keep its
proportions, orientation, composition, and color unchanged. The Kotlin visual
plan maps every node explicitly; there is no generic fallback. A TeamCity job
remains `teamcity` even when its nested steps invoke Gradle; `gradle` is reserved
for a node whose complete scope represents the Gradle Build Tool. The preflight
must fail when the nested instance is absent or duplicated, belongs to another
component set, lacks the `environment` property, or exposes a different set of
variant values.
The `.ci node` component also exposes `runtime platform`, `runtime service`,
`runtime startup`, `runtime identity`, and `show runtime`. The writer sets all
four text properties and enables the runtime block only when the visual node
contains complete runtime data. The preflight requires these properties even
for targets whose instances keep the runtime block hidden.
Text alignment is owned by `.ci node`, `.Header`, and the connector template;
the writer does not override sublayer alignment because the MCP text proxy does
not expose that style mutation consistently.
The CI parent section explicitly uses the `Light` mode from the collection that
owns `md/sys/color/surface`. Its bound fill also carries the resolved Light
color as fallback so exports and MCP screenshots do not render the section as a
black surface when variable resolution is unavailable.
The writer derives labels from triggers and connection purposes rather than
using generic continuation text.

Child CI sections have no fill and use the standard outline stroke contract.
They are stacked with 114 px between sections. The parent owns the only direct
`.Header`, uses the surface fill and 28 px corner radius, has no stroke, and is
the only node locked after synchronization. A granular rerun removes and
recreates only content marked as managed inside the requested child section;
other CI child sections remain untouched.

After stacking, resize the parent from the visible child section bounds plus
the standard padding. The existing `.Header` width must not participate in
that calculation because it can retain a stale width from an earlier layout.
Once the parent size is known, resize the direct `.Header` to the computed
parent width. Parent height still includes the header when no child section
extends below it.

Preflight for a CI target validates the destination page, the `.ci node`
component properties, and all required `ci/cd` modes before visual mutation.
CI model fixtures must remain complete even when a test generates only one
target-scoped visual plan: the planner validates the aggregate CI contract,
including the authoritative `TeamCity CI` published check, before it filters
the requested target.

## Version Visual Sync

The version sync reads `content.versionSections` from `design-model.json` and
uses the Figma variable collection named
`repo\dependency-catalog\versions.properties`.

For each repository version:

- Find an existing variable whose name is either the version key or ends with
  `/<versionKey>`, such as `Libraries/kotestLibraryVersion`.
- Keep the repository version sections semantically named:
  `Main project dependencies`, `Libraries`, and `Plugins`.
- Map those semantic sections to the visual subsections named `main versions`,
  `library versions`, and `plugin versions` in Figma. These subsection node ids
  are part of `WaterMyPlantsFigmaWriterProjectConfig.kt`; update them whenever
  the `.dependency version` component area is rebuilt manually. The version
  visual containers may be
  Figma `SECTION` nodes or `FRAME` nodes as long as they own the direct
  `.dependency version` instances.
- Keep `androidGradlePluginVersion` and `kotlinVersion` in
  `Main project dependencies`. These keys may be referenced by plugin catalog
  nodes, but their source version section remains the main project section.
- Name library-owned version keys with the `LibraryVersion` suffix.
- Name plugin-owned version keys with the `PluginVersion` suffix.
- `checkFigmaVersionNaming` enforces this naming contract in CI through the
  root Gradle `check` lifecycle. See
  [dependency-version-naming.md](../standards/dependency-version-naming.md).
- Ensure `Version alias` mode equals the version key.
- Set `Version number` mode to the repository value.
- Create a missing Figma variable under the matching section folder.
- Create a missing `.dependency version` instance in the matching visual frame.
- Layout `.dependency version` instances as a row-major grid with at most two
  instances per row, 64 px between columns, and 32 px between rows. Start the
  grid 100 px from the visual section's top and left edges, then resize the
  section after layout so all four edges keep the same 100 px padding.
- Stack the visual frames for `Main project dependencies`, `Libraries`, and
  `Plugins` with 128 px between one frame bottom edge and the next frame top
  edge.
- Remove stale `.dependency version` instances whose alias is no longer present
  in that version section, such as old keys left behind after renaming a library
  version to the `LibraryVersion` suffix.
- Keep at most one `.dependency version` instance for each expected version key.
- Bind both component properties to the created or existing variable.
- Do not edit visual text nodes directly. `.dependency version` instances update
  through variable bindings.

The sync must fail without writing metadata if the design model contains an
unknown version section.

Every Figma `SECTION` managed or created by the visual sync must use a single
stroke bound to `md/sys/color/outline`, aligned `INSIDE`, with weight `2`,
unless it directly contains a `.Header`. Parent documentation sections with a
direct `.Header` must have no stroke. The sync reapplies this structural
contract to existing catalog and version sections so legacy or manually
changed strokes are normalized on the next targeted sync.

Direct child sections are positioned 100 px from their parent section's left
edge before the parent is resized to fit. This keeps the left padding equal to
the 100 px right and bottom padding added by the resize operation, including the
two Water My Plants catalog trees inside `Water My Plants version catalogs`.

The `Gradle convention plugins` and `Gradle plugins` inventories are not child
sections of that catalog container. They are independent top-level sections on
the Gradle dependencies page, each with its own direct `.Header`. Their node ids
must be present in `PARENT_SECTION_NODE_IDS`; the writer then positions them
horizontally beside the other managed top-level documentation sections using
`PARENT_SECTION_SIBLING_GAP`.

## Catalog Tree Sync

The catalog tree sync reads `content.catalogs` from `design-model.json` and
updates these visual sections:

- Water My Plants libraries.
- Water My Plants plugins.
- Custom Gradle convention plugins.
- Custom Gradle plugins.

Consumer modules are scoped to the catalog that owns the visual section:

- `waterMyPlants.libraries` and `waterMyPlants.plugins` represent the catalog
  generated from `repo/dependency-catalog` into the main Water My Plants build.
  Their `Used by module` chips must come only from modules in the main build,
  such as `:app`, `:core:*`, and `:onboarding:*`.
- `waterMyPlants.*` sections are stable documentation targets. If their model
  nodes are empty, the writer may hide the section, but the target remains part
  of the stable documentation surface.
- `waterMyPlants.customGradleConventionPlugins` is populated from included
  builds explicitly marked as convention-plugin publishers. In this repository
  that source is `repo/gradle-plugins`.
- `waterMyPlants.customGradlePlugins` is populated from regular Gradle plugin
  declarations in the repository and excludes convention-plugin implementation
  classes.
- Included-build library and plugin catalogs are not Water My Plants visual
  targets. Their aliases may inform model usage, but their private catalog trees
  must not be rendered beside the product catalog.

Do not merge consumers across catalogs just because the same plugin id,
artifact coordinate, or version alias appears in more than one catalog. When an
included build declares its own `plugins` catalog, plugin aliases applied by
that included build belong to that included-build target, not to
`waterMyPlants.plugins`.

For each section:

- Match existing tree nodes by label inside the section. Labels must be unique
  per section until stable Figma path metadata is introduced.
- Update exposed component properties for library groups, plugin ids, plugin
  versions, and artifact visibility.
- Update existing library artifact name/version text overrides when the
  instance structure can represent the model.
- After preserving an existing root position during a root-scoped sync, shift
  the complete subtree when necessary so its leftmost group remains at least
  100 px inside the owning root section. A wider regenerated descendant must
  not extend outside its section.
- When a `.tree node` contains an `artifacts` frame, select its direct
  `.artifact` / `.artifacts bundle` children first, including hidden template
  slots that can be made visible. Do not let visible nested `.artifact` rows
  inside a `.artifacts bundle` win over direct hidden slots.
- Bind direct catalog artifact entries to direct `.artifact` slots only. Bind
  artifacts declared inside a bundle only to child `.artifact` rows inside the
  corresponding `.artifacts bundle`; they must not consume sibling `.artifact`
  slots in the `.tree node` `artifacts` frame.
- Update `Used by module` instances for library artifacts from
  `requiredByModules` plus the modules listed by each
  `providedByConventionPlugins.requiredByModules` entry.
- Library artifacts and bundles may also carry `providedByConventionPlugins`.
  Each usage has `pluginId`, `pluginModule`, and `requiredByModules`. It is the
  model source for `Applied by plugin` `.usage block type=applied-by-plugin`
  rows, even when `requiredByModules` is empty because no module currently
  applies that convention plugin.
- Plugin catalog entries may also carry `providedByConventionPlugins`. Each
  usage has `pluginId`, `pluginModule`, and `requiredByModules`. It is the
  model source for `Used by convention plugin` `.usage block
  type=used-by-convention-plugin` rows on the `Plugin` `.tree node` variant,
  even when no module currently applies that convention plugin.
- Library artifacts may also carry `configuredByConventionPlugins`. Each usage
  has `pluginId`, `pluginModule`, and `target`; it means the convention plugin
  uses the artifact as build tooling configuration, not that it provides the
  artifact to production modules. Render these rows under the visible
  `Tool artifacts` section with `.tool artifact usage`, not a plain
  `.usage chip`.
- Hide `Applied by plugin`, `Used by module`, and `Tool artifacts` blocks when
  their source lists are empty. Do not render empty headings or empty chip
  containers.
- Keep structural `separator` frames inside `.artifact` and `.artifacts bundle`
  visible. Only `usage separator` frames, or separators bound to a usage
  boolean through `componentPropertyReferences.visible`, are conditional on
  usage content.
- Control each usage block through its own component boolean on `.artifact` and
  `.artifacts bundle`: `Show applied by plugin`, `Show used by module`,
  and `Show configured as tool` where the component supports tooling rows.
  `Show configured as tool` controls the visible `Tool artifacts` section; it
  is not the required heading text. `.artifact` and `.artifacts bundle` do not
  expose an unused-entry boolean. Do not use one aggregate boolean to show
  multiple usage blocks.
- `.artifact` and `.artifacts bundle` must not expose the legacy aggregate
  `Show consumer modules` property. Their usage block visibility is derived
  only from model data and the granular booleans above.
- Direct artifact entries and bundles with no direct `requiredByModules`, no
  `providedByConventionPlugins`, and no `configuredByConventionPlugins` are
  invalid catalog data. `checkFigmaCatalogUsage` must fail in CI before the
  model can be merged, so `.artifact` and `.artifacts bundle` must not render
  or expose an unused-entry block.
- Update `Used by module` instances for `.artifacts bundle` entries from
  the bundle `requiredByModules` plus the modules listed by each
  `providedByConventionPlugins.requiredByModules` entry. Child `.artifact`
  instances inside a bundle must not show their own `Used by module` section.
- Update `Applied by module` instances for plugin tree nodes from
  `appliedToModules` plus the modules listed by each
  `providedByConventionPlugins.requiredByModules` entry.
- For plugin tree nodes, hide `Applied by module` and
  `Used by convention plugin` when their source lists are empty. Plugin catalog
  entries with no `appliedToModules` and no `providedByConventionPlugins` are
  invalid catalog data and must be rejected by `checkFigmaCatalogUsage`.
- The portable writer may support a host-configured custom Gradle plugin
  inventory. Those leaves are not dependency catalog entries and may use the
  static warning block `No module applies it` instead of `Unused catalog
  entry`. Water My Plants configures separate inventories for convention
  plugins and regular Gradle plugins; neither inventory is an included-build
  dependency catalog.
- Control plugin usage blocks through their own component boolean on `.tree
  node` `Plugin`: `Show applied by module`,
  `Show used by convention plugin`, and
  `Show unused` for the custom-plugin warning block. Do not expose
  the legacy aggregate `Show consumer module` property.
- In `.tree node` `Plugin`, usage blocks are direct children of the component:
  `.usage block type=applied-by-module`, `.usage block
  type=used-by-convention-plugin`, plus a static `No module applies it` status
  block for the custom-plugin warning, each controlled by its own boolean. Do
  not require a wrapper frame named `content`.
- Parent components must expose every usage section in their template. The sync
  decides visibility on each generated instance from the model data by setting
  the granular boolean and the direct block visibility for that section.
- Represent usage metadata with `.usage chip` instances. The custom-plugin
  warning block is not usage metadata and must not render `.usage chip`
  instances.
- Represent shared usage sections with `.usage block`. The supported `type`
  variants are `applied-by-plugin`, `used-by-module`,
  `used-by-convention-plugin`, and `applied-by-module`.
- `.usage chip` exposes only two `kind` variants: `module` for modules that
  require or apply an item, and `convention-plugin` for Gradle convention
  plugins that provide dependencies to production modules or configure tooling
  artifacts.
- `.usage chip` exposes `name` as a text component property bound to the label.
  Do not add a variant for every module, plugin, artifact, or dependency name.
- `.tool artifact usage` represents one `configuredByConventionPlugins` entry.
  Its direct text property `tool artifact target` receives `target`; its nested
  `.usage chip` receives `kind=convention-plugin` and `name=pluginId`.
- Render the custom-plugin warning as a static status block: the container fill
  is `md/sys/color/error-container`, the label fill is
  `md/sys/color/on-error-container`, and the `.tree node` `Plugin` variant must
  not contain `.usage chip` instances inside that block. The writer accepts
  `No module applies it`; legacy `Not used by module` and
  `Unused catalog entry` headings are accepted only to clean up sections still
  being migrated.
- If a declared catalog target contains no model nodes, remove that target
  section and resize/re-stack its parent sections. For example, an included
  build catalog explicitly selected by another host must not leave an empty
  section in Figma. Water My Plants selects no included-build catalog targets.
  Stable documentation targets with no model nodes are hidden instead.
- Create missing `.tree node` instances by cloning a compatible existing node
  from the same visual section, then applying generated model values.
- Create missing top-level tree sections when a new top-level library group or
  plugin id appears.
- Remove stale catalog tree nodes and their connectors when they no longer
  exist in the generated model.
- Treat removed Figma nodes as immediately invalid. When removing stale
  connectors, record connector ids before `connector.remove()` and keep the
  remaining connector list by id. Do not inspect connector endpoints or shared
  plugin data after removal.
- Fail without writing metadata if an existing or cloned instance cannot
  represent artifact text or consumer module structure from the generated model.
- Child library/plugin catalog sections must not have fill. Every configured
  parent documentation section keeps exactly one visible solid fill bound to
  `md/sys/color/surface`; preflight rejects a missing, duplicated, hidden, or
  unbound fill. Every parent also keeps corner radius `28`, which preflight
  validates from the typed project configuration. This includes the
  independent top-level plugin inventories
  `64886:247` and `64886:248`, even though the writer also synchronizes catalog
  tree nodes directly inside them. Child catalog sections keep their geometry
  and outline stroke but use an empty `fills` array.

## Tree Node Component

Use `.tree node` component:

- Figma URL:
  `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63069-678`
- Use the `Library` variant for libraries.
- Use the `Plugin` variant for plugins.
- The `Plugin` variant exposes granular usage booleans:
  `Show applied by module`, `Show used by convention plugin`, and
  `Show unused`.
- The `Plugin` variant must not expose the legacy aggregate
  `Show consumer module` switch. Visual correctness comes from the granular
  booleans and the `.usage block` visibility.
- The plugin component property for marking repository Gradle plugin nodes is
  `Show is a gradle plugin#63112:4`.
- The Gradle plugin badge frame and its separator are visible only when
  `Show is a gradle plugin` is true.
- The older property name
  `Show is a gradle convention plugin#63112:4` is stale and must not be used.

## Connector Contract

Tree connectors represent parent/child `.tree node` edges. The valid structure
is group-based:

- Each `.tree node` instance is wrapped in a `.tree node group`.
- The `.tree node group` is the node moved by layout.
- The `.tree node` instance is the only child of its `.tree node group`.
- The `.tree node group` carries shared plugin data key `treeNodeGroupNode`
  pointing to the contained `.tree node` id.
- Each `simple-solid_arrow` connector carries shared plugin data key
  `treeConnectorEdge` for the represented parent/child `.tree node` ids.
- Connectors are children of the visual tree section, not of the Figma page.
- Connector endpoints point to `.tree node group` nodes:
  `connectorStart = parentGroup.BOTTOM` and
  `connectorEnd = childGroup.TOP`.
- `simple-solid_arrow` connectors with `treeConnectorEdge` are managed sync
  state. Cleanup, stale-node removal, and connector reconciliation must identify
  them by metadata and id, not by visual position alone.
- A newly created catalog target with parent/child edges must retain one hidden
  local `simple-solid_arrow` carrying
  `treeConnectorEdge=__template__->__template__`. It is the clone source before
  the target has any generated connector and is not a model edge.

Do not bind connectors directly to `.tree node` instances. Figma rejects that
endpoint shape with:

```text
Connecting to this node type is not supported
```

If group endpoint binding fails, positional connector endpoints are allowed only
as a fallback. Their positions must be computed from absolute page bounds of the
parent and child nodes, even when the connector itself is a child of the tree
section. Keep `treeConnectorEdge` on every positional connector so cleanup and
later resyncs can identify the represented edge.

## Layout Contract

The sync must re-layout tree nodes before the final connector sync so connectors
follow moved nodes.

Layout rules:

- Move `.tree node group` nodes, not the `.tree node` instance inside them.
- Center each parent horizontally over its children.
- When a touched `.tree node` changes width because usage blocks, status
  blocks, or labels are shown/hidden, preserve the `.tree node group`
  horizontal center before syncing connectors.
- Use 128 px between a parent bottom edge and its child top edge.
- A parent with a single child should be centered directly above that child so
  the connector is vertical.
- Resize every touched tree section and parent section to fit after visual
  updates.
- Stack direct child sections inside touched section containers with 114 px
  between one section bottom edge and the next section top edge. Apply the same
  spacing to ancestor section containers after their children are resized.
- Reflowed direct child sections must start at the container padding, or below
  the direct `.Header` plus the section gap when the container has a header.
  Do not preserve the first child section's previous `y` after deleting an
  earlier sibling; otherwise removed roots leave a stale top gap.
- Direct child sections stacked inside the same container must share the same
  left edge. This keeps module sections such as `gradle-plugins` and
  `figma-documentation-sync` horizontally aligned when they belong to the same parent
  package section.
- Top-level parent documentation sections on the Gradle dependencies page must
  keep 1139 px of horizontal space between one section right edge and the next
  section left edge.
- Direct `.Header` instances in touched sections must span section width. If a
  section is narrower than the header's Hug width, resize the section first so
  the fixed-width header can show its content without clipping.
- The `headers` target owns parent documentation links. When one header lists
  multiple sources, render one source path per line and assign each path its
  own hyperlink range. Keep the `Link` text horizontally aligned `LEFT`; do not
  inherit centered alignment from the component or bind the complete text to
  only the first URL. Canonical sources are:
  - `Gradle dependency visualization components`: `LibraryTreeDsl.kt`,
    `LibraryScope.kt`, and `PluginTreeDsl.kt` under
    `repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/`.
  - `Project versions`: `repo/water-my-plants-project-config/versions.properties`.
  - `Water My Plants version catalogs`: `LibraryTrees.kt` and `PluginTrees.kt`
    under `repo/water-my-plants-project-config/catalog/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/catalog/`.
  - `Gradle convention plugins`: the `repo/gradle-plugins` directory.
  - `Gradle plugins`: the regular plugin implementations under
    `repo/dependency-catalog`, `repo/figma-documentation-sync`,
    `repo/verification-platform`, and `repo/water-my-plants-project-config`.
- Keep explanatory prose outside generated catalog containers. In particular,
  do not recreate the removed free-standing `Not actually trees` text in the
  repository tooling catalog parent; future contextual guidance belongs in a
  dedicated documentation surface.

## Locking Contract

After visual mutation:

- Lock only the root catalog section.
- Leave descendant sections, `.tree node group` groups, `.tree node` instances,
  connectors, and component internals unlocked.

This avoids stale child locks. Figma surfaces the root section lock through its
children in the editor, so locking descendants makes later MCP syncs and manual
inspections harder.

## UML Documentation Target

Module dependency graphs are architecture documentation. Represent them in
PlantUML diagrams under the repository UML convention and publish them to the
Figma UML documentation page:

- Page:
  `https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63308-2386`
- Example source:
  `repo/figma-documentation-sync/docs/uml/diagrams/architecture.puml`
- Rendered upload artifact: generated `.svg` for each `.puml`.
- Module grouping section: `figmaDocumentationSync`.
- Diagram section name: the full `.puml` file name, for example
  `architecture.puml`.
- Diagram section style: no fill, `md/sys/color/outline` stroke, stroke align
  `INSIDE`, stroke weight `2`.
- Visual structure: each generated SVG is imported into its own diagram section
  and flattened so only the imported `Group` remains inside the section.
- `.Header` links: the visible `Link` property must list relevant repository
  files and each filename must hyperlink to its GitHub `main` branch URL.

## Future Improvement

The next visual sync phase is to introduce stable Figma path metadata so
duplicate labels can be reconciled safely.
