---
title: MCP payload transport
type: runbook
scope: repo/figma-design-sync
owner: figma-design-sync
status: active
last-reviewed: 2026-07-18
review-cycle-days: 90
sources:
  - repo/figma-design-sync/tools/scripts/write-mcp-runner.ts
  - repo/figma-design-sync/tools/scripts/execute-mcp-runner.ts
---

# MCP Payload Transport Runbook

## Purpose

Use this runbook when a TeamCity `design-model.json` artifact and the generated
MCP script must be transported into the Figma MCP runtime. Figma MCP cannot read
local files or TeamCity artifacts directly, so payloads are staged through
temporary Figma shared plugin data.

For official trunk syncs, prefer the generated PNG payload transport. It writes
the model and MCP script into a small PNG uploaded through Figma assets, then a
short MCP runner extracts that payload and stages the validated values. Chunked
shared-plugin-data transport remains available as the fallback when the PNG
payload is too large or asset upload is unavailable.

## Build The MCP Bundle

The visual sync source lives under `repo/figma-design-sync/tools/src/`.
TypeScript is the source of truth; JavaScript is only the generated runtime
artifact executed by Figma MCP.

```powershell
cd repo\figma-design-sync\tools
npm ci
npm run build
```

The generated script is:

- `repo/figma-design-sync/tools/sync-trunk-design-model.mcp.js`

Do not commit the generated JavaScript. The script expects
`design-model.json` to be injected as `DESIGN_MODEL` before execution and must
run in the Figma MCP runtime because it uses the Figma plugin API.

To generate MCP runner snippets for an official TeamCity artifact:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json
```

To validate the Figma visual contract before mutating visual targets, generate a
preflight runner:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --target=preflight --allow-partial=true
```

`preflight` reads the same official model and validates variables, component
properties, usage chip variants, configured sections, target model shape, and
root filters. It must return `mutatedNodeIds: []`.

To make a visual write fail before mutation when the visual contract is stale,
combine `preflight` with one visual target:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --targets=preflight,waterMyPlants.libraries --allow-partial=true
```

The generated `99-run-target.mcp.js` executes the requested diagnostic targets
in one atomic `use_figma` call. Partial runs cannot complete an official
integration. Do not combine `metadata` with any other target; metadata must run
alone after the complete visual runner succeeds.

Official mode defaults to `--transport=png` and writes:

- `10-official-sync-payload.png`
- `00-clear-staging.mcp.js`
- `10-stage-payload-from-png.mcp.js`
- `90-finalize-staging.mcp.js`
- `99-00-preflight.mcp.js` through the final ordered visual execution unit
- `manifest.json`

Manifest schema 2 records `modelHash`, `gitSha`, `writerHash`, `transportHash`,
`manifestHash`, per-file hashes, execution scopes, and per-target
fingerprints. The executor uses this identity to reject stale checkpoints and
the visual planner uses the fingerprints to select `none`, `partial`, or
`full` execution without parsing generated source.

Upload `10-official-sync-payload.png` to the Figma file before running
`10-stage-payload-from-png.mcp.js`. Then run every generated `.mcp.js` snippet
in lexical order. Full official runners use one `99-*.mcp.js` call per bounded
execution unit. Non-catalog targets use one call; catalog targets with declared
roots use one call per root followed by a cleanup-only call. The PNG asset is a
transport artifact only; the staging runner removes the uploaded image node
after extracting the payload.

`upload_assets` returns a single-use URL under `https://mcp.figma.com`. Upload
the raw PNG bytes with an explicit `Content-Type: image/png` header. Sending it
as multipart form data or the default `application/octet-stream` is rejected.
Request a new URL after any failed or consumed upload attempt; do not reuse an
old URL.

`upload_assets` may place the temporary image on the current Figma page, which
does not have to be the metadata page. Uploaded assets are direct page children,
so the staging runner inspects only direct children of each document page. It
must not call `figma.root.findAll`, which traverses the complete design and can
exhaust the plugin runtime, or rely on unsupported `loadAllPagesAsync`.

To run only one top-level catalog root, use its explicit execution scope:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --target=waterMyPlants.libraries.androidx --allow-partial=true
```

Use this for large catalog targets that hit MCP timeouts or generic Figma
runtime failures. `--roots` scopes the already-official TeamCity model in the
runner; it does not create or authorize a branch-local design model.

If the generated PNG exceeds the supported upload size or asset upload is not
usable, regenerate with chunk transport:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --transport=chunks
```

