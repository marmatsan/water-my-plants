import { createHash } from "node:crypto";
import { mkdir, readFile, writeFile } from "node:fs/promises";
import { basename, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import {
  buildOfficialSyncPayload,
  createPayloadPng,
  MAX_FIGMA_UPLOAD_ASSET_BYTES,
  PAYLOAD_PNG_SCHEMA_VERSION,
  PAYLOAD_PNG_TEXT_KEYWORD,
  stringifyAsciiJson,
} from "./payload-png";
import {
  createWriterScopeFingerprints,
  WRITER_SCOPE_FINGERPRINT_SCHEMA_VERSION,
} from "./writer-scope-fingerprints";
import {
  CATALOG_TARGET_NAMES,
  CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY,
  DEFAULT_FIXTURE_TARGETS,
  METADATA_PAGE_ID,
  OFFICIAL_STAGING_NAMESPACE,
  PREVIEW_STAGING_NAMESPACE,
  REPOSITORY_ROOT_RELATIVE_TO_TOOLS,
  WRITER_TARGET_NAMES,
} from "@figma-design-sync/project-config";

const TOOL_ROOT = fileURLToPath(new URL("..", import.meta.url));
const DEFAULT_TRUNK_SYNC_SCRIPT = resolve(TOOL_ROOT, "sync-trunk-design-model.mcp.js");
const DEFAULT_PREVIEW_CATALOG_SCRIPT = resolve(TOOL_ROOT, "dist", "sync-catalog-tree-preview.mcp.js");
const FIXTURE_ROOT = resolve(TOOL_ROOT, "fixtures", "visual");
const DEFAULT_OUT_ROOT = resolve(TOOL_ROOT, "dist", "mcp-runners");
const DEFAULT_CHUNK_SIZE = 30_000;
const PAYLOAD_PNG_FILE_NAME = "10-official-sync-payload.png";
const MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH = 100_000;
const MANIFEST_SCHEMA_VERSION = 3;
const TRANSPORT_CONTRACT_VERSION = 1;

const KNOWN_TARGETS = WRITER_TARGET_NAMES;

const FULL_VISUAL_TARGETS = KNOWN_TARGETS.filter((target) => target !== "metadata");

const CATALOG_TARGETS = CATALOG_TARGET_NAMES;

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
    targets: "npm_config_targets",
    "out-dir": "npm_config_out_dir",
    "chunk-size": "npm_config_chunk_size",
    transport: "npm_config_transport",
    "section-node-id": "npm_config_section_node_id",
    roots: "npm_config_roots",
    "allow-official-sections": "npm_config_allow_official_sections",
    "allow-partial": "npm_config_allow_partial",
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
  const targetValue = args.targets || args.target || (fixture
    ? DEFAULT_FIXTURE_TARGETS[fixture]
    : mode === "official" ? "all" : undefined);
  if (!targetValue) {
    throw new Error("Missing --target. Preview fixtures can infer a default target.");
  }
  const requestedTargets = targetValue === "all" ? [...FULL_VISUAL_TARGETS] : parseTargets(targetValue);
  const scopedTargets = requestedTargets.map(parseTargetScope);
  const targets = scopedTargets.map(({ target }) => target);
  if (targets.length === 0) {
    throw new Error("Missing --target.");
  }
  const unknownTargets = targets.filter((target) => !KNOWN_TARGETS.includes(target));
  if (unknownTargets.length > 0) {
    throw new Error(`Unknown target(s) '${unknownTargets.join(", ")}'. Expected one of: ${KNOWN_TARGETS.join(", ")}.`);
  }
  if (mode === "preview" && targets.length !== 1) {
    throw new Error("Preview runners support exactly one target.");
  }
  if (targets.includes("metadata") && targets.length > 1) {
    throw new Error("The metadata target must run alone after every visual target is correct.");
  }
  const fullVisualSync = sameTargets(targets, FULL_VISUAL_TARGETS);
  const allowPartial = args["allow-partial"] === "true";
  if (mode === "official" && !fullVisualSync && targets[0] !== "metadata" && !allowPartial) {
    throw new Error(
      "Official visual sync must target the complete visual model. " +
        "Omit --target or use --target=all. Use --allow-partial=true only for supervised diagnosis or repair."
    );
  }
  const target = targets[0];
  if (mode === "preview" && targets.includes("metadata")) {
    throw new Error("Preview runners must not target metadata.");
  }
  if (mode === "official" && !args.model) {
    throw new Error("Official runners require --model with the TeamCity design-model.json artifact.");
  }
  if (mode === "preview" && targets.includes("preflight")) {
    throw new Error("Preview runners must not target preflight.");
  }
  if (mode === "preview" && targets.includes("headers")) {
    throw new Error("Header sync requires an official main artifact runner.");
  }

  const transport = args.transport || (mode === "official" ? "png" : "chunks");
  if (!["chunks", "png"].includes(transport)) {
    throw new Error(`Unsupported --transport '${transport}'. Expected 'chunks' or 'png'.`);
  }
  if (transport === "png" && mode !== "official") {
    throw new Error("--transport=png is only supported for official runners.");
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
  const scopedRoots = scopedTargets.flatMap(({ root }) => root ? [root] : []);
  if (scopedRoots.length > 0 && args.roots) {
    throw new Error("A root-qualified target cannot be combined with --roots.");
  }
  const roots = scopedRoots.length > 0 ? scopedRoots : parseRoots(args.roots);
  const allowOfficialSections = args["allow-official-sections"] === "true";

  if ((sectionNodeId || roots.length > 0) && targets.length !== 1) {
    throw new Error("--section-node-id and --roots can only be used with a single target.");
  }

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
    allowOfficialSections,
    allowPartial,
    fullVisualSync,
  };
}

