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
connectors. If a stale sibling needs cleanup, run that sibling root or the full
target explicitly.

Partial root sync is a repair/execution granularity, not a completion signal.
The official metadata (`gitSha` and `modelHash`) must be written only after all
required roots and all other official targets have completed successfully.

## Version Visual Sync

The version sync reads `content.versionSections` from `design-model.json` and
uses the Figma variable collection named
`repo\dependency-catalog\versions.properties`.

For each repository version:

- Find an existing variable whose name is either the version key or ends with
  `/<versionKey>`, such as `Libraries/kotestVersion`.
- Ensure `Version alias` mode equals the version key.
- Set `Version number` mode to the repository value.
- Create a missing Figma variable under the matching section folder.
- Create a missing `.project version` instance in the matching visual frame.
- Bind both component properties to the created or existing variable.
- Do not edit visual text nodes directly. `.project version` instances update
  through variable bindings.

The sync must fail without writing metadata if the design model contains an
unknown version section.

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
