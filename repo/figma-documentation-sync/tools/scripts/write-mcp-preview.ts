import { createHash } from "node:crypto";
import { mkdir, readFile, writeFile } from "node:fs/promises";
import { basename, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import {
  CATALOG_TARGET_NAMES,
  CI_VISUAL_TARGET_NAMES,
  DEFAULT_FIXTURE_TARGETS,
  METADATA_PAGE_ID,
  PREVIEW_STAGING_NAMESPACE,
  WRITER_TARGET_NAMES,
} from "@figma-documentation-sync/project-config";

const TOOL_ROOT = fileURLToPath(new URL("..", import.meta.url));
const DEFAULT_TRUNK_SYNC_SCRIPT = resolve(TOOL_ROOT, "sync-trunk-design-model.mcp.js");
const DEFAULT_PREVIEW_CATALOG_SCRIPT = resolve(TOOL_ROOT, "dist", "sync-catalog-tree-preview.mcp.js");
const DEFAULT_OUT_ROOT = resolve(TOOL_ROOT, "dist", "mcp-runners");
const DEFAULT_CHUNK_SIZE = 30_000;
const MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH = 100_000;
const MANIFEST_SCHEMA_VERSION = 4;
const TRANSPORT_CONTRACT_VERSION = 1;
const WRITER_SCOPE_FINGERPRINT_SCHEMA_VERSION = 1;

const KNOWN_TARGETS = WRITER_TARGET_NAMES;
const CATALOG_TARGETS = CATALOG_TARGET_NAMES;

const args = {
  ...readNpmConfigArgs(),
  ...parseArgs(process.argv.slice(2)),
};
const options = resolveOptions(args);
await writePreviewRunnerFiles(options);

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
    targets: "npm_config_targets",
    "out-dir": "npm_config_out_dir",
    "chunk-size": "npm_config_chunk_size",
    transport: "npm_config_transport",
    "ci-visual-plan": "npm_config_ci_visual_plan",
    "section-node-id": "npm_config_section_node_id",
    roots: "npm_config_roots",
    "allow-canonical-sections": "npm_config_allow_canonical_sections",
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
  if (mode !== "preview") {
    throw new Error(
      "The TypeScript runner supports preview only. Use the Kotlin prepareCanonicalFigmaSync task for canonical runners."
    );
  }

  const fixture = args.fixture || (!args.model ? "catalog-tree" : undefined);
  const targetValue = args.targets || args.target || (fixture
    ? DEFAULT_FIXTURE_TARGETS[fixture]
    : undefined);
  if (!targetValue) {
    throw new Error("Missing --target. Preview fixtures can infer a default target.");
  }
  const requestedTargets = parseTargets(targetValue);
  const scopedTargets = requestedTargets.map(parseTargetScope);
  const targets = scopedTargets.map(({ target }) => target);
  if (targets.length === 0) {
    throw new Error("Missing --target.");
  }
  const unknownTargets = targets.filter((target) => !KNOWN_TARGETS.includes(target));
  if (unknownTargets.length > 0) {
    throw new Error(`Unknown target(s) '${unknownTargets.join(", ")}'. Expected one of: ${KNOWN_TARGETS.join(", ")}.`);
  }
  if (targets.length !== 1) {
    throw new Error("Preview runners support exactly one target.");
  }
  const target = targets[0];
  if (targets.includes("metadata")) {
    throw new Error("Preview runners must not target metadata.");
  }
  if (targets.includes("preflight")) {
    throw new Error("Preview runners must not target preflight.");
  }
  if (targets.includes("headers")) {
    throw new Error("Header sync requires a canonical main artifact runner.");
  }

  const transport = args.transport || "chunks";
  if (transport !== "chunks") {
    throw new Error("Preview runners support chunk transport only.");
  }

  const entrypoint = args.entrypoint || (
    isCatalogTarget(target) ? "preview-catalog" : "trunk-sync"
  );
  if (!["trunk-sync", "preview-catalog"].includes(entrypoint)) {
    throw new Error(`Unsupported --entrypoint '${entrypoint}'. Expected 'trunk-sync' or 'preview-catalog'.`);
  }
  if (entrypoint === "preview-catalog" && !isCatalogTarget(target)) {
    throw new Error("--entrypoint=preview-catalog can only be used with preview catalog targets.");
  }

  const modelPath = resolve(
    TOOL_ROOT,
    args.model || join("fixtures", "visual", `${fixture}.design-model.json`)
  );
  const defaultScriptPath = entrypoint === "preview-catalog"
    ? DEFAULT_PREVIEW_CATALOG_SCRIPT
    : DEFAULT_TRUNK_SYNC_SCRIPT;
  const scriptPath = resolve(TOOL_ROOT, args.script || defaultScriptPath);
  const outRoot = resolve(TOOL_ROOT, args["out-dir"] || DEFAULT_OUT_ROOT);
  const chunkSize = Number(args["chunk-size"] || DEFAULT_CHUNK_SIZE);
  if (!Number.isInteger(chunkSize) || chunkSize < 1_000) {
    throw new Error("--chunk-size must be an integer greater than or equal to 1000.");
  }

  const namespace = PREVIEW_STAGING_NAMESPACE;
  const writeMetadata = false;
  const sectionNodeId = args["section-node-id"];
  const scopedRoots = scopedTargets.flatMap(({ root }) => root ? [root] : []);
  if (scopedRoots.length > 0 && args.roots) {
    throw new Error("A root-qualified target cannot be combined with --roots.");
  }
  const roots = scopedRoots.length > 0 ? scopedRoots : parseRoots(args.roots);
  const allowCanonicalSections = args["allow-canonical-sections"] === "true";
  const ciTargets = targets.filter((name) => CI_VISUAL_TARGET_NAMES.includes(name));
  const ciVisualPlanPath = args["ci-visual-plan"]
    ? resolve(TOOL_ROOT, args["ci-visual-plan"])
    : undefined;

  if (ciTargets.length > 0 && !ciVisualPlanPath) {
    throw new Error(
      "CI targets require --ci-visual-plan with output from the generateFigmaCiVisualPlan Gradle task."
    );
  }

  if ((sectionNodeId || roots.length > 0) && targets.length !== 1) {
    throw new Error("--section-node-id and --roots can only be used with a single target.");
  }

  if (
    isCatalogTarget(target) &&
    !sectionNodeId &&
    !allowCanonicalSections
  ) {
    throw new Error(
      "Preview catalog runners require --section-node-id for a sandbox section. " +
        "Use --allow-canonical-sections=true only for supervised manual repair."
    );
  }

  return {
    mode,
    fixture,
    entrypoint,
    target,
    targets,
    requestedTargets,
    modelPath,
    scriptPath,
    outRoot,
    chunkSize,
    transport,
    namespace,
    writeMetadata,
    sectionNodeId,
    roots,
    allowCanonicalSections,
    allowPartial: false,
    fullVisualSync: false,
    ciVisualPlanPath,
  };
}

