import { mkdir, readFile, writeFile } from "node:fs/promises";
import { basename, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const TOOL_ROOT = fileURLToPath(new URL("..", import.meta.url));
const DEFAULT_TRUNK_SYNC_SCRIPT = resolve(TOOL_ROOT, "sync-trunk-design-model.mcp.js");
const DEFAULT_PREVIEW_CATALOG_SCRIPT = resolve(TOOL_ROOT, "dist", "sync-catalog-tree-preview.mcp.js");
const FIXTURE_ROOT = resolve(TOOL_ROOT, "fixtures", "visual");
const DEFAULT_OUT_ROOT = resolve(TOOL_ROOT, "dist", "mcp-runners");
const OFFICIAL_STAGING_NAMESPACE = "water_my_plants_sync_staging";
const PREVIEW_STAGING_NAMESPACE = "water_my_plants_sync_preview";
const METADATA_PAGE_ID = "62934:908";
const DEFAULT_CHUNK_SIZE = 30_000;

const KNOWN_TARGETS = [
  "versions",
  "waterMyPlants.libraries",
  "waterMyPlants.plugins",
  "waterMyPlants.customGradleConventionPlugins",
  "waterMyPlants.customGradlePlugins",
  "gradlePlugins.libraries",
  "gradlePlugins.plugins",
  "figmaDesignSync.libraries",
  "figmaDesignSync.plugins",
  "metadata",
];

const DEFAULT_FIXTURE_TARGETS = {
  "catalog-tree": "waterMyPlants.plugins",
  versions: "versions",
};

const args = {
  ...readNpmConfigArgs(),
  ...parseArgs(process.argv.slice(2)),
};
const options = resolveOptions(args);
await writeRunnerFiles(options);

function parseArgs(argv) {
  const parsed = {};
  const positional = [];

  for (let index = 0; index < argv.length; index += 1) {
    const arg = argv[index];
    if (!arg.startsWith("--")) {
      positional.push(arg);
      continue;
    }

    const [rawKey, inlineValue] = arg.slice(2).split("=", 2);
    const key = rawKey.trim();
    const value = inlineValue ?? argv[index + 1];

    if (inlineValue === undefined) {
      index += 1;
    }

    if (!key || value === undefined || value.startsWith("--")) {
      throw new Error(`Missing value for --${key}.`);
    }

    parsed[key] = value;
  }

  applyPositionalArgs(parsed, positional);
  return parsed;
}

function readNpmConfigArgs() {
  const mappings = {
    mode: "npm_config_mode",
    fixture: "npm_config_fixture",
    model: "npm_config_model",
    script: "npm_config_script",
    entrypoint: "npm_config_entrypoint",
    target: "npm_config_target",
    "out-dir": "npm_config_out_dir",
    "chunk-size": "npm_config_chunk_size",
    "section-node-id": "npm_config_section_node_id",
    roots: "npm_config_roots",
    "allow-official-sections": "npm_config_allow_official_sections",
  };
  const values = {};

  for (const [key, envName] of Object.entries(mappings)) {
    const value = process.env[envName];
    if (value) {
      values[key] = value;
    }
  }

  return values;
}

function applyPositionalArgs(parsed, positional) {
  const remaining = [...positional];

  if (!parsed.fixture && !parsed.model && remaining.length > 0) {
    parsed.fixture = remaining.shift();
  }
  if (!parsed.target && remaining.length > 0) {
    parsed.target = remaining.shift();
  }
  if (!parsed["section-node-id"] && remaining.length > 0) {
    parsed["section-node-id"] = remaining.shift();
  }
  if (remaining.length > 0) {
    throw new Error(`Unexpected positional arguments: ${remaining.join(", ")}.`);
  }
}

function resolveOptions(args) {
  const mode = args.mode || "preview";
  if (!["preview", "official"].includes(mode)) {
    throw new Error(`Unsupported mode '${mode}'. Expected 'preview' or 'official'.`);
  }

  const fixture = args.fixture || (mode === "preview" && !args.model ? "catalog-tree" : undefined);
  const target = args.target || (fixture ? DEFAULT_FIXTURE_TARGETS[fixture] : undefined);
  if (!target) {
    throw new Error("Missing --target. Preview fixtures can infer a default target.");
  }
  if (!KNOWN_TARGETS.includes(target)) {
    throw new Error(`Unknown target '${target}'. Expected one of: ${KNOWN_TARGETS.join(", ")}.`);
  }
  if (mode === "preview" && target === "metadata") {
    throw new Error("Preview runners must not target metadata.");
  }
  if (mode === "official" && !args.model) {
    throw new Error("Official runners require --model with the TeamCity design-model.json artifact.");
  }

  const entrypoint = args.entrypoint || (
    mode === "preview" && isCatalogTarget(target) ? "preview-catalog" : "trunk-sync"
  );
  if (!["trunk-sync", "preview-catalog"].includes(entrypoint)) {
    throw new Error(`Unsupported --entrypoint '${entrypoint}'. Expected 'trunk-sync' or 'preview-catalog'.`);
  }
  if (mode === "official" && entrypoint !== "trunk-sync") {
    throw new Error("Official runners must use --entrypoint=trunk-sync.");
  }
  if (entrypoint === "preview-catalog" && (mode !== "preview" || !isCatalogTarget(target))) {
    throw new Error("--entrypoint=preview-catalog can only be used with preview catalog targets.");
  }

  const modelPath = resolve(
    TOOL_ROOT,
    args.model || join("fixtures", "visual", `${fixture}.design-model.json`)
  );
  if (mode === "official" && modelPath.startsWith(FIXTURE_ROOT)) {
    throw new Error("Official runners must not use visual fixtures as their design model.");
  }
  const defaultScriptPath = entrypoint === "preview-catalog"
    ? DEFAULT_PREVIEW_CATALOG_SCRIPT
    : DEFAULT_TRUNK_SYNC_SCRIPT;
  const scriptPath = resolve(TOOL_ROOT, args.script || defaultScriptPath);
  const outRoot = resolve(TOOL_ROOT, args["out-dir"] || DEFAULT_OUT_ROOT);
  const chunkSize = Number(args["chunk-size"] || DEFAULT_CHUNK_SIZE);
  if (!Number.isInteger(chunkSize) || chunkSize < 1_000) {
    throw new Error("--chunk-size must be an integer greater than or equal to 1000.");
  }

  const namespace = mode === "preview"
    ? PREVIEW_STAGING_NAMESPACE
    : OFFICIAL_STAGING_NAMESPACE;
  const writeMetadata = mode === "official" && target === "metadata";
  const sectionNodeId = args["section-node-id"];
  const roots = parseRoots(args.roots);
  const allowOfficialSections = args["allow-official-sections"] === "true";

  if (
    mode === "preview" &&
    isCatalogTarget(target) &&
    !sectionNodeId &&
    !allowOfficialSections
  ) {
    throw new Error(
      "Preview catalog runners require --section-node-id for a sandbox section. " +
        "Use --allow-official-sections=true only for supervised manual repair."
    );
  }

  return {
    mode,
    fixture,
    entrypoint,
    target,
    modelPath,
    scriptPath,
    outRoot,
    chunkSize,
    namespace,
    writeMetadata,
    sectionNodeId,
    roots,
    allowOfficialSections,
  };
}

async function writeRunnerFiles(options) {
  const modelJson = await readFile(options.modelPath, "utf8");
  const minifiedModelJson = JSON.stringify(JSON.parse(modelJson));
  const designModel = JSON.parse(minifiedModelJson);
  const script = await readFile(options.scriptPath, "utf8");
  validateDesignModel(designModel, options);

  const scriptBase64 = Buffer.from(script, "utf8").toString("base64");
  const runDirName = `${options.mode}-${safeName(options.entrypoint)}-${safeName(options.target)}-${safeName(basename(options.modelPath, ".design-model.json"))}`;
  const outDir = join(options.outRoot, runDirName);
  const files = [];

  await mkdir(outDir, { recursive: true });

  files.push(await writeFileIn(outDir, "00-clear-staging.mcp.js", clearStagingSource(options.namespace)));
  files.push(...await writeChunkSources(outDir, "designModelJson", minifiedModelJson, options));
  files.push(...await writeChunkSources(outDir, "scriptBase64", scriptBase64, options));
  files.push(await writeFileIn(outDir, "90-finalize-staging.mcp.js", finalizeStagingSource(options, designModel, minifiedModelJson, script, scriptBase64)));
  files.push(await writeFileIn(outDir, "99-run-target.mcp.js", runTargetSource(options)));
  files.push(await writeManifest(outDir, files, options, designModel, minifiedModelJson, script, scriptBase64));

  console.log(`Wrote ${files.length} MCP runner files to ${outDir}`);
  console.log(`Run them in lexical order, ending with 99-run-target.mcp.js.`);
}

async function writeChunkSources(outDir, key, value, options) {
  const chunks = chunkString(value, options.chunkSize);
  const files = [];
  let previousLength = 0;

  for (let index = 0; index < chunks.length; index += 1) {
    const chunk = chunks[index];
    const fileName = `${key === "designModelJson" ? "10" : "20"}-${key}-${String(index + 1).padStart(3, "0")}.mcp.js`;
    files.push(await writeFileIn(
      outDir,
      fileName,
      appendChunkSource({
        namespace: options.namespace,
        key,
        chunk,
        chunkIndex: index + 1,
        chunkCount: chunks.length,
        previousLength,
      })
    ));
    previousLength += chunk.length;
  }

  return files;
}

async function writeManifest(outDir, files, options, designModel, modelJson, script, scriptBase64) {
  return writeFileIn(
    outDir,
    "manifest.json",
    JSON.stringify(
      {
        mode: options.mode,
        entrypoint: options.entrypoint,
        target: options.target,
        writeMetadata: options.writeMetadata,
        namespace: options.namespace,
        sectionNodeId: options.sectionNodeId || null,
        roots: options.roots,
        allowOfficialSections: options.allowOfficialSections,
        metadataPageId: METADATA_PAGE_ID,
        modelPath: relativeToToolRoot(options.modelPath),
        scriptPath: relativeToToolRoot(options.scriptPath),
        modelHash: designModel.modelHash,
        gitSha: designModel.gitSha,
        designModelLength: modelJson.length,
        scriptLength: script.length,
        scriptBase64Length: scriptBase64.length,
        files,
      },
      null,
      2
    )
  );
}

async function writeFileIn(outDir, fileName, source) {
  await writeFile(join(outDir, fileName), source, "utf8");
  return fileName;
}

function validateDesignModel(designModel, options) {
  if (designModel.branch !== "main") {
    throw new Error(`MCP runners require a main design model. Found '${designModel.branch ?? "<missing>"}'.`);
  }
  if (!designModel.gitSha) {
    throw new Error("MCP runners require designModel.gitSha.");
  }
  if (!designModel.modelHash) {
    throw new Error("MCP runners require designModel.modelHash.");
  }
  if (options.mode === "preview" && options.writeMetadata) {
    throw new Error("Preview runners must never write metadata.");
  }
}

function clearStagingSource(namespace) {
  return `${runtimeHeader()}
const namespace = ${JSON.stringify(namespace)};
const keys = [
  "designModelJson",
  "designModelHash",
  "designModelGitSha",
  "designModelLength",
  "scriptBase64",
  "scriptLength",
  "scriptBase64Length"
];

for (const key of keys) {
  page.setSharedPluginData(namespace, key, "");
}

return { namespace, clearedKeys: keys };
`;
}

function appendChunkSource({ namespace, key, chunk, chunkIndex, chunkCount, previousLength }) {
  return `${runtimeHeader()}
const namespace = ${JSON.stringify(namespace)};
const key = ${JSON.stringify(key)};
const chunk = ${JSON.stringify(chunk)};
const expectedChunkLength = ${chunk.length};
const expectedPreviousLength = ${previousLength};
const previous = page.getSharedPluginData(namespace, key);

if (chunk.length !== expectedChunkLength) {
  throw new Error(\`Unexpected staged \${key} chunk ${chunkIndex}/${chunkCount} length: \${chunk.length} != \${expectedChunkLength}\`);
}

if (previous.length !== expectedPreviousLength) {
  throw new Error(\`Unexpected staged \${key} length before chunk ${chunkIndex}/${chunkCount}: \${previous.length} != \${expectedPreviousLength}\`);
}

page.setSharedPluginData(namespace, key, previous + chunk);

return {
  namespace,
  key,
  chunkIndex: ${chunkIndex},
  chunkCount: ${chunkCount},
  currentLength: page.getSharedPluginData(namespace, key).length
};
`;
}

function finalizeStagingSource(options, designModel, modelJson, script, scriptBase64) {
  return `${runtimeHeader()}
const namespace = ${JSON.stringify(options.namespace)};
const expected = {
  designModelHash: ${JSON.stringify(designModel.modelHash)},
  designModelGitSha: ${JSON.stringify(designModel.gitSha)},
  designModelLength: ${JSON.stringify(String(modelJson.length))},
  scriptLength: ${JSON.stringify(String(script.length))},
  scriptBase64Length: ${JSON.stringify(String(scriptBase64.length))}
};

const stagedModelJson = page.getSharedPluginData(namespace, "designModelJson");
const stagedScriptBase64 = page.getSharedPluginData(namespace, "scriptBase64");

if (String(stagedModelJson.length) !== expected.designModelLength) {
  throw new Error(\`Staged model length mismatch: \${stagedModelJson.length} != \${expected.designModelLength}\`);
}

if (String(stagedScriptBase64.length) !== expected.scriptBase64Length) {
  throw new Error(\`Staged scriptBase64 length mismatch: \${stagedScriptBase64.length} != \${expected.scriptBase64Length}\`);
}

const parsedModel = JSON.parse(stagedModelJson);
if (parsedModel.modelHash !== expected.designModelHash) {
  throw new Error(\`Staged modelHash mismatch: \${parsedModel.modelHash} != \${expected.designModelHash}\`);
}
if (parsedModel.gitSha !== expected.designModelGitSha) {
  throw new Error(\`Staged gitSha mismatch: \${parsedModel.gitSha} != \${expected.designModelGitSha}\`);
}

page.setSharedPluginData(namespace, "designModelHash", expected.designModelHash);
page.setSharedPluginData(namespace, "designModelGitSha", expected.designModelGitSha);
page.setSharedPluginData(namespace, "designModelLength", expected.designModelLength);
page.setSharedPluginData(namespace, "scriptLength", expected.scriptLength);
page.setSharedPluginData(namespace, "scriptBase64Length", expected.scriptBase64Length);

return {
  namespace,
  mode: ${JSON.stringify(options.mode)},
  target: ${JSON.stringify(options.target)},
  modelHash: expected.designModelHash,
  gitSha: expected.designModelGitSha,
  designModelLength: expected.designModelLength,
  scriptBase64Length: expected.scriptBase64Length
};
`;
}

function runTargetSource(options) {
  const syncOptions = {
    targets: [options.target],
    writeMetadata: options.writeMetadata,
    ...(options.sectionNodeId ? { sectionNodeOverrides: { [options.target]: options.sectionNodeId } } : {}),
    ...(options.roots.length > 0 ? { catalogRootFilters: { [options.target]: options.roots } } : {}),
  };

  const invokeScript = options.entrypoint === "preview-catalog"
    ? previewCatalogInvocationSource()
    : trunkSyncInvocationSource();

  return `${runtimeHeader()}
const namespace = ${JSON.stringify(options.namespace)};
const mode = ${JSON.stringify(options.mode)};
const entrypoint = ${JSON.stringify(options.entrypoint)};
const syncOptions = ${JSON.stringify(syncOptions)};

if (mode === "preview" && (syncOptions.writeMetadata === true || syncOptions.targets.includes("metadata"))) {
  throw new Error("Preview runners must not write official Figma sync metadata.");
}

if (
  mode === "preview" &&
  ${JSON.stringify(isCatalogTarget(options.target))} &&
  !syncOptions.sectionNodeOverrides?.[syncOptions.targets[0]] &&
  ${JSON.stringify(!options.allowOfficialSections)}
) {
  throw new Error("Preview catalog runners require a sandbox section override.");
}

const stagedModelJson = page.getSharedPluginData(namespace, "designModelJson");
const scriptBase64 = page.getSharedPluginData(namespace, "scriptBase64");

if (!stagedModelJson || !scriptBase64) {
  throw new Error("Missing staged model or script.");
}

const stagedModel = JSON.parse(stagedModelJson);
const stagedModelHash = page.getSharedPluginData(namespace, "designModelHash");
const stagedModelGitSha = page.getSharedPluginData(namespace, "designModelGitSha");
const stagedModelLength = page.getSharedPluginData(namespace, "designModelLength");
const scriptLength = page.getSharedPluginData(namespace, "scriptLength");
const scriptBase64Length = page.getSharedPluginData(namespace, "scriptBase64Length");

for (const [key, value] of Object.entries({
  designModelHash: stagedModelHash,
  designModelGitSha: stagedModelGitSha,
  designModelLength: stagedModelLength,
  scriptLength,
  scriptBase64Length
})) {
  if (!value) {
    throw new Error(\`Missing staged \${key}.\`);
  }
}

if (stagedModelHash !== stagedModel.modelHash) {
  throw new Error(\`Staged modelHash mismatch: \${stagedModelHash} != \${stagedModel.modelHash}\`);
}

if (stagedModelGitSha !== stagedModel.gitSha) {
  throw new Error(\`Staged gitSha mismatch: \${stagedModelGitSha} != \${stagedModel.gitSha}\`);
}

if (Number(stagedModelLength) !== stagedModelJson.length) {
  throw new Error(\`Staged model length mismatch: \${stagedModelLength} != \${stagedModelJson.length}\`);
}

if (Number(scriptBase64Length) !== scriptBase64.length) {
  throw new Error(\`Staged script length mismatch: \${scriptBase64Length} != \${scriptBase64.length}\`);
}

let script = atob(scriptBase64);

if (Number(scriptLength) !== script.length) {
  throw new Error(\`Decoded script length mismatch: \${scriptLength} != \${script.length}\`);
}

${invokeScript}
`;
}

function trunkSyncInvocationSource() {
  return `
script = script.replace(
  "const DESIGN_MODEL = undefined;",
  "const DESIGN_MODEL = stagedModel;"
);
script = script.replace(
  "const SYNC_OPTIONS = undefined;",
  \`const SYNC_OPTIONS = \${JSON.stringify(syncOptions)};\`
);

const AsyncFunction = Object.getPrototypeOf(async function() {}).constructor;
const run = new AsyncFunction("figma", "stagedModel", script);

return await run(figma, stagedModel);
`;
}

function previewCatalogInvocationSource() {
  return `
if (entrypoint !== "preview-catalog") {
  throw new Error(\`Unexpected entrypoint '\${entrypoint}'.\`);
}

const target = syncOptions.targets[0];
const sectionNodeId = syncOptions.sectionNodeOverrides?.[target];
if (!target || !sectionNodeId) {
  throw new Error("Preview catalog entrypoint requires one target and a sandbox section override.");
}

const AsyncFunction = Object.getPrototypeOf(async function() {}).constructor;
const run = new AsyncFunction(
  "figma",
  "stagedModel",
  "target",
  "sectionNodeId",
  \`\${script}
return await FigmaCatalogTreePreview.main(stagedModel, { target, sectionNodeId });
\`
);

return await run(figma, stagedModel, target, sectionNodeId);
`;
}

function runtimeHeader() {
  return `const page = await figma.getNodeByIdAsync(${JSON.stringify(METADATA_PAGE_ID)});

if (!page || page.type !== "PAGE") {
  throw new Error("Expected sync page ${METADATA_PAGE_ID} to be a PAGE");
}

await figma.setCurrentPageAsync(page);
`;
}

function chunkString(value, chunkSize) {
  const chunks = [];
  for (let index = 0; index < value.length; index += chunkSize) {
    chunks.push(value.slice(index, index + chunkSize));
  }
  return chunks.length > 0 ? chunks : [""];
}

function parseRoots(value) {
  if (!value) {
    return [];
  }

  const roots = value
    .split(",")
    .map((root) => root.trim())
    .filter(Boolean);

  if (roots.length === 0) {
    throw new Error("--roots must contain at least one root label.");
  }

  return [...new Set(roots)];
}

function safeName(value) {
  return value.replace(/[^a-zA-Z0-9]+/g, "-").replace(/^-|-$/g, "");
}

function relativeToToolRoot(path) {
  const relativePath = relative(TOOL_ROOT, path);
  return relativePath && !relativePath.startsWith("..")
    ? relativePath.replace(/\\/g, "/")
    : path;
}

function isCatalogTarget(target) {
  return target !== "versions" && target !== "metadata";
}
