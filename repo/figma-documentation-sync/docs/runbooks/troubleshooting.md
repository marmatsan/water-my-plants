---
title: Figma sync troubleshooting
type: runbook
scope: repo/figma-documentation-sync
owner: figma-documentation-sync
status: active
last-reviewed: 2026-07-27
review-cycle-days: 90
sources:
  - repo/figma-documentation-sync/tools/src
  - repo/figma-documentation-sync/tools/tests
  - repo/figma-documentation-sync/plugin/src/main/kotlin/com/marmatsan/figmaDocumentationSync/plugin/task/canonical
  - repo/figma-documentation-sync/plugin/src/test/kotlin/com/marmatsan/figmaDocumentationSync/plugin/task/canonical/CanonicalFigmaSyncGradleTasksTest.kt
---

# Figma Sync Troubleshooting

## Purpose

Use this document when a Figma trunk sync fails or produces visually incorrect
catalog trees. The normal execution flow lives in
[trunk-sync.md](trunk-sync.md), and the expected visual state lives in
[visual-sync-contract.md](../reference/visual-sync-contract.md).

## Metadata Safety

The sync writes metadata last on purpose. `checkFigmaTrunkSync` trusts the
metadata hash, so writing metadata before visuals are reconciled can make CI
pass while Figma is still visually stale.

A metadata-only write is acceptable only when the visual model is already known
to match `design-model.json` and the only mismatch is stale shared plugin
metadata.

If visual mutation fails, leave the old hash in Figma and let
`checkFigmaTrunkSync` fail until the visual sync can be rerun successfully.

The canonical TeamCity artifact and the MCP visual write are separate failure
domains. A `Figma Sync` run can generate and publish a valid `design-model.json`
artifact from `main`, while the visual write fails later because the Figma
component contract no longer matches the writer code. In that case, keep using
the TeamCity artifact as the authoritative model input, fix the visual contract
or writer, and rerun the failed MCP visual target before writing metadata.

## Canonical Gradle Configuration Cache Failures

The phased TeamCity command that materializes CI configuration and generates the
canonical model runs with the repository configuration cache enabled. A task can
finish its action and still fail the build while Gradle stores that cache. The
failure signature is:

```text
cannot serialize object of type 'org.gradle.api.internal.project.DefaultProject'
```

Treat this as a task-boundary defect, not as a reason to disable configuration
cache. A canonical task must expose execution data through Gradle
`Property`, `ListProperty`, `RegularFileProperty`, or `DirectoryProperty`
inputs. External processes use `ExecOperations`; task fields and runtime
closures must not retain `Project`, an extension, or a registrar that owns a
project.

To reproduce the TeamCity boundary, run the canonical TestKit contract with
configuration-cache problems promoted to failures:

```powershell
.\gradlew.bat -p repo\figma-documentation-sync :plugin:test `
  --tests '*CanonicalFigmaSyncGradleTasksTest' `
  --configuration-cache-problems=fail
```

After correcting the task inputs, run the command twice so the second execution
can reuse the stored entry, merge the fix through the normal PR gate, and rerun
the complete post-merge `Figma Sync` chain. Do not accept a successful task
action when the enclosing Gradle invocation reports a configuration-cache
failure, and do not weaken `org.gradle.configuration-cache=true` as recovery.

## Timeout With Unknown Completion

A failed `use_figma` response does not commit partial visual mutations.
However, a caller or subprocess timeout is ambiguous because the remote
`use_figma` call may still be running when the local process stops waiting.

Before retrying after a timeout:

1. Stop only the abandoned local runner process if it is still active.
2. Inspect the target section in read-only mode.
3. Compare managed child node ids or expected node counts with the state before
   the attempted write.
4. Retry only when the inspection confirms that the target was not committed.

Do not infer success from elapsed time, and do not infer failure only because
the local runner returned no result. Keep metadata unchanged until every target
has a confirmed successful result.

## Transient Figma Memory Failures

Figma may return a direct `Out of memory` error from operations such as
`get_locked` or `findAllWithCriteria`, particularly during catalog cleanup
execution units. Unlike a caller timeout, this is an explicit failed
`use_figma` response: the unit is atomic and does not commit partial visual
mutations.

When this happens:

1. Retry exactly the failed `99-*.mcp.js` execution unit with the same staged
   canonical payload.
2. Do not restart already completed roots or targets.
3. Confirm that the retried unit reports its requested target in
   `completedTargets` before continuing.
