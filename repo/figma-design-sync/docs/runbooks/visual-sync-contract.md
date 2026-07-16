# Figma Visual Sync Contract

## Purpose

This document captures the stable visual contract used by the MCP sync tooling.
The execution flow lives in [trunk-sync.md](trunk-sync.md); this file describes
what the generated Figma state must look like after the sync.

## Tooling Boundary

The visual sync source of truth is TypeScript under
`repo/figma-design-sync/tools/src/`.

The tool source follows the same dependency direction as the Gradle sync code:

- `domain` contains generated-model types and pure catalog rules.
- `ports` contains gateway contracts.
- `usecases` coordinates sync behavior through those contracts.
- `figma` contains the Figma MCP API adapters.
- `app` wires concrete gateways for the generated MCP entrypoint.

The generated JavaScript bundle is a temporary MCP runtime artifact and must not
be committed.

Preview runners may pass `sectionNodeOverrides` so a catalog target mutates a
sandbox section instead of the configured official section. Official sync runs
must use the configured section ids from `figma-config.ts` unless a documented
manual repair explicitly overrides one target.

The `preflight` target validates the visual contract without mutating Figma.
Run it after staging the official model and before visual targets when component
contracts changed. It checks the metadata page, version variable collection,
version section frames, `.tree node` component properties, `.artifact` and
`.artifacts bundle` template properties, `.tool artifact usage`, `.usage chip`
variants, configured catalog sections, catalog target model shape, and root
filters. A successful preflight must return `mutatedNodeIds: []`.

For library catalog items, preflight validates `.artifact`, `.artifacts bundle`,
and `.tool artifact usage` templates from the target section when present,
falling back to the base `.tree node` component only when the section has no
template. This matches the writer behavior: new nodes clone compatible section
instances before falling back to the base component variant.

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
The official metadata (`gitSha` and `modelHash`) must be written only after all
required roots and all other official targets have completed successfully.

## CI Documentation Visual Sync

The CI writer reads only `content.ci` from the official `main`
`design-model.json`. It creates or updates one parent section named
`Continuous Integration and Design Documentation` on Figma page `63153:2876`
and exposes four independently runnable targets:

- `ci.overview`;
- `ci.pullRequestIntegration`;
- `ci.postMergeDesignDocumentation`;
- `ci.infrastructureAndAccess`.

Each visual entity is an instance of `.ci node` (`64301:3927`). The writer
selects the matching mode from the `ci/cd` variable collection: `Actor`,
`System`, `Git reference`, `Pipeline`, `Job`, `Artifact`, `Check`, or `Gate`.
The collection's `type_label` string supplies the visible type label for each
mode.
It binds the exposed `name`, `description`, `steps`, and `source` text
properties and controls `show steps` and `show source` from actual model
content. Commands are summarized for display; the `source` row links to the
canonical file on GitHub `main`, where the literal DSL remains available.

Every `.ci node` instance is the only child of a managed group. Native Figma
connectors are cloned from the existing `simple-solid_arrow` template because
the MCP runtime does not expose `figma.createConnector()`. The clones attach to
the managed node groups using the magnets selected for the section layout,
remain children of the target section, and are inserted behind nodes.
Cloned connector text may initially expose an empty font name; the writer uses
the design file's `Poppins Regular` connector font as the explicit fallback
before clearing the native connector text. Connector labels are managed groups
named `.ci connector label`, composed of a surface background and horizontal
text, and placed between connected node groups. Do not use native connector
text for CI labels because Figma rotates it with vertical and elbowed connector
paths.
Inside every label group, `Background` must be the bottom layer and `Label`
must be the top layer. The background is opaque, so reversing this order keeps
the text in the document but makes it disappear visually.
`Overview`, `Pull Request Integration`, and `Post-merge Design Documentation`
use a left-to-right flow. `Infrastructure and Access` keeps its two-dimensional
topology grid. Horizontal flows connect from the side anchors of their node
groups and vertically align node centers. Their inter-node gap grows when
necessary so the connector label fits centered on the horizontal connector.
Disconnected horizontal flows are stacked as separate rows and each row starts
at the same left edge. Post-merge job order is derived from declared artifact
publication and job dependencies, never from the order of jobs in the generated
TeamCity model.
Return connections use bottom anchors and route below the row instead of
crossing intermediate nodes. In the topology grid, parallel opposite vertical
connections keep the forward path direct and route the return path around the
left side so its label cannot obscure the forward path or adjacent connectors.
Label text wraps when it exceeds 280 px, and label placement must avoid every
`.ci node group` and previously placed connector label. If no direct gap is
available, search additional positions outside the connected nodes instead of
covering a node or another label.
Distinct connections that share the same endpoints must remain visually
distinct. Route horizontal parallel connections above and below their nodes;
for opposite vertical connections, keep the forward path direct and route the
return path around one side.
The icon shown in a `.ci node` header is exactly one nested `.ci icon` instance
from component set `64361:716`. Its `environment` variant is configured directly
on the nested instance because Figma does not promote that property to the
parent `.ci node` component. Supported environments are `github`, `teamcity`,
`cloudflare`, `figma`, `codex`, `browser`, `terminal`, `operator`, and `json`.
The visual plan maps every node explicitly; there is no generic fallback. The
preflight must fail when the nested instance is absent or duplicated, belongs
to another component set, lacks the `environment` property, or exposes a
different set of variant values.
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

