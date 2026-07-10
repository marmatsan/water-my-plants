# MCP Chunk Transport Runbook

## Purpose

Use this runbook when a TeamCity `design-model.json` artifact and the generated
MCP script must be transported into the Figma MCP runtime. Figma MCP cannot read
local files or TeamCity artifacts directly, so payloads are staged through
temporary Figma shared plugin data.

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

To generate chunked MCP runner snippets for an official TeamCity artifact:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --target=waterMyPlants.plugins
```

To run only one top-level catalog root against its child section, add `--roots`
and `--section-node-id`:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --target=waterMyPlants.libraries --roots=androidx --section-node-id=63069:630
```

Use this for large catalog targets that hit MCP timeouts or generic Figma
runtime failures. `--roots` scopes the already-official TeamCity model in the
runner; it does not create or authorize a branch-local design model.

If a generated runner file is too large for the MCP transport, reduce the chunk
size instead of copying the long payload manually:

```powershell
node dist\write-mcp-runner.mjs --mode=official --model=PATH\TO\design-model.json --target=waterMyPlants.plugins --chunk-size=8000
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
shared plugin data.

## Staging Keys

The staging namespace is not authoritative state. It is a transport mechanism
for the current sync run. The authoritative namespace remains
`water_my_plants_sync`, and only the final `metadata` target writes to it.
Always overwrite staged values for a new sync run; do not reuse values already
present in `water_my_plants_sync_staging`.

Stage these keys on page `62934:908` under
`water_my_plants_sync_staging`:

| Key | Value |
|-----|-------|
| `designModelJson` | Minified JSON text from TeamCity's official `design-model.json` artifact. |
| `designModelHash` | The artifact `modelHash`, used to validate the staged model. |
| `designModelGitSha` | The artifact `gitSha`, used to validate the staged model. |
| `designModelLength` | Character length of `designModelJson`, used to catch truncated staging writes. |
| `scriptBase64` | Base64-encoded generated `sync-trunk-design-model.mcp.js` content. |
| `scriptLength` | Character length of the decoded script. |
| `scriptBase64Length` | Character length of `scriptBase64`, used to catch truncated staging writes. |

The Figma MCP `use_figma` call has a practical source-size limit near 50k
characters. Stage large payloads in temporary shared plugin data, validate
lengths before execution, and do not copy long base64 payloads manually from
terminal output. If chunking is needed for staging, validate chunk count and
encoded length before assembling the final `scriptBase64` value.

Generated `.mcp.js` runner files are source snippets for the Figma MCP
`use_figma` call. They are not local scripts that can talk to Figma from
PowerShell or Node. The Figma plugin runtime cannot read local files from
`%TEMP%`, `dist/`, or any repository path, so a generated runner must still be
passed as the `use_figma` code argument or transported through staged shared
plugin data.

Generated staging snippets are intentionally defensive: each chunk validates
its own length and the previously staged length before writing. A failed
`use_figma` call is atomic, so a chunk length failure does not append partial
data. If `designModelJson` is already fully staged and only a `scriptBase64`
chunk fails, clear only `scriptBase64`, `scriptLength`, and
`scriptBase64Length`, regenerate the runner with a smaller `--chunk-size`, and
rerun the `20-scriptBase64-*.mcp.js`, `90-finalize-staging.mcp.js`, and target
runner files in lexical order. If the model chunks are uncertain, rerun the
full runner from `00-clear-staging.mcp.js`.

Before diagnosing a visual no-op as a model or component bug, confirm that the
official payload was actually staged. A common interrupted-sync symptom is
`designModelJson.length = 0` in `water_my_plants_sync_staging`, which means
`99-run-target.mcp.js` has no model to apply:

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
  scriptBase64Length: page.getSharedPluginData(
    "water_my_plants_sync_staging",
    "scriptBase64"
  ).length
};
```

## Run A Visual Target

Run visual updates by granular target. Do not run `metadata` until every visual
target has completed successfully.

Reusable MCP target runner:

```javascript
const page = await figma.getNodeByIdAsync("62934:908");

if (!page || page.type !== "PAGE") {
  throw new Error("Expected sync page 62934:908 to be a PAGE");
}

const stagingNamespace = "water_my_plants_sync_staging";
const stagedModelJson = page.getSharedPluginData(stagingNamespace, "designModelJson");
const scriptBase64 = page.getSharedPluginData(stagingNamespace, "scriptBase64");

if (!stagedModelJson || !scriptBase64) {
  throw new Error("Missing staged model or script.");
}

const stagedModel = JSON.parse(stagedModelJson);
const stagedModelHash = page.getSharedPluginData(stagingNamespace, "designModelHash");
const stagedModelGitSha = page.getSharedPluginData(stagingNamespace, "designModelGitSha");
const stagedModelLength = page.getSharedPluginData(stagingNamespace, "designModelLength");
const scriptLength = page.getSharedPluginData(stagingNamespace, "scriptLength");
const scriptBase64Length = page.getSharedPluginData(stagingNamespace, "scriptBase64Length");

const requiredStagingValues = {
  designModelHash: stagedModelHash,
  designModelGitSha: stagedModelGitSha,
  designModelLength: stagedModelLength,
  scriptLength,
  scriptBase64Length
};

for (const [key, value] of Object.entries(requiredStagingValues)) {
  if (!value) {
    throw new Error(`Missing staged ${key}.`);
  }
}

if (stagedModelHash !== stagedModel.modelHash) {
  throw new Error(`Staged modelHash mismatch: ${stagedModelHash} != ${stagedModel.modelHash}`);
}

if (stagedModelGitSha !== stagedModel.gitSha) {
  throw new Error(`Staged gitSha mismatch: ${stagedModelGitSha} != ${stagedModel.gitSha}`);
}

if (Number(stagedModelLength) !== stagedModelJson.length) {
  throw new Error(`Staged model length mismatch: ${stagedModelLength} != ${stagedModelJson.length}`);
}

if (Number(scriptBase64Length) !== scriptBase64.length) {
  throw new Error(`Staged script length mismatch: ${scriptBase64Length} != ${scriptBase64.length}`);
}

let script = atob(scriptBase64);

if (Number(scriptLength) !== script.length) {
  throw new Error(`Decoded script length mismatch: ${scriptLength} != ${script.length}`);
}

script = script.replace(
  "const DESIGN_MODEL = undefined;",
  "const DESIGN_MODEL = stagedModel;"
);
script = script.replace(
  "const SYNC_OPTIONS = undefined;",
  "const SYNC_OPTIONS = {\"targets\":[\"TARGET_NAME\"],\"writeMetadata\":false};"
);

const AsyncFunction = Object.getPrototypeOf(async function() {}).constructor;
const run = new AsyncFunction("figma", "stagedModel", script);

return await run(figma, stagedModel);
```

Replace `TARGET_NAME` with a granular target from
[target-scopes.md](target-scopes.md).

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