4. Keep metadata unchanged until every visual execution unit has completed.

Cleanup-only units are idempotent, so an exact retry is the expected recovery.
If the same unit fails repeatedly, stop retrying and inspect its target scope
and Figma document size before changing the transport or restarting the full
sync.

## Payload Transport Failures

The Figma MCP `use_figma` call has a practical source-size limit near 50k
characters. The plugin runtime supports `atob`, `btoa`, and `Function`, but it
does not provide browser or Node transfer helpers such as `fetch`,
`XMLHttpRequest`, `importScripts`, `TextDecoder`, `Blob`, `Response`, or
`DecompressionStream`.
Do not try to work around this with a local HTTP payload server; `fetch` is not
defined in the Figma MCP `use_figma` runtime, so Figma cannot download the
TeamCity artifact from `127.0.0.1`.

If a payload is truncated, fail before mutating Figma. One failed sync attempt
had expected encoded length `45228`, but only `44624` characters reached Figma.

If a target runner appears to complete without visual changes, inspect staging
before debugging the catalog model. An empty staging value means the runner had
no canonical payload to apply:

```javascript
const page = await figma.getNodeByIdAsync("62934:908");

if (!page || page.type !== "PAGE") {
  throw new Error("Expected sync page 62934:908 to be a PAGE");
}

await figma.setCurrentPageAsync(page);

return {
  designModelJsonLength: page.getSharedPluginData(
    "water_my_plants_sync_staging",
    "designModelJson"
  ).length,
  scriptLength: page.getSharedPluginData(
    "water_my_plants_sync_staging",
    "script"
  ).length
};
```

`designModelJsonLength: 0` after `00-clear-staging.mcp.js` means the payload was
not staged. In PNG transport, confirm that `10-canonical-sync-payload.png` was
uploaded to Figma and that `10-stage-payload-from-png.mcp.js` completed. If PNG
asset upload is blocked, rerun the authorized canonical TeamCity generation with
`-PfigmaMcpTransport=chunks`.

If a chunk call is too large and never reaches Figma, regenerate the chunk
runner with a smaller chunk size and rerun the files in lexical order:

Set `-PfigmaMcpChunkSize=8000` on that authorized generation.

For chunk transport, stage base64 chunks in temporary shared plugin data:

- Store a `runId`, expected chunk count, expected encoded length, and every
  chunk.
- Validate each chunk's own length and the previously staged length before
  writing it.
- In the final call, read all chunks, validate count and length, decode, run the
  script, verify the returned `modelHash`, then delete the temporary data.
- If the final call fails before execution, Figma visuals stay unchanged; only
  temporary staged chunk data may need cleanup.

Do not copy long base64 payloads manually from terminal output.

If PNG staging fails with `loadAllPagesAsync is not a supported API` or
`findAll: Out of memory`, treat it as a runner bug, not a bad payload. The
generated staging code must inspect only direct children of each document page.
`upload_assets` creates the temporary image as a direct page child, even when
the upload lands on a different page from the metadata page, so neither API is
required.

The tools package can generate visual preview runner files. Canonical runners
are generated in Kotlin only as part of the authorized TeamCity artifact:

```powershell
.\gradlew.bat buildFigmaDocumentationSyncTools
cd repo\figma-documentation-sync\tools
node dist\write-mcp-preview.mjs --entrypoint=preview-catalog --fixture=catalog-tree --target=waterMyPlants.plugins --section-node-id=SANDBOX_SECTION_ID
```

Use preview runners to reproduce visual issues quickly. They stage data under
`water_my_plants_sync_preview` and must not be used to write canonical metadata.
Catalog preview uses the smaller `sync-catalog-tree-preview.mcp.js` entrypoint
by default so connector and layout fixes can be tested without transporting the
full trunk-sync bundle.

For narrow connector or catalog-tree diagnostics, a temporary bundle that
imports only the catalog tree gateway can reduce the payload enough to run a
single root scope. Treat that bundle as a diagnostic artifact only: it does not
replace the canonical TeamCity artifact plus `sync-trunk-design-model.mcp.js`
runner, and it must not write final sync metadata.

Figma MCP cannot execute a local runner file by path. If a diagnostic bundle is
generated under `%TEMP%` or `dist/`, its JavaScript still has to be supplied as
the `use_figma` code argument or staged through shared plugin data. Do not
spend time trying to make the Figma runtime read local files directly.

## Figma REST Read Flakes

`checkFigmaTrunkSync` can fail because the Figma REST API closes the connection
while the task is reading metadata:

```text
java.io.EOFException: Failed to parse HTTP response: the server prematurely closed the connection
```

Treat this as a transport failure, not as proof that Figma metadata is stale. If
a direct Figma MCP read shows `water_my_plants_sync.modelHash` and `gitSha`
match the TeamCity artifact, rerun `checkFigmaTrunkSync` once before changing
the model, the metadata, or the visual sync code.

## Component Instance Shape

Figma component instances can contain hidden template internals. Some remain
addressable through the instance API, while nested content hidden by a boolean
instance property can disappear from `findAllWithCriteria` even though it is
present in the main component. Do not treat exact child counts as a stable
contract for reused instances.

For example, library artifact text updates must allow extra hidden
`artifact name` and `artifact version` text nodes and update only the expected
visible entries. The failure signature is similar to:

```text
expected 0 'artifact name' text nodes, found 8
```

Library artifact and bundle slot selection must prefer direct children of the
`.tree node` `artifacts` frame, including hidden reusable slots. If the sync
counts only visible descendants, it can accidentally select a nested artifact
inside `.artifacts bundle` and fail with a misleading slot count:

```text
Tree node '...' expected at least 4 '.artifact' instances, found 1.
```

Preflight can report a similar false negative when it selects an `.artifact`
inside `.artifacts bundle` whose `Show configured as tool` property is false:

```text
Expected '...' to contain a '.tool artifact usage' template instance.
```

First inspect the `.artifact` main component. If it already contains the
required `.tool artifact usage` slots and public text property, keep the
component unchanged and make preflight validate the main component as a
read-only fallback. Do not mutate the instance property during preflight and do
not remove the component slots to match a hidden instance.

A bundle-only catalog node can fail during its write even though a separate
read of the same bundle finds all expected artifacts:

```text
Bundle '...' expected at least 4 '.artifact' instances, found 0.
```

This happens when the direct-artifact fallback selects descendant `.artifact`
rows owned by `.artifacts bundle` and hides them as unused before the bundle
writer runs. Direct artifact selection must exclude every candidate with an
`.artifacts bundle` ancestor inside the `.tree node`; the bundle writer remains
responsible for those nested rows. Do not add component properties or retry the
same canonical runner to mask this failure.

After `swapComponent`, Figma can expose only the bundle component's first
`.artifact` row even when its master reserves more hidden rows. A mixed node can
then fail with `no library tree-node template has enough configurable
catalog-item slots`. Inspect the bundle master with invisible instance children
included. The writer must temporarily set
`skipInvisibleInstanceChildren=false`, reveal exactly the nested rows required
by the model, and restore the previous option in both success and failure paths.
Do not duplicate visible rows manually in the document to bypass this adapter
responsibility.

The same traversal rule applies to reserved usage rows. A cloned plugin node
can reveal its `Applied by Gradle project` block while Figma still omits every
hidden `.usage chip` descendant, producing an error such as:

```text
Node '...' expected at least 3 '.usage chip' instances for 'Applied by Gradle project' or 'Applied by module', found 0.
```

Inspect the `.tree node` master with invisible instance children included. If
the master already reserves enough chips, keep the component unchanged and fix
the adapter to enumerate, populate, and reveal those slots within the temporary
traversal scope. Apply the same rule to `.tool artifact usage`; do not clone
rows manually into generated instances.

When deriving a new component set from existing variants, cloning a variant
preserves its layers but does not recreate the shared component-set property
contract reliably. Create the shared properties on the new set, wire every
variant layer to those properties, and validate `componentPropertyDefinitions`
before updating project configuration. Reparenting the final variant can cause
Figma to remove the now-empty source set automatically; check whether the set
still exists before calling `remove()` explicitly.

## Invalid CI Hierarchy Slots

The CI writer depends on public direct-child contracts inside named transparent
layout frames, not on nested sublayer IDs:

- `.ci node` owns an `execution plan` frame containing `phase 01` through
  `phase 08`, each backed by `.ci phase`;
- `.ci phase` owns a `steps` frame containing `step 01` through `step 08`, each
  backed by `.ci step`;
- `.ci node` owns an `outcome` frame containing `outcome 01` through
  `outcome 04`, each backed by `.ci outcome`.

Every slot must be visible in its master and exposed to the containing
component. Generated instances hide only unused slots.

