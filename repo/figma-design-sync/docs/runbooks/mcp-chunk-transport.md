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

Upload `10-official-sync-payload.png` to the Figma file before running
`10-stage-payload-from-png.mcp.js`. Then run every generated `.mcp.js` snippet
in lexical order. Full official runners use one `99-*.mcp.js` call per bounded
execution unit. Non-catalog targets use one call; catalog targets with declared
roots use one call per root followed by a cleanup-only call. The PNG asset is a
transport artifact only; the staging runner removes the uploaded image node
after extracting the payload.

`upload_assets` returns a single-use URL under `https://mcp.figma.com`. Upload
the PNG as multipart form data with an explicit `image/png` content type.
Sending it as the default `application/octet-stream` is rejected. Request a new
URL after any failed or consumed upload attempt; do not reuse an old URL.

`upload_assets` may place the temporary image on the current Figma page, which
does not have to be the metadata page. The staging runner searches document
image fills and does not rely on `loadAllPagesAsync`; this MCP runtime may
expose that API while rejecting it at execution time.

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

Restage from `00-clear-staging.mcp.js` whenever the TeamCity artifact or the
generated MCP bundle changes. A stable `modelHash` is not sufficient: a
visual-only TypeScript fix changes the generated script lengths even when the
model content is unchanged. After that fix reaches `main`, the next official
TeamCity artifact also has a new `gitSha`, so restage again before writing
metadata.

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

The PNG transport writes all keys in one staging step after validating the
payload hash, Git SHA, model length, and script length.
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

## Run The Complete Visual Sync

Execute every generated `99-*.mcp.js` file in lexical order without editing its
target or root. The official runner contains bounded calls for `preflight` and
every visual target from [target-scopes.md](target-scopes.md). Catalog calls are
split by roots from the official model and finish with stale-node cleanup. All
calls use `writeMetadata=false`.

If a focused diagnostic is necessary, regenerate the runner with the target
and `--allow-partial=true`. A partial runner may confirm a repair, but it cannot
authorize metadata. Regenerate and execute the complete runner before closing
the official synchronization.

## Write Metadata

Metadata must be written last. `checkFigmaTrunkSync` trusts the metadata hash, so
writing it before visuals are reconciled can make CI pass while Figma is still
visually stale.

Read these values from `design-model.json`:

- `schemaVersion`
- `branch`
- `gitSha`
- `modelHash`

Write them to page `62934:908` under namespace `water_my_plants_sync`, plus
`syncedAt`. Figma shared plugin data namespaces accept only alphanumeric
characters, `_`, and `.`.

```javascript
const namespace = "water_my_plants_sync";
const page = await figma.getNodeByIdAsync("62934:908");

if (!page || page.type !== "PAGE") {
  throw new Error("Expected node 62934:908 to be a PAGE");
}

await figma.setCurrentPageAsync(page);

page.setSharedPluginData(namespace, "schemaVersion", String(SCHEMA_VERSION_FROM_JSON));
page.setSharedPluginData(namespace, "branch", BRANCH_FROM_JSON);
page.setSharedPluginData(namespace, "gitSha", GIT_SHA_FROM_JSON);
page.setSharedPluginData(namespace, "modelHash", MODEL_HASH_FROM_JSON);
page.setSharedPluginData(namespace, "syncedAt", new Date().toISOString());

return {
  mutatedNodeIds: [page.id],
  pageName: page.name,
  namespace,
  modelHash: page.getSharedPluginData(namespace, "modelHash"),
  gitSha: page.getSharedPluginData(namespace, "gitSha")
};
```

A metadata-only write is acceptable only when the visual model is already known
to match `design-model.json` and the only mismatch is stale shared plugin
metadata.

When using the staged MCP runner above, write metadata by changing
`SYNC_OPTIONS` to:

```javascript
"const SYNC_OPTIONS = {\"targets\":[\"metadata\"],\"writeMetadata\":true};"
```

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
  syncedAt: page.getSharedPluginData(namespace, "syncedAt")
};
```