If a generated chunk runner file is too large for the MCP transport, reduce the
chunk size instead of copying the long payload manually:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --transport=chunks --chunk-size=8000
```

Use [visual-preview.md](visual-preview.md) instead when testing fixture-driven
visual changes before the change reaches `main`.

Build and execute the MCP bundle from a code version compatible with the
TeamCity `main` artifact being synced. If local sync tooling is ahead of `main`,
run a bundle generated from `main` or pass an explicit target list that matches
the catalogs present in the TeamCity model.

## Runtime Boundary

The Figma MCP runtime cannot read local files or TeamCity artifacts directly.
Before calling `use_figma`, download the official TeamCity artifact outside
Figma, build the compatible MCP bundle, and stage both values in temporary Figma
shared plugin data.

The runtime supports `atob`, `btoa`, and `Function`, but not browser or Node
transfer helpers such as `fetch`, `XMLHttpRequest`, `importScripts`,
`TextDecoder`, `Blob`, `Response`, or `DecompressionStream`.

That means a local HTTP payload server is not a valid shortcut for loading the
TeamCity artifact into `use_figma`; the payload must be staged through Figma
shared plugin data. The PNG transport still follows this boundary: Figma only
receives an uploaded image asset and a short MCP runner that reads that image
through the Figma plugin image API.

## Staging Keys

The staging namespace is not authoritative state. It is a transport mechanism
for the current sync run. The authoritative namespace remains
`water_my_plants_sync`, and only the final `metadata` target writes to it.

Staging is reused across consecutive `99-*.mcp.js` target executions only when
all of these values remain identical:

- `designModelHash`;
- `designModelGitSha`;
- `designModelLength`;
- `scriptLength`.
- `writerHash`;
- `transportHash`.

Restage from `00-clear-staging.mcp.js` whenever the TeamCity artifact, compiled
writer, or staging transport changes. `writerHash` identifies visual behavior;
`transportHash` identifies only the staging protocol. A stable `modelHash` is
not sufficient to reuse a different writer, while a new `gitSha` alone does not
make unchanged visuals stale. Follow the generated `visual-sync-plan.json` and
the checkpoint rules in [visual-sync-efficiency.md](visual-sync-efficiency.md).

Stage these keys on page `62934:908` under
`water_my_plants_sync_staging`:

| Key | Value |
|-----|-------|
| `designModelJson` | Minified JSON text from TeamCity's official `design-model.json` artifact. |
| `designModelHash` | The artifact `modelHash`, used to validate the staged model. |
| `designModelGitSha` | The artifact `gitSha`, used to validate the staged model. |
| `designModelLength` | Character length of `designModelJson`, used to catch truncated staging writes. |
| `script` | Generated `sync-trunk-design-model.mcp.js` content in plain text. |
| `scriptLength` | Character length of `script`, used to catch truncated staging writes. |
| `writerHash` | Hash of the compiled visual writer staged for execution. |
| `transportHash` | Hash of the staging contract used to deliver the payload. |

The PNG transport writes all keys in one staging step after validating the
payload hash, Git SHA, model length, script length, writer hash, and transport
hash.
Chunk transport writes the same keys incrementally.

The PNG payload also carries a `payloadSchemaVersion`. Candidate discovery must
match that version before comparing model metadata, so an uploaded image from
an older transport contract cannot be selected merely because it has the same
model hash and lengths. The staging runner removes only the selected compatible
payload node after a successful write.

The Figma MCP `use_figma` call has a practical source-size limit near 50k
characters. Stage large payloads in temporary shared plugin data, validate
lengths before execution, and do not copy long payloads manually from terminal
output. Figma limits each shared plugin data entry to about 100k characters, so
runner generation fails locally when either `designModelJson` or `script`
exceeds 100,000 characters. Chunk transport can reduce each MCP call size, but
cannot bypass the final per-entry limit.

Generated `.mcp.js` runner files are source snippets for the Figma MCP
`use_figma` call. They are not local scripts that can talk to Figma from
PowerShell or Node. The Figma plugin runtime cannot read local files from
`%TEMP%`, `dist/`, or any repository path, so a generated runner must still be
passed as the `use_figma` code argument or transported through staged shared
plugin data.

Generated staging snippets are intentionally defensive. In PNG mode, the
staging snippet rejects payloads whose hash, SHA, or lengths do not match the
TeamCity artifact used to generate the runner. In chunk mode, each chunk
validates its own length and the previously staged length before writing. A
failed `use_figma` call is atomic, so a chunk length failure does not append
partial data. If `designModelJson` is already fully staged and only a
`script` chunk fails, clear only `script` and `scriptLength`, regenerate the
runner with a smaller `--chunk-size`, and rerun the `20-script-*.mcp.js`,
`90-finalize-staging.mcp.js`, and target
runner files in lexical order. If the model chunks are uncertain, rerun the
full runner from `00-clear-staging.mcp.js`.

Before diagnosing a visual no-op as a model or component bug, confirm that the
official payload was actually staged. A common interrupted-sync symptom is
`designModelJson.length = 0` in `water_my_plants_sync_staging`, which means
the first `99-*.mcp.js` target runner has no model to apply:

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

## Prerequisites

- Use the official `main` artifact and generated runner manifest.
- Build the TypeScript tools with the repository lockfile.
- Confirm the local MCP endpoint capabilities before attempting a write.

## Verification

Confirm every planned runner unit is recorded as successful, the visual state
matches the selected plan, and metadata is written only after all authorized
visual scopes complete.

## Recovery

Resume from `execution-state.json` when execution identity still matches.
Regenerate runners when model, writer, transport, or manifest hashes differ.
Use chunk staging only when PNG upload is unavailable and the endpoint supports
the required write tools.

## Prohibited Actions

- Do not paste the complete model or compiled writer into chat.
- Do not write metadata after an ad hoc partial repair.
- Do not treat an MCP handshake as proof of write capability.

## Sources

- `tools/scripts/write-mcp-runner.ts`
- `tools/scripts/execute-mcp-runner.ts`
- `tools/scripts/payload-png.ts`

## Run The Planned Visual Sync

The official runner contains bounded calls for `preflight` and every visual
target from [target-scopes.md](../reference/target-scopes.md). Apply
`visual-sync-plan.json`, then execute every selected `99-*.mcp.js` file in
lexical order without editing its target or root. Catalog calls are split by
roots from the official model and finish with stale-node cleanup. All calls use
`writeMetadata=false`.

If a focused diagnostic is necessary, regenerate the runner with the target
and `--allow-partial=true`. A partial runner may confirm a repair, but it cannot
authorize metadata. Regenerate the official artifact and complete its generated
visual plan before closing the synchronization.

## Write Metadata

Metadata must be written last. `checkFigmaTrunkSync` trusts the metadata hash, so
writing it before visuals are reconciled can make CI pass while Figma is still
visually stale.

The generated metadata runner combines these values from `design-model.json`:

- `schemaVersion`
- `branch`
- `gitSha`
- `modelHash`

It also reads `writerHash`, `transportHash`, and `targetFingerprints` from the
runner manifest. It writes all of them plus `syncedAt` to page `62934:908`
under namespace `water_my_plants_sync`. Figma shared plugin data namespaces
accept only alphanumeric characters, `_`, and `.`.

A metadata-only write is acceptable only when the visual model is already known
to match `design-model.json` and the only mismatch is stale shared plugin
metadata.

Generate metadata independently and reuse staging only with a matching
completed visual checkpoint:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --target=metadata
node dist/execute-mcp-runner.mjs --manifest=PATH\TO\metadata\manifest.json --reuse-staging --visual-state=PATH\TO\visual\execution-state.json --dry-run
```

Do not edit `SYNC_OPTIONS` or manifest identity fields by hand.

Then verify the stored metadata before rerunning TeamCity:

```javascript
const page = await figma.getNodeByIdAsync("62934:908");

if (!page || page.type !== "PAGE") {
  throw new Error("Expected sync page 62934:908 to be a PAGE");
}

const namespace = "water_my_plants_sync";

return {
  schemaVersion: page.getSharedPluginData(namespace, "schemaVersion"),
  branch: page.getSharedPluginData(namespace, "branch"),
  gitSha: page.getSharedPluginData(namespace, "gitSha"),
  modelHash: page.getSharedPluginData(namespace, "modelHash"),
  writerHash: page.getSharedPluginData(namespace, "writerHash"),
  transportHash: page.getSharedPluginData(namespace, "transportHash"),
  targetFingerprints: page.getSharedPluginData(namespace, "targetFingerprints"),
  syncedAt: page.getSharedPluginData(namespace, "syncedAt")
};
```
