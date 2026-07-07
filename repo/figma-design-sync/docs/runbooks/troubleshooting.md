# Figma Sync Troubleshooting

## Purpose

Use this document when a Figma trunk sync fails or produces visually incorrect
catalog trees. The normal execution flow lives in
[trunk-sync.md](trunk-sync.md), and the expected visual state lives in
[visual-sync-contract.md](visual-sync-contract.md).

## Metadata Safety

The sync writes metadata last on purpose. `checkFigmaTrunkSync` trusts the
metadata hash, so writing metadata before visuals are reconciled can make CI
pass while Figma is still visually stale.

A metadata-only write is acceptable only when the visual model is already known
to match `design-model.json` and the only mismatch is stale shared plugin
metadata.

If visual mutation fails, leave the old hash in Figma and let
`checkFigmaTrunkSync` fail until the visual sync can be rerun successfully.

## Payload Transport Failures

The Figma MCP `use_figma` call has a practical source-size limit near 50k
characters. The plugin runtime supports `atob`, `btoa`, and `Function`, but it
does not provide browser or Node transfer helpers such as `fetch`,
`XMLHttpRequest`, `importScripts`, `TextDecoder`, `Blob`, `Response`, or
`DecompressionStream`.

If a payload is truncated, fail before mutating Figma. One failed sync attempt
had expected encoded length `45228`, but only `44624` characters reached Figma.

For larger payloads, stage base64 chunks in temporary shared plugin data:

- Store a `runId`, expected chunk count, expected encoded length, and every
  chunk.
- In the final call, read all chunks, validate count and length, decode, run the
  script, verify the returned `modelHash`, then delete the temporary data.
- If the final call fails before execution, Figma visuals stay unchanged; only
  temporary staged chunk data may need cleanup.

Do not copy long base64 payloads manually from terminal output.

## Component Instance Shape

Figma component instances can contain hidden template internals that remain
addressable through the plugin API. Do not treat exact child counts as a stable
contract for reused instances.

For example, library artifact text updates must allow extra hidden
`artifact name` and `artifact version` text nodes and update only the expected
visible entries. The failure signature is similar to:

```text
expected 0 'artifact name' text nodes, found 8
```

## Connector Binding Failures

Cloned tree nodes and cloned connectors must be made visible before connector
endpoints are rebound. Otherwise Figma can reject connector assignment with:

```text
set_connectorStart: Connecting to this node type is not supported
```

When repairing existing connectors, avoid temporary invalid endpoint states.
Assign the child endpoint first (`connectorEnd = child.TOP`) and then assign the
parent endpoint (`connectorStart = parent.BOTTOM`) unless the current connector
orientation requires a different safe order. This prevents transient
self-connections such as `parent -> parent`, which Figma can reject even though
the final `parent -> child` edge is valid.

The supported connector target is `.tree node group`, not the `.tree node`
component instance. The group is the parent of the component instance, has the
same visual bounds as the component, and carries `treeNodeGroupNode` shared
plugin data for the contained `.tree node`.

Tree layout must move the `.tree node group`, not the component instance inside
it. Moving the group keeps connector endpoints attached while preserving the
component as the rendered visual node.

## Connectors That Jump To The Page

Do not confuse a visually aligned connector with a valid connector binding. A
connector can look correct when `connectorStart.position` and
`connectorEnd.position` are page-absolute coordinates, but its endpoint node is
still the Figma page.

That state is fragile: manually moving a connector end can reparent the
connector to the page and make it leave the visual section.

For every repaired catalog tree section, verify connector structure as well as
the screenshot:

- Every `.tree node` is the only child of a `.tree node group`.
- Every `.tree node group` carries `treeNodeGroupNode` with the contained
  `.tree node` id.
- Every `simple-solid_arrow` for that section is a child of the section, not the
  page.
- Every connector keeps `treeConnectorEdge` for the represented parent/child
  `.tree node` ids.
- Every connector endpoint points to a `.tree node group`, with
  `connectorStart.magnet = BOTTOM` and `connectorEnd.magnet = TOP`.
- Only the root catalog section is explicitly locked. The descendants are
  explicitly unlocked so future MCP syncs and manual inspections do not inherit
  stale child locks from previous repairs.

If endpoint binding still fails, the catalog tree sync falls back to positional
connectors. Positional connector coordinates are page-absolute Figma
coordinates. Do not convert them to section-local coordinates after moving a
connector into a section: that makes the line render far away from the
`.tree node` instances, often looking like it disappeared behind other layers.

Verify a touched section visually before writing metadata when connector
behavior changes.

## Missing Connector Template

Missing parent/child edges must be represented by cloned `simple-solid_arrow`
connectors. If connector creation fails, report the parent and child node ids in
the diagnostic path before writing metadata. That keeps the failed visual
relation actionable instead of reducing the problem to a generic Figma API
error.

## Missing Component Property

If the MCP execution fails with a missing component property such as:

```text
Could not find a component property with name: 'Show is a gradle convention plugin#63112:4'
```

inspect the `.tree node` component set and update `TREE_NODE_PROPS` in
`repo/figma-design-sync/tools/src/config/figma-config.ts` to match the actual
Figma component property name before rerunning the sync.

Do not work around this by writing metadata only; the visual update did not
complete. The current property name is:

```text
Show is a gradle plugin#63112:4
```

## Bundle And Model Mismatch

If a newer MCP bundle fails against a TeamCity model from `main` with a missing
catalog target such as:

```text
designModel.content.catalogs.figmaDesignSync.libraries is required for catalog tree sync.
```

the bundle and model are from different repository contracts. Rerun with a
bundle generated from the same revision as the TeamCity artifact, or restrict
`SYNC_OPTIONS.targets` to the target names present in that artifact before
writing metadata.

## Unsupported Headless TeamCity Write Path

The supported repo contract is:

- TeamCity generates and publishes `design-model.json`.
- TeamCity verifies Figma metadata with `checkFigmaTrunkSync`.
- The Figma write step is MCP-operated outside TeamCity until a supported
  automation path exists.

Do not reintroduce speculative headless TeamCity write automation based on the
unsupported remote Figma MCP path that returned `403 Forbidden`. If Figma's MCP
support model changes, document the new supported path before changing the CI
contract.