async function writePreviewRunnerFiles(options) {
  const modelJson = await readFile(options.modelPath, "utf8");
  const minifiedModelJson = JSON.stringify(JSON.parse(modelJson));
  const designModel = JSON.parse(minifiedModelJson);
  const ciVisualPlan = options.ciVisualPlanPath
    ? JSON.parse(await readFile(options.ciVisualPlanPath, "utf8"))
    : undefined;
  const script = await readFile(options.scriptPath, "utf8");
  validateDesignModel(designModel);
  validateCiVisualPlan(ciVisualPlan, options.targets);
  const writerHash = sha256(script);
  const transportHash = createTransportHash(options);
  const targetFingerprints = createTargetFingerprints(designModel, options.requestedTargets[0]);
  const writerScopeFingerprints = Object.fromEntries(
    Object.keys(targetFingerprints).map((scope) => [scope, writerHash])
  );

  validateStagingEntryLength("designModelJson", minifiedModelJson);
  validateStagingEntryLength("script", script);
  const targetRunName = options.targets.join("-");
  const runDirName = `${options.mode}-${safeName(options.entrypoint)}-${safeName(targetRunName)}-${safeName(options.transport)}-${safeName(basename(options.modelPath, ".design-model.json"))}`;
  const outDir = join(options.outRoot, runDirName);
  const files = [];
  const payloadImage = null;

  await mkdir(outDir, { recursive: true });

  files.push(await writeFileIn(outDir, "00-clear-staging.mcp.js", clearStagingSource(options.namespace)));

  files.push(...await writeChunkSources(outDir, "designModelJson", minifiedModelJson, options));
  files.push(...await writeChunkSources(outDir, "script", script, options));

  files.push(await writeFileIn(
    outDir,
    "90-finalize-staging.mcp.js",
    finalizeStagingSource(options, designModel, minifiedModelJson, script, writerHash, transportHash)
  ));
  const targetRunner = await writeTargetRunnerFiles(outDir, { ...options, ciVisualPlan }, {
    writerHash,
    transportHash,
    targetFingerprints,
    writerScopeFingerprints,
    writerScopeFingerprintSchemaVersion: WRITER_SCOPE_FINGERPRINT_SCHEMA_VERSION,
  });
  files.push(...targetRunner.files);
  files.push(await writeManifest(
    outDir,
    files,
    options,
    designModel,
    minifiedModelJson,
    script,
    payloadImage,
    {
      writerHash,
      transportHash,
      targetFingerprints,
      writerScopeFingerprints,
      writerScopeFingerprintSchemaVersion: WRITER_SCOPE_FINGERPRINT_SCHEMA_VERSION,
      executionScopes: targetRunner.executionScopes,
    }
  ));

  console.log(`Wrote ${files.length} MCP runner files to ${outDir}`);
  console.log("Run every generated .mcp.js file in lexical order.");
}