async function writeRunnerFiles(options) {
  const modelJson = await readFile(options.modelPath, "utf8");
  const minifiedModelJson = JSON.stringify(JSON.parse(modelJson));
  const designModel = JSON.parse(minifiedModelJson);
  const script = await readFile(options.scriptPath, "utf8");
  validateDesignModel(designModel, options);
  const writerHash = sha256(script);
  const transportHash = createTransportHash(options);
  const targetFingerprints = createTargetFingerprints(designModel);
  const writerScopeFingerprints = await createWriterScopeFingerprints({
    sourceRoot: resolve(TOOL_ROOT, "src"),
    repositoryRoot: resolve(TOOL_ROOT, REPOSITORY_ROOT_RELATIVE_TO_TOOLS),
    policyPath: resolve(
      TOOL_ROOT,
      REPOSITORY_ROOT_RELATIVE_TO_TOOLS,
      CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY
    ),
    scopes: Object.keys(targetFingerprints),
  });

  validateStagingEntryLength("designModelJson", minifiedModelJson);
  validateStagingEntryLength("script", script);
  const targetRunName = options.fullVisualSync ? "all-visual" : options.targets.join("-");
  const runDirName = `${options.mode}-${safeName(options.entrypoint)}-${safeName(targetRunName)}-${safeName(options.transport)}-${safeName(basename(options.modelPath, ".design-model.json"))}`;
  const outDir = join(options.outRoot, runDirName);
  const files = [];
  let payloadImage = null;

  await mkdir(outDir, { recursive: true });

  files.push(await writeFileIn(outDir, "00-clear-staging.mcp.js", clearStagingSource(options.namespace)));

  if (options.transport === "png") {
    const payload = buildOfficialSyncPayload({
      designModel,
      modelJson: minifiedModelJson,
      script,
      writerHash,
      transportHash,
    });
    const payloadJson = stringifyAsciiJson(payload);
    const payloadPng = createPayloadPng(payloadJson);
    if (payloadPng.length > MAX_FIGMA_UPLOAD_ASSET_BYTES) {
      throw new Error(
        `Official payload PNG is ${payloadPng.length} bytes and exceeds ` +
          `${MAX_FIGMA_UPLOAD_ASSET_BYTES} bytes. Regenerate with --transport=chunks.`
      );
    }

    await writeFile(join(outDir, PAYLOAD_PNG_FILE_NAME), payloadPng);
    payloadImage = {
      fileName: PAYLOAD_PNG_FILE_NAME,
      byteLength: payloadPng.length,
      sha256: sha256(payloadPng),
      textKeyword: PAYLOAD_PNG_TEXT_KEYWORD,
    };
    files.push(await writeFileIn(outDir, "10-stage-payload-from-png.mcp.js", stagePayloadFromPngSource(
      options,
      designModel,
      minifiedModelJson,
      script,
      PAYLOAD_PNG_FILE_NAME,
      writerHash,
      transportHash
    )));
  } else {
    files.push(...await writeChunkSources(outDir, "designModelJson", minifiedModelJson, options));
    files.push(...await writeChunkSources(outDir, "script", script, options));
  }

  files.push(await writeFileIn(
    outDir,
    "90-finalize-staging.mcp.js",
    finalizeStagingSource(options, designModel, minifiedModelJson, script, writerHash, transportHash)
  ));
  const targetRunner = await writeTargetRunnerFiles(outDir, options, designModel, {
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
  if (payloadImage) {
    console.log(`Upload ${PAYLOAD_PNG_FILE_NAME} to Figma before running 10-stage-payload-from-png.mcp.js.`);
  }
  console.log("Run every generated .mcp.js file in lexical order.");
}

async function writeTargetRunnerFiles(outDir, options, designModel, executionMetadata) {
  if (!options.fullVisualSync) {
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

  const files = [];
  const executionScopes = {};
  for (let index = 0; index < options.targets.length; index += 1) {
    const target = options.targets[index];
    const roots = catalogRoots(designModel, target);
    if (roots.length === 0) {
      const fileName = `99-${String(index).padStart(2, "0")}-${safeName(target)}.mcp.js`;
      files.push(await writeFileIn(
        outDir,
        fileName,
        runTargetSource(options, [target], { executionScope: target }, executionMetadata)
      ));
      executionScopes[fileName] = target;
      continue;
    }

    for (let rootIndex = 0; rootIndex < roots.length; rootIndex += 1) {
      const root = roots[rootIndex];
      const fileName = `99-${String(index).padStart(2, "0")}-${String(rootIndex).padStart(2, "0")}-${safeName(target)}-${safeName(root)}.mcp.js`;
      files.push(await writeFileIn(
        outDir,
        fileName,
        runTargetSource(
          options,
          [target],
          { roots: [root], executionScope: `${target}.${root}` },
          executionMetadata
        )
      ));
      executionScopes[fileName] = `${target}.${root}`;
    }

    const cleanupFileName = `99-${String(index).padStart(2, "0")}-99-${safeName(target)}-cleanup.mcp.js`;
    files.push(await writeFileIn(
      outDir,
      cleanupFileName,
      runTargetSource(
        options,
        [target],
        { cleanupOnly: true, executionScope: `${target}.cleanup` },
        executionMetadata
      )
    ));
    executionScopes[cleanupFileName] = `${target}.cleanup`;
  }
  return { files, executionScopes };
}

function catalogRoots(designModel, target) {
  if (!CATALOG_TARGETS.includes(target)) {
    return [];
  }

  const [catalogName, treeName] = target.split(".");
  const nodes = designModel.content?.catalogs?.[catalogName]?.[treeName];
  if (!Array.isArray(nodes) || nodes.length === 0) {
    return [];
  }

  const rootKey = treeName === "libraries" ? "group" : "id";
  return [...new Set(nodes.map((node) => node?.[rootKey]).filter(Boolean))];
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
    allowOfficialSections: options.allowOfficialSections,
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
  "scriptBase64",
    "scriptLength",
    "scriptBase64Length",
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

function stagePayloadFromPngSource(
  options,
  designModel,
  modelJson,
  script,
  payloadFileName,
  writerHash,
  transportHash
) {
  return `${runtimeHeader()}
const namespace = ${JSON.stringify(options.namespace)};
const payloadKeyword = ${JSON.stringify(PAYLOAD_PNG_TEXT_KEYWORD)};
const payloadFileName = ${JSON.stringify(payloadFileName)};
const expected = {
  payloadSchemaVersion: ${JSON.stringify(PAYLOAD_PNG_SCHEMA_VERSION)},
  designModelHash: ${JSON.stringify(designModel.modelHash)},
  designModelGitSha: ${JSON.stringify(designModel.gitSha)},
  designModelLength: ${JSON.stringify(String(modelJson.length))},
  scriptLength: ${JSON.stringify(String(script.length))},
  writerHash: ${JSON.stringify(writerHash)},
  transportHash: ${JSON.stringify(transportHash)}
};

const candidates = [];
const imageHashes = new Set();
const imageNodes = [];
for (const documentPage of figma.root.children) {
  if (documentPage.type !== "PAGE") continue;

  for (const node of documentPage.children) {
    const fills = "fills" in node ? node.fills : undefined;
    if (!Array.isArray(fills)) continue;

    let hasPayloadCandidate = false;
    for (const fill of fills) {
      if (fill?.type === "IMAGE" && fill.imageHash) {
        hasPayloadCandidate = true;
        imageHashes.add(fill.imageHash);
      }
    }
    if (hasPayloadCandidate) imageNodes.push(node);
  }
}

for (const imageHash of imageHashes) {
  const image = figma.getImageByHash(imageHash);
  if (!image) {
    continue;
  }

  const bytes = await image.getBytesAsync();
  const encodedPayload = readPayloadFromPngText(bytes, payloadKeyword);
  if (!encodedPayload) {
    continue;
  }

  const payload = JSON.parse(atob(encodedPayload));
  if (
    payload.payloadSchemaVersion === expected.payloadSchemaVersion &&
    payload.designModelHash === expected.designModelHash &&
    String(payload.designModelLength) === expected.designModelLength &&
    String(payload.scriptLength) === expected.scriptLength
    && payload.writerHash === expected.writerHash
    && payload.transportHash === expected.transportHash
  ) {
    candidates.push({ imageHash, payload });
  }
}

if (candidates.length === 0) {
  throw new Error(
    "Could not find official sync payload PNG for model " + expected.designModelHash + ". " +
    "Upload " + payloadFileName + " with Figma upload_assets before this step."
  );
}

const { imageHash, payload } = candidates[0];
validatePayload(payload, expected);

page.setSharedPluginData(namespace, "designModelJson", payload.designModelJson);
page.setSharedPluginData(namespace, "script", payload.script);
page.setSharedPluginData(namespace, "designModelHash", expected.designModelHash);
page.setSharedPluginData(namespace, "designModelGitSha", expected.designModelGitSha);
page.setSharedPluginData(namespace, "designModelLength", expected.designModelLength);
page.setSharedPluginData(namespace, "scriptLength", expected.scriptLength);
page.setSharedPluginData(namespace, "writerHash", expected.writerHash);
page.setSharedPluginData(namespace, "transportHash", expected.transportHash);

let payloadNodesRemoved = 0;
for (const node of imageNodes) {
  const fills = Array.isArray(node.fills) ? node.fills : [];
  if (fills.some((fill) => fill?.type === "IMAGE" && fill.imageHash === imageHash)) {
    node.remove();
    payloadNodesRemoved += 1;
  }
}

return {
  namespace,
  transport: "png",
  payloadSchemaVersion: expected.payloadSchemaVersion,
  payloadNodesRemoved,
  modelHash: expected.designModelHash,
  gitSha: expected.designModelGitSha,
  designModelLength: expected.designModelLength,
  scriptLength: expected.scriptLength,
  writerHash: expected.writerHash,
  transportHash: expected.transportHash
};

function validatePayload(payload, expected) {
  for (const [key, value] of Object.entries({
    designModelJson: payload.designModelJson,
    script: payload.script,
    designModelHash: payload.designModelHash,
    designModelGitSha: payload.designModelGitSha,
    writerHash: payload.writerHash,
    transportHash: payload.transportHash
  })) {
    if (!value) {
      throw new Error(\`Payload is missing \${key}.\`);
    }
  }

  if (payload.payloadSchemaVersion !== expected.payloadSchemaVersion) {
    throw new Error(\`Payload schema version mismatch: \${payload.payloadSchemaVersion} != \${expected.payloadSchemaVersion}\`);
  }
  if (payload.designModelHash !== expected.designModelHash) {
    throw new Error(\`Payload modelHash mismatch: \${payload.designModelHash} != \${expected.designModelHash}\`);
  }
  if (payload.designModelGitSha !== expected.designModelGitSha) {
    throw new Error(\`Payload gitSha mismatch: \${payload.designModelGitSha} != \${expected.designModelGitSha}\`);
  }
  if (String(payload.designModelLength) !== expected.designModelLength) {
    throw new Error(\`Payload model length metadata mismatch: \${payload.designModelLength} != \${expected.designModelLength}\`);
  }
  if (payload.designModelJson.length !== Number(expected.designModelLength)) {
    throw new Error(\`Payload model JSON length mismatch: \${payload.designModelJson.length} != \${expected.designModelLength}\`);
  }
  if (String(payload.scriptLength) !== expected.scriptLength) {
    throw new Error(\`Payload script length metadata mismatch: \${payload.scriptLength} != \${expected.scriptLength}\`);
  }
  if (payload.script.length !== Number(expected.scriptLength)) {
    throw new Error(\`Payload script length mismatch: \${payload.script.length} != \${expected.scriptLength}\`);
  }
  if (payload.writerHash !== expected.writerHash) {
    throw new Error(\`Payload writerHash mismatch: \${payload.writerHash} != \${expected.writerHash}\`);
  }
  if (payload.transportHash !== expected.transportHash) {
    throw new Error(\`Payload transportHash mismatch: \${payload.transportHash} != \${expected.transportHash}\`);
  }

  const parsedModel = JSON.parse(payload.designModelJson);
  if (parsedModel.modelHash !== expected.designModelHash) {
    throw new Error(\`Payload model JSON hash mismatch: \${parsedModel.modelHash} != \${expected.designModelHash}\`);
  }
  if (parsedModel.gitSha !== expected.designModelGitSha) {
    throw new Error(\`Payload model JSON gitSha mismatch: \${parsedModel.gitSha} != \${expected.designModelGitSha}\`);
  }

}

function readPayloadFromPngText(bytes, keyword) {
  const pngSignature = [137, 80, 78, 71, 13, 10, 26, 10];
  for (let index = 0; index < pngSignature.length; index += 1) {
    if (bytes[index] !== pngSignature[index]) {
      return null;
    }
  }

  let offset = 8;
  while (offset + 12 <= bytes.length) {
    const length = readUint32(bytes, offset);
    const type = readLatin1(bytes, offset + 4, offset + 8);
    const dataStart = offset + 8;
    const dataEnd = dataStart + length;
    const nextOffset = dataEnd + 4;
    if (dataEnd > bytes.length || nextOffset > bytes.length) {
      return null;
    }

    if (type === "tEXt") {
      const text = readLatin1(bytes, dataStart, dataEnd);
      const separatorIndex = text.indexOf("\\0");
      if (separatorIndex > -1 && text.slice(0, separatorIndex) === keyword) {
        return text.slice(separatorIndex + 1);
      }
    }

    offset = nextOffset;
  }

  return null;
}

function readUint32(bytes, offset) {
  return (
    (bytes[offset] * 0x1000000) +
    ((bytes[offset + 1] << 16) >>> 0) +
    ((bytes[offset + 2] << 8) >>> 0) +
    bytes[offset + 3]
  ) >>> 0;
}

function readLatin1(bytes, start, end) {
  let text = "";
  for (let index = start; index < end; index += 8192) {
    const chunk = bytes.slice(index, Math.min(index + 8192, end));
    text += String.fromCharCode(...chunk);
  }
  return text;
}
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
  const syncOptions = {
    targets,
    writeMetadata: options.writeMetadata,
    ...(options.sectionNodeId ? { sectionNodeOverrides: { [options.target]: options.sectionNodeId } } : {}),
    ...(roots.length > 0 ? { catalogRootFilters: { [target]: roots } } : {}),
    ...(runOptions.cleanupOnly ? { catalogCleanupOnlyTargets: [target] } : {}),
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
    payloadSchemaVersion: options.transport === "png" ? PAYLOAD_PNG_SCHEMA_VERSION : null,
    clearStagingSource: clearStagingSource.toString(),
    appendChunkSource: appendChunkSource.toString(),
    stagePayloadFromPngSource: stagePayloadFromPngSource.toString(),
    finalizeStagingSource: finalizeStagingSource.toString(),
  }));
}

function createTargetFingerprints(designModel) {
  const fingerprints = {};
  for (const target of FULL_VISUAL_TARGETS) {
    const slice = modelSliceForTarget(designModel, target);
    fingerprints[target] = sha256(stableJson(slice));

    if (!CATALOG_TARGETS.includes(target)) {
      continue;
    }

    const roots = catalogRoots(designModel, target);
    const [catalogName, treeName] = target.split(".");
    const nodes = designModel.content?.catalogs?.[catalogName]?.[treeName] || [];
    const rootKey = treeName === "libraries" ? "group" : "id";
    for (const root of roots) {
      fingerprints[`${target}.${root}`] = sha256(stableJson(
        nodes.filter((node) => node?.[rootKey] === root)
      ));
    }
    fingerprints[`${target}.cleanup`] = sha256(stableJson({ roots }));
  }
  return fingerprints;
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

function sameTargets(actual, expected) {
  return actual.length === expected.length && actual.every((target, index) => target === expected[index]);
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