Preflight for a CI target validates the destination page, the `.ci node`
component properties, and all required `ci/cd` modes before visual mutation.

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
  are part of `figma-config.ts`; update them whenever the `.dependency version`
  component area is rebuilt manually. The version visual containers may be
  Figma `SECTION` nodes or `FRAME` nodes as long as they own the direct
  `.dependency version` instances.
- Keep `androidGradlePluginVersion` and `kotlinVersion` in
  `Main project dependencies`. These keys may be referenced by plugin catalog
  nodes, but their source version section remains the main project section.
- Name library-owned version keys with the `LibraryVersion` suffix.
- Name plugin-owned version keys with the `PluginVersion` suffix.
- `checkFigmaVersionNaming` enforces this naming contract in CI through the
  root Gradle `check` lifecycle. See
  [dependency-version-naming.md](dependency-version-naming.md).
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
`gradle-plugins` and `figma-design-sync` sections inside the repository tooling
catalog container.

## Catalog Tree Sync

The catalog tree sync reads `content.catalogs` from `design-model.json` and
updates these visual sections:

- Water My Plants libraries.
- Water My Plants plugins.
- Custom Gradle convention plugins.
- Custom Gradle plugins.
- `gradle-plugins` libraries.
- `gradle-plugins` plugins.
- `figma-design-sync` libraries.
- `figma-design-sync` plugins.

Consumer modules are scoped to the catalog that owns the visual section:

- `waterMyPlants.libraries` and `waterMyPlants.plugins` represent the catalog
  generated from `repo/dependency-catalog` into the main Water My Plants build.
  Their `Used by module` chips must come only from modules in the main build,
  such as `:app`, `:core:*`, and `:onboarding:*`.
- `waterMyPlants.*` sections are stable documentation targets. If their model
  nodes are empty, the writer may hide the section, but the target remains part
  of the stable documentation surface.
- `gradlePlugins.*` sections are declared catalog targets from catalogs declared
  by `repo/gradle-plugins/settings.gradle.kts`. Their consumers may be
  `:gradle-plugins:*` modules.
- `figmaDesignSync.*` sections are declared catalog targets from catalogs
  declared by `repo/figma-design-sync/settings.gradle.kts`. Their consumers may
  be `:figma-design-sync:*` modules.

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
- Leaf nodes in `waterMyPlants.customGradlePlugins` are an inventory of
  repository-owned Gradle plugins, not dependency catalog entries. They may be
  present even when no module consumes them yet. When such a leaf has no
  effective consumers, show the static warning block `No module applies it`
  instead of `Unused catalog entry`.
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
  build with no `plugins` catalog must not leave an empty
  `gradlePlugins.plugins` section in Figma. Stable documentation targets with
  no model nodes are hidden instead.
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
- Library/plugin catalog tree sections must not have fill. Only parent
  documentation sections, such as the configured Gradle dependencies parent
  section, keep the `md/sys/color/surface` fill. Child catalog sections keep
  their section geometry and stroke but use an empty `fills` array.

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
  `figma-design-sync` horizontally aligned when they belong to the same parent
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
  - `Project versions`: `repo/dependency-catalog/versions.properties`.
  - `Water My Plants version catalogs`: `LibraryTrees.kt` and `PluginTrees.kt`
    under `repo/dependency-catalog/water-my-plants-catalog/src/main/kotlin/com/marmatsan/dependencies/`.
  - `Repository Gradle tooling version catalogs`:
    `repo/gradle-plugins/settings.gradle.kts` and
    `repo/figma-design-sync/settings.gradle.kts`.
  - `Custom Gradle convention plugins`: the `repo/gradle-plugins` directory.
  - `Custom Gradle plugins`: the regular plugin implementation at
    `repo/figma-design-sync/plugin/src/main/kotlin/com/marmatsan/figmaDesignSync/plugin/gradle/FigmaDesignSyncGradlePlugin.kt`.
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
  `repo/figma-design-sync/docs/uml/diagrams/architecture.puml`
- Rendered upload artifact: generated `.svg` for each `.puml`.
- Module grouping section: `figmaDesignSync`.
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