async function writeTargetRunnerFiles(outDir, options, executionMetadata) {
  const file = await writeFileIn(
    outDir,
    "99-run-target.mcp.js",
    runTargetSource(options, options.targets, {}, executionMetadata)
  );
  return {
    files: [file],
    executionScopes: { [file]: options.requestedTargets.join(",") },
  };
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

async function writeManifest(
  outDir,
  files,
  options,
  designModel,
  modelJson,
  script,
  payloadImage,
  executionMetadata
) {
  const fileHashes = Object.fromEntries(await Promise.all(
    files.map(async (file) => [file, sha256(await readFile(join(outDir, file)))])
  ));
  const manifestBody = {
    schemaVersion: MANIFEST_SCHEMA_VERSION,
    mode: options.mode,
    entrypoint: options.entrypoint,
    target: options.target,
    targets: options.targets,
    writeMetadata: options.writeMetadata,
    transport: options.transport,
    namespace: options.namespace,
    sectionNodeId: options.sectionNodeId || null,
    roots: options.roots,
    allowCanonicalSections: options.allowCanonicalSections,
    allowPartial: options.allowPartial,
    fullVisualSync: options.fullVisualSync,
    metadataPageId: METADATA_PAGE_ID,
    modelPath: relativeToToolRoot(options.modelPath),
    scriptPath: relativeToToolRoot(options.scriptPath),
    modelHash: designModel.modelHash,
    gitSha: designModel.gitSha,
    designModelLength: modelJson.length,
    scriptLength: script.length,
    writerHash: executionMetadata.writerHash,
    transportHash: executionMetadata.transportHash,
    targetFingerprints: executionMetadata.targetFingerprints,
    writerScopeFingerprints: executionMetadata.writerScopeFingerprints,
    writerScopeFingerprintSchemaVersion: executionMetadata.writerScopeFingerprintSchemaVersion,
    executionScopes: executionMetadata.executionScopes,
    payloadImage,
    files,
    fileHashes,
  };
  const manifestHash = sha256(stableJson(manifestBody));
  return writeFileIn(
    outDir,
    "manifest.json",
    JSON.stringify(
      { ...manifestBody, manifestHash },
      null,
      2
    )
  );
}

async function writeFileIn(outDir, fileName, source) {
  await writeFile(join(outDir, fileName), source, "utf8");
  return fileName;
}

function validateDesignModel(designModel) {
  if (designModel.branch !== "main") {
    throw new Error(`MCP runners require a main design model. Found '${designModel.branch ?? "<missing>"}'.`);
  }
  if (!designModel.gitSha) {
    throw new Error("MCP runners require designModel.gitSha.");
  }
  if (!designModel.modelHash) {
    throw new Error("MCP runners require designModel.modelHash.");
  }
}

function validateStagingEntryLength(key, value) {
  if (value.length > MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH) {
    throw new Error(
      `${key} is ${value.length} characters and exceeds the ${MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH}-character ` +
        "sharedPluginData staging limit. Reduce the generated payload before creating the runner."
    );
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
  "script",
  "scriptLength",
  "writerHash",
  "transportHash"
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

function finalizeStagingSource(options, designModel, modelJson, script, writerHash, transportHash) {
  return `${runtimeHeader()}
const namespace = ${JSON.stringify(options.namespace)};
const expected = {
  designModelHash: ${JSON.stringify(designModel.modelHash)},
  designModelGitSha: ${JSON.stringify(designModel.gitSha)},
  designModelLength: ${JSON.stringify(String(modelJson.length))},
  scriptLength: ${JSON.stringify(String(script.length))},
  writerHash: ${JSON.stringify(writerHash)},
  transportHash: ${JSON.stringify(transportHash)}
};

const stagedModelJson = page.getSharedPluginData(namespace, "designModelJson");
const stagedScript = page.getSharedPluginData(namespace, "script");

if (String(stagedModelJson.length) !== expected.designModelLength) {
  throw new Error(\`Staged model length mismatch: \${stagedModelJson.length} != \${expected.designModelLength}\`);
}

if (String(stagedScript.length) !== expected.scriptLength) {
  throw new Error(\`Staged script length mismatch: \${stagedScript.length} != \${expected.scriptLength}\`);
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
page.setSharedPluginData(namespace, "writerHash", expected.writerHash);
page.setSharedPluginData(namespace, "transportHash", expected.transportHash);

return {
  namespace,
  mode: ${JSON.stringify(options.mode)},
  target: ${JSON.stringify(options.target)},
  modelHash: expected.designModelHash,
  gitSha: expected.designModelGitSha,
  designModelLength: expected.designModelLength,
  scriptLength: expected.scriptLength,
  writerHash: expected.writerHash,
  transportHash: expected.transportHash
};
`;
}

function runTargetSource(options, targets = options.targets, runOptions = {}, executionMetadata = {}) {
  const target = targets[0];
  const roots = runOptions.roots || options.roots;
  const ciVisualPlan = targetCiVisualPlan(options.ciVisualPlan, targets);
  const syncOptions = {
    targets,
    writeMetadata: options.writeMetadata,
    ...(options.sectionNodeId ? { sectionNodeOverrides: { [options.target]: options.sectionNodeId } } : {}),
    ...(roots.length > 0 ? { catalogRootFilters: { [target]: roots } } : {}),
    ...(runOptions.cleanupOnly ? { catalogCleanupOnlyTargets: [target] } : {}),
    ...(ciVisualPlan ? { ciVisualPlan } : {}),
    executionMetadata,
  };
  const executionScope = runOptions.executionScope || options.requestedTargets?.[0] || target;

  const invokeScript = options.entrypoint === "preview-catalog"
    ? previewCatalogInvocationSource()
    : trunkSyncInvocationSource(executionScope, target);

  return `${runtimeHeader()}
const namespace = ${JSON.stringify(options.namespace)};
const mode = ${JSON.stringify(options.mode)};
const entrypoint = ${JSON.stringify(options.entrypoint)};
const syncOptions = ${JSON.stringify(syncOptions)};

if (mode === "preview" && (syncOptions.writeMetadata === true || syncOptions.targets.includes("metadata"))) {
  throw new Error("Preview runners must not write canonical Figma sync metadata.");
}

if (
  mode === "preview" &&
  ${JSON.stringify(isCatalogTarget(options.target))} &&
  !syncOptions.sectionNodeOverrides?.[syncOptions.targets[0]] &&
  ${JSON.stringify(!options.allowCanonicalSections)}
) {
  throw new Error("Preview catalog runners require a sandbox section override.");
}

const stagedModelJson = page.getSharedPluginData(namespace, "designModelJson");
let script = page.getSharedPluginData(namespace, "script");

if (!stagedModelJson || !script) {
  throw new Error("Missing staged model or script.");
}

const stagedModel = JSON.parse(stagedModelJson);
const stagedModelHash = page.getSharedPluginData(namespace, "designModelHash");
const stagedModelGitSha = page.getSharedPluginData(namespace, "designModelGitSha");
const stagedModelLength = page.getSharedPluginData(namespace, "designModelLength");
const scriptLength = page.getSharedPluginData(namespace, "scriptLength");
const writerHash = page.getSharedPluginData(namespace, "writerHash");
const transportHash = page.getSharedPluginData(namespace, "transportHash");

for (const [key, value] of Object.entries({
  designModelHash: stagedModelHash,
  designModelGitSha: stagedModelGitSha,
  designModelLength: stagedModelLength,
  scriptLength,
  writerHash,
  transportHash
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

if (Number(scriptLength) !== script.length) {
  throw new Error(\`Staged script length mismatch: \${scriptLength} != \${script.length}\`);
}

if (writerHash !== syncOptions.executionMetadata.writerHash) {
  throw new Error(\`Staged writerHash mismatch: \${writerHash} != \${syncOptions.executionMetadata.writerHash}\`);
}

if (transportHash !== syncOptions.executionMetadata.transportHash) {
  throw new Error(\`Staged transportHash mismatch: \${transportHash} != \${syncOptions.executionMetadata.transportHash}\`);
}

${invokeScript}
`;
}

function validateCiVisualPlan(plan, targets) {
  const ciTargets = targets.filter((target) => CI_VISUAL_TARGET_NAMES.includes(target));
  if (ciTargets.length === 0) return;
  if (
    !plan ||
    plan.schemaVersion !== 2 ||
    typeof plan.parentName !== "string" ||
    !Array.isArray(plan.sections)
  ) {
    throw new Error(
      "The Kotlin CI visual plan must use schemaVersion 2 and contain parentName and sections."
    );
  }

  for (const target of ciTargets) {
    const matches = plan.sections.filter((section) => section?.target === target);
    if (matches.length !== 1) {
      throw new Error(`The Kotlin CI visual plan must contain exactly one section for '${target}'.`);
    }
    if (!matches[0].nodes?.every((node) => Array.isArray(node.steps))) {
      throw new Error(`Every CI visual node in '${target}' must contain a typed steps array.`);
    }
  }
}

function targetCiVisualPlan(plan, targets) {
  const ciTargets = targets.filter((target) => CI_VISUAL_TARGET_NAMES.includes(target));
  if (ciTargets.length === 0) return undefined;
  return {
    schemaVersion: plan.schemaVersion,
    parentName: plan.parentName,
    sections: plan.sections.filter((section) => ciTargets.includes(section.target)),
  };
}

function trunkSyncInvocationSource(executionScope, target) {
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

const result = await run(figma, stagedModel);
return { ...result, executionScope: ${JSON.stringify(executionScope)}, modelTarget: ${JSON.stringify(target)} };
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

function createTransportHash(options) {
  return sha256(stableJson({
    contractVersion: TRANSPORT_CONTRACT_VERSION,
    transport: options.transport,
    chunkSize: options.transport === "chunks" ? options.chunkSize : null,
    payloadSchemaVersion: null,
    clearStagingSource: clearStagingSource.toString(),
    appendChunkSource: appendChunkSource.toString(),
    finalizeStagingSource: finalizeStagingSource.toString(),
  }));
}

function createTargetFingerprints(designModel, executionScope) {
  const { target } = parseTargetScope(executionScope);
  return { [executionScope]: sha256(stableJson(modelSliceForTarget(designModel, target))) };
}

function modelSliceForTarget(designModel, target) {
  if (target === "preflight") {
    return designModel.content;
  }
  if (target === "headers") {
    return { target };
  }
  if (target === "versions") {
    return {
      versions: designModel.content?.versions,
      versionSections: designModel.content?.versionSections,
    };
  }
  if (target.startsWith("ci.")) {
    return designModel.content?.ci;
  }
  if (CATALOG_TARGETS.includes(target)) {
    const [catalogName, treeName] = target.split(".");
    return designModel.content?.catalogs?.[catalogName]?.[treeName] || [];
  }
  return null;
}

function sha256(value) {
  return `sha256:${createHash("sha256").update(value).digest("hex")}`;
}

function stableJson(value) {
  return JSON.stringify(sortJson(value ?? null));
}

function sortJson(value) {
  if (Array.isArray(value)) {
    return value.map(sortJson);
  }
  if (value && typeof value === "object" && !Buffer.isBuffer(value)) {
    return Object.fromEntries(
      Object.keys(value).sort().map((key) => [key, sortJson(value[key])])
    );
  }
  return value;
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

function parseTargets(value) {
  return [...new Set(
    String(value)
      .split(",")
      .map((target) => target.trim())
      .filter(Boolean)
  )];
}

function parseTargetScope(value) {
  if (KNOWN_TARGETS.includes(value)) return { target: value, root: null };
  const catalogTarget = [...CATALOG_TARGETS].sort((left, right) => right.length - left.length)
    .find((target) => value.startsWith(`${target}.`));
  if (!catalogTarget) return { target: value, root: null };
  const root = value.slice(catalogTarget.length + 1);
  return !root || root === "cleanup" || root.includes(".")
    ? { target: value, root: null }
    : { target: catalogTarget, root };
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
  return CATALOG_TARGETS.includes(target);
}