The `.ci phase` master must also expose `show steps` as a BOOLEAN with default
`true`, and its direct `steps` frame must bind `visible` to that property. The
writer sets `show steps=false` when `phase.steps` is empty. If all step slots
are hidden but the phase still contains a large blank region, inspect this
container binding first: resizing the parent only fits the already oversized
child and does not correct the missing collapse contract.

The `.ci node` master follows the same rule for `show outcome`: its direct
`outcome` frame binds `visible` to that BOOLEAN, and the writer derives the
instance value from whether the node has typed outcomes. Without the aggregate
binding, hiding the reserved outcome slots leaves an empty full-width frame and
expands otherwise compact nodes.

Reparenting a layer inside a component can clear its
`componentPropertyReferences`, even when the layer itself and the component
properties survive. After moving optional CI layers, restore and validate the
`visible` bindings explicitly. In particular, `runtime context` remains bound
to `show runtime`, `source` remains bound to `show source`, and their outer
container remains bound to `show optional details`. Validate at least one
compact instance after the move; checking only the fully expanded master can
hide a lost binding or a fixed-width child.

If CI preflight reports a `.ci node` outline mismatch, keep exactly one visible
solid stroke on the root component, bind it to `md/sys/color/outline`, align it
`INSIDE`, and set its weight to `2`. Remove any stroke from `optional details`
so expanded and compact instances have one boundary instead of a double
outline.

If preflight reports missing, duplicated, unexpected, unexposed, or foreign CI
slots:

1. Edit the owning main component, not a published instance or nested sublayer
   URL.
2. Restore the configured transparent frame as a direct child of its owner and
   the exact numeric slots as direct children of that frame. Do not add another
   intermediate frame.
3. Use an instance of the configured child component for every slot, mark it as
   exposed, and keep it visible in the master.
4. Run the visual preflight again before retrying the CI visual target.

Do not weaken the slot-count check, traverse implementation-specific child ids,
or create ad hoc sibling steps to bypass a broken component. When a visual plan
legitimately exceeds 8 phases, 8 steps in one phase, or 4 outcomes, split the
documentation boundary and regenerate the Kotlin plan. Keep final sync metadata
unchanged until preflight and the affected visual target both succeed.

If every `role=group` instance shows the same placeholder tasks or loses its
bullets after an override, inspect the `.ci step` contract. The component set
must expose a `tasks` TEXT property, and the group variant must bind exactly one
left-aligned `tasks` text layer to it. The writer supplies literal bullets in a
multiline value. Native Figma list range styling is not a substitute because a
component TEXT override resets that formatting.

## TeamCity Artifact Handoff Returns HTML

If `prepareTeamCityFigmaSyncHandoff` fails while decoding the TeamCity response
with `invalid character '<' looking for beginning of value`, the protected
artifact request returned HTML instead of the expected JSON. Verify the CLI
authentication and endpoint first. If the artifact was already downloaded by
another authorized route, use the documented artifact-directory fallback with
both `-PfigmaArtifactDirectory` and the exact
`-PfigmaExpectedGitSha`. The explicit revision check is mandatory; never accept
an unverified local artifact or weaken the handoff validation.

## Usage Blocks Hidden Despite Model Data

The canonical `design-model.json` can be correct while the visible Figma
`.artifact` or `.tree node` instance is still visually stale. One observed
failure mode was a library artifact whose model contained
`providedByConventionPlugins` and effective `requiredByModules`, while the
visible `.artifact` either still exposed the obsolete aggregate
`Show consumer modules` property or kept its direct `Applied by plugin` /
`Used by module` blocks hidden. The equivalent plugin failure is a
`Plugin` `.tree node` whose model contains `appliedToModules` or
`providedByConventionPlugins`, while `Applied by Gradle project` /
`Used by convention plugin` stay hidden or the custom-plugin warning block
remains visible. Hidden template internals under the same `.tree node` can
still contain chips, which makes the file look partially updated through the
plugin API but not visually updated on canvas.
Dependency catalog entries without usage should fail `checkFigmaCatalogUsage`
before merge. The static warning block is only for repository-owned custom
Gradle plugin leaves that no module consumes yet. The `.tree node` `Plugin`
variant must not contain `.usage chip` instances inside that block; if one
appears there, repair the component contract before changing catalog extraction.
Do not treat an empty `providedByConventionPlugins.requiredByModules` list as
unused by itself: the entry is still applied by the convention plugin, but no
module currently applies that convention plugin.

Diagnose this as a visual sync inconsistency before changing catalog
extraction:

- Confirm the canonical TeamCity `design-model.json` contains the artifact usage.
- Inspect the visible direct `.artifact` or `.artifacts bundle` instance under
  the `artifacts` frame, not hidden template internals.
- Confirm the visible direct `.artifact` / `.artifacts bundle` instance has the
  granular component booleans expected by the model:
  `Show applied by plugin`, `Show used by module`, `Show configured as tool`
  when present. `.artifact` and `.artifacts bundle` do not expose an
  unused-entry boolean because unused libraries are rejected by CI. `Show
  consumer modules` is obsolete and must not be used to validate the surface.
- For plugin trees, confirm the visible `Plugin` `.tree node` has the granular
  component booleans expected by the model: `Show applied by module`,
  `Show used by convention plugin`, and `Show unused` only when a custom Gradle
  plugin warning is expected.

The TypeScript sync must fail the visual target before metadata if this
invariant is not true. Do not repair this with a metadata-only write.

## Missing Usage Chip Heading Text

If the visual target fails with a message like:

```text
Node '...' is missing 'Configured as tool' usage chip heading text.
```

the canonical TeamCity artifact and staging can still be valid. This failure
usually means the writer's expected visual contract is stale relative to the
Figma component structure. For example, `.artifact` now renders tooling rows in
a visible `Tool artifacts` section that contains `.tool artifact usage`
instances; `Configured as tool` is no longer the visible heading text even
though the granular boolean may still be named `Show configured as tool`.

Treat this as a sync-code/component-contract mismatch:

- Inspect the failing node id and confirm which visible section and nested
  component instances exist.
- Update `repo/figma-documentation-sync/tools/src/figma/figma-consumer-modules-gateway.ts`
  and the Figma config constants to match the current component text and
  component property names.
- Rebuild the tools package, regenerate the MCP runner, and rerun the failed
  visual target before writing metadata.

Do not fix this by editing metadata. The visual mutation did not complete.

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

## CI Connectors That Cross Nodes

CI relations use the native `simple-line_arrow / neutral` connector at node
`64835:3289`, including its centered text. The locked template is positioned
over the CI component documentation section but remains outside managed CI
targets so cleanup cannot delete it. Relations do not use a separately grouped
label. If an elbowed CI connector blocks a node, inspect its endpoints before
changing spacing or recreating visual labels:

- connections between nodes in the same horizontal row use side magnets;
- downward cross-row connections use `BOTTOM` to `TOP`;
- upward cross-row connections use `TOP` to `BOTTOM`;
- same-row return connections use bottom magnets so they route below the row;
- opposite vertical connections between the same nodes use left and right side
  lanes instead of sharing the central inter-row band.

Every endpoint then binds to its own transparent `.ci connector port` group on
the selected side. If several labels or arrows still converge at one point,
inspect the endpoint ids: they must reference different managed port groups,
not the `.ci node group` itself and not one shared port. Ports on the same side
must appear in the order of their opposite nodes. If that order is wrong,
reproduce the routing input in the TypeScript port-allocation unit test before
changing Figma spacing manually.

A side-to-side route between different rows can cross an intermediate node even
when the connector label itself is correct. Reproduce route changes in an
isolated Figma section first, keep one obstacle node between the endpoints, and
verify both directions before changing the canonical writer. After the writer
is rebuilt, rerun the affected granular `ci.*` target and inspect both endpoint
ports, the native label, and the complete elbow before writing metadata.

## Removed Connector Lookup Failures

If a catalog tree or granular CI documentation sync fails with a message like:

```text
The node with id "..." does not exist
```

while removing stale nodes or connectors, inspect whether the sync removed a
connector or one of its endpoint groups and then read an invalidated object
again in the same `use_figma` execution. Figma can invalidate removed nodes
immediately, and removing a native connector endpoint may remove its connector
as a side effect.

Catalog cleanup must collect removed connector ids before calling
`connector.remove()` and then filter the in-memory connector list by those ids.
CI section cleanup must classify managed children before mutating the tree,
remove connectors before endpoint groups, and check `node.removed` immediately
before every `remove()`. Do not call `connectorReferencesAnyNode()` or read
shared plugin data from any node after it has been removed.

This is a writer bug, not evidence that the TeamCity `design-model.json`
artifact is invalid. Rebuild the MCP bundle after fixing the writer and rerun
the failed visual target before writing metadata.

## Missing Connector Template

Missing parent/child edges must be represented by cloned `simple-solid_arrow`
connectors. If connector creation fails, report the parent and child node ids in
the diagnostic path before writing metadata. That keeps the failed visual
relation actionable instead of reducing the problem to a generic Figma API
error.

CI connectors have a separate exact-node contract. The preflight must fail if
node `64835:3289` is missing, is not a connector, is not named
`simple-line_arrow / neutral`, is not elbowed with an arrow-lines end cap, or
has no native text. Do not fall back to `simple-solid_arrow` and do not recreate
the removed `.ci connector label` structure.

## Moved Version Collection

The versions preflight resolves the local Figma variable collection by the
configured canonical `versions.properties` path. When that source moves without
changing the product catalog identity, the preflight fails before visual writes
with `Variable collection '...' was not found`.

Inspect local collections before retrying. If exactly one collection has the
former canonical path, the target path does not exist, and its modes are still
`Version alias` and `Version number`, rename that collection in place. Do not
create a second collection or recreate its variables: preserving the collection
and variable ids preserves existing component bindings. Record the failed
preflight checkpoint, perform the supervised rename, and retry that same
preflight runner before continuing with visual targets.

## Missing Nested Instance After Property Synchronization

Figma can invalidate an exposed nested-instance proxy after `setProperties()`
recomposes its component subtree. An error such as `in get_parent: The node
with id ... does not exist` during CI row collapse means the writer retained a
pre-mutation slot reference. Capture slot indexes and their owning rows before
mutating phase properties, then derive final row visibility from the ordered
phase count in the model. Do not re-query `owner.exposedInstances` afterward:
hidden slots may be absent. Do not work around the failure by keeping the empty
row visible or by assigning it a fixed height.

## Missing Component Property

If the MCP execution fails with a missing component property such as:

```text
Could not find a component property with name: 'Show is a gradle convention plugin#63112:4'
```

inspect the `.tree node` component set and update `treeNodeProps` in
root `build.gradle.kts`
to match the actual Figma component property name before rerunning the sync.

Do not work around this by writing metadata only; the visual update did not
complete. The current property name is:

```text
Show is a gradle plugin#63112:4
```

## Bundle And Model Mismatch

If a newer MCP bundle fails against a TeamCity model from `main` with a missing
catalog target such as:

```text
designModel.content.catalogs.waterMyPlants.libraries is required for catalog tree sync.
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

## Local MCP Endpoint Is Read-only

The deterministic executor can probe the Figma Desktop endpoint:

```powershell
.\gradlew.bat probeFigmaMcp
```

At the time this contract was implemented, `http://127.0.0.1:3845/mcp`
advertised metadata, screenshot, design-context, and Code Connect tools but did
not advertise `use_figma` or `upload_assets`. That is a capability result, not a
TeamCity authentication failure. `runFigmaMcp` must stop before mutation and
report the missing tools.

Continue with the Codex-operated canonical Figma MCP writer and record each
successful or failed generated unit in `execution-state.json` as described in
[visual-sync-efficiency.md](visual-sync-efficiency.md). Re-test the endpoint
with `probeFigmaMcp` before enabling direct execution; do not infer write support
from a successful MCP handshake.

## Code Connect Component Is Not Published

Code Connect mappings require a component or component set published to a
Figma team library. A local component can still appear in
`get_code_connect_suggestions`, but `send_code_connect_mappings` rejects it
atomically with:

```text
Published component not found
```

Inspect the component and every nested component targeted by the mapping with
`getPublishStatusAsync()`. When `publishStatus` is `UNPUBLISHED`, publish the
component sets from the Figma library UI before retrying. Do not redirect the
mapping to a variant id or resend the same payload: neither operation publishes
the component. After publication, rerun the suggestions, confirm the resolved
main component ids, save the approved mappings, and read them back with the
Code Connect map endpoint.

## Prerequisites

Capture the failing target, runner file, execution identity, Figma section, and
the smallest relevant tool response. Do not begin with a new full write when a
compatible checkpoint exists.

## Verification

Re-run the failed atomic unit, inspect its target section, and confirm the
result is recorded in `execution-state.json`. Complete the authoritative visual
plan before metadata verification.

## Recovery

Use the symptom-specific sections above. When completion is unknown, inspect
the target before retrying. When capability or identity is incompatible,
regenerate rather than overriding the guard.

## Prohibited Actions

- Do not infer success from a timeout.
- Do not move connectors or managed nodes to the page as a permanent repair.
- Do not bypass preflight, capability, hash, or metadata completion checks.

## Sources

- `tools/src/`
- `tools/tests/`
- `execution-state.json` generated beside the canonical runners
