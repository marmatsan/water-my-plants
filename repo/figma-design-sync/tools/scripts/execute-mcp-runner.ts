import { createHash } from "node:crypto";
import { readFile, rename, rm, writeFile } from "node:fs/promises";
import { basename, dirname, join, resolve } from "node:path";
import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StreamableHTTPClientTransport } from "@modelcontextprotocol/sdk/client/streamableHttp.js";

const DEFAULT_ENDPOINT = "http://127.0.0.1:3845/mcp";
const DEFAULT_FILE_KEY = "YBZXsd8oyGLbcI2KWxJvRK";
const STATE_SCHEMA_VERSION = 1;
const REQUIRED_WRITE_TOOL = "use_figma";
const REQUIRED_UPLOAD_TOOL = "upload_assets";
const FIGMA_USE_SKILL_URI = "skill://figma/figma-use/SKILL.md";

export async function main(argv = process.argv.slice(2), dependencies = {}) {
  const options = parseArgs(argv);
  const clientFactory = dependencies.clientFactory || createSdkClient;

  if (options.probe) {
    const client = await clientFactory(options.endpoint);
    try {
      const capabilities = await probeCapabilities(client);
      console.log(JSON.stringify(capabilities, null, 2));
      return capabilities.writeCapable ? 0 : 2;
    } finally {
      await client.close();
    }
  }

  const manifestPath = resolve(options.manifest);
  const runnerDirectory = dirname(manifestPath);
  const manifest = validateManifest(JSON.parse(await readFile(manifestPath, "utf8")));
  const statePath = resolve(options.state || join(runnerDirectory, "execution-state.json"));
  const existingState = await readOptionalJson(statePath);
  const visualState = options.visualState ? await readRequiredJson(resolve(options.visualState)) : null;
  const syncPlan = options.plan ? await readRequiredJson(resolve(options.plan)) : null;
  const executionFiles = selectExecutionFiles(manifest, options, existingState, visualState, syncPlan);

  if (options.recordSuccess || options.recordFailure) {
    if (options.recordSuccess && options.recordFailure) {
      throw new Error("Use only one of --record-success or --record-failure.");
    }
    const file = options.recordSuccess || options.recordFailure;
    if (!manifest.files.includes(file)) {
      throw new Error(`Recorded file '${file}' is not in the runner manifest.`);
    }
    let state = createOrResumeState(
      manifest,
      existingState,
      { ...options, resume: existingState !== null },
      executionFiles.length > 0 ? executionFiles : existingState?.plannedFiles || manifest.files
    );
    state = options.recordSuccess
      ? recordSuccess(state, manifest, file, 0, options.summary || "Recorded by MCP operator")
      : recordFailure(state, file, 0, new Error(options.summary || "Recorded by MCP operator"));
    await writeJsonAtomic(statePath, state);
    console.log(`${options.recordSuccess ? "Completed" : "Failed"}: ${file}`);
    return 0;
  }

  if (options.next) {
    console.log(executionFiles[0] || "COMPLETE");
    return 0;
  }

  if (options.dryRun) {
    console.log(JSON.stringify({
      manifestHash: manifest.manifestHash,
      statePath,
      reuseStaging: options.reuseStaging,
      syncPlan: syncPlan ? { decision: syncPlan.decision, executionScopes: syncPlan.executionScopes } : null,
      executionFiles,
    }, null, 2));
    return 0;
  }

  const client = await clientFactory(options.endpoint);
  let state = createOrResumeState(manifest, existingState, options, executionFiles);
  try {
    const capabilities = await probeCapabilities(client);
    requireWriteCapabilities(capabilities, manifest, executionFiles);
    await loadFigmaUseGuidance(client);
    console.log(`Figma MCP: ${executionFiles.length} unit(s), ${capabilities.toolNames.length} advertised tool(s).`);

    for (let index = 0; index < executionFiles.length; index += 1) {
      const file = executionFiles[index];
      const startedAt = Date.now();
      try {
        if (file === "10-stage-payload-from-png.mcp.js") {
          await uploadPayload(client, manifest, runnerDirectory, options.fileKey);
        }

        const source = await readFile(join(runnerDirectory, file), "utf8");
        assertContentHash(manifest.fileHashes[file], source, file);
        const result = await client.callTool(REQUIRED_WRITE_TOOL, {
          fileKey: options.fileKey,
          code: source,
          description: `Water My Plants Figma Sync: ${file}`,
          skillNames: "resource:figma-use",
        });
        if (result?.isError === true) {
          throw new Error(toolResultText(result) || `Figma MCP reported an error for ${file}.`);
        }

        state = recordSuccess(state, manifest, file, Date.now() - startedAt, toolResultText(result));
        await writeJsonAtomic(statePath, state);
        reportProgress(index + 1, executionFiles.length, file, options.verbose);
      } catch (error) {
        state = recordFailure(state, file, Date.now() - startedAt, error);
        await writeJsonAtomic(statePath, state);
        throw error;
      }
    }

    console.log(`Figma MCP complete. Checkpoint: ${statePath}`);
    return 0;
  } finally {
    await client.close();
  }
}

export function validateManifest(manifest) {
  for (const key of ["schemaVersion", "modelHash", "gitSha", "writerHash", "transportHash", "manifestHash", "files", "fileHashes"]) {
    if (manifest?.[key] === undefined || manifest?.[key] === null) {
      throw new Error(`MCP manifest is missing '${key}'. Regenerate it with the current write-mcp-runner.`);
    }
  }
  if (manifest.schemaVersion < 2) {
    throw new Error(`Unsupported MCP manifest schema ${manifest.schemaVersion}; expected 2 or newer.`);
  }
  if (!Array.isArray(manifest.files) || manifest.files.some((file) => typeof file !== "string")) {
    throw new Error("MCP manifest files must be a string array.");
  }
  for (const file of manifest.files) {
    if (typeof manifest.fileHashes[file] !== "string") {
      throw new Error(`MCP manifest is missing the hash for '${file}'.`);
    }
  }
  const calculatedManifestHash = calculateManifestHash(manifest);
  if (manifest.manifestHash !== calculatedManifestHash) {
    throw new Error(`MCP manifest hash mismatch: ${manifest.manifestHash} != ${calculatedManifestHash}.`);
  }
  if (manifest.transport === "png" && typeof manifest.payloadImage?.sha256 !== "string") {
    throw new Error("PNG MCP manifest is missing 'payloadImage.sha256'.");
  }
  return manifest;
}

export function calculateManifestHash(manifest) {
  const { manifestHash: _manifestHash, ...body } = manifest;
  return hashText(stableJson(body));
}

export function assertContentHash(expectedHash, content, label) {
  const actualHash = hashText(content);
  if (actualHash !== expectedHash) {
    throw new Error(`MCP content hash mismatch for '${label}': ${actualHash} != ${expectedHash}.`);
  }
}

export async function probeCapabilities(client) {
  const tools = await client.listTools();
  const toolNames = tools.map((tool) => tool.name).sort();
  return {
    toolNames,
    canUseFigma: toolNames.includes(REQUIRED_WRITE_TOOL),
    canUploadAssets: toolNames.includes(REQUIRED_UPLOAD_TOOL),
    writeCapable: toolNames.includes(REQUIRED_WRITE_TOOL) && toolNames.includes(REQUIRED_UPLOAD_TOOL),
  };
}

export function requireWriteCapabilities(capabilities, manifest, executionFiles) {
  const missing = [];
  if (!capabilities.canUseFigma) missing.push(REQUIRED_WRITE_TOOL);
  if (needsPayloadUpload(manifest, executionFiles) && !capabilities.canUploadAssets) {
    missing.push(REQUIRED_UPLOAD_TOOL);
  }
  if (missing.length > 0) {
    throw new Error(
      `MCP endpoint is read-only for this runner; missing tool(s): ${missing.join(", ")}. ` +
      "Keep the Codex-operated Figma write path until the local endpoint advertises them."
    );
  }
}

export async function loadFigmaUseGuidance(client) {
  try {
    const resource = await client.readResource(FIGMA_USE_SKILL_URI);
    const text = (resource?.contents || [])
      .map((content) => content?.text || "")
      .join("\n")
      .trim();
    if (!text) throw new Error("resource was empty");
    return text;
  } catch (error) {
    throw new Error(
      `Write-capable Figma MCP endpoint did not provide required ${FIGMA_USE_SKILL_URI}: ` +
      `${error instanceof Error ? error.message : String(error)}`
    );
  }
}

export function selectExecutionFiles(manifest, options, existingState, visualState, syncPlan = null) {
  let files = manifest.files.filter((file) => file.endsWith(".mcp.js"));

  if (syncPlan && !manifest.writeMetadata) {
    if (syncPlan.manifestHash !== manifest.manifestHash) {
      throw new Error("Visual sync plan manifestHash does not match the runner manifest.");
    }
    if (syncPlan.decision === "none") {
      files = [];
    } else if (syncPlan.decision === "partial") {
      const scopes = new Set(syncPlan.executionScopes || []);
      files = files.filter((file) => !file.startsWith("99-") || scopes.has(manifest.executionScopes?.[file]));
    } else if (syncPlan.decision !== "full") {
      throw new Error(`Unknown visual sync plan decision '${syncPlan.decision}'.`);
    }
  }

  if (options.reuseStaging) {
    if (!manifest.writeMetadata || manifest.targets?.length !== 1 || manifest.targets[0] !== "metadata") {
      throw new Error("--reuse-staging is only valid for a metadata-only manifest.");
    }
    assertCompletedVisualState(manifest, visualState);
    files = files.filter((file) => file.startsWith("99-"));
  }

  if (options.from) {
    const index = files.indexOf(options.from);
    if (index < 0) throw new Error(`--from file '${options.from}' is not in the execution plan.`);
    files = files.slice(index);
  }

  if (options.retryFailed) {
    if (!existingState?.failedFile) {
      throw new Error("--retry-failed requires a checkpoint with failedFile.");
    }
    if (!files.includes(existingState.failedFile)) {
      throw new Error(`Checkpoint failed file '${existingState.failedFile}' is not in this manifest.`);
    }
    return [existingState.failedFile];
  }

  if (options.resume && existingState) {
    assertStateIdentity(manifest, existingState);
    const completed = new Set(existingState.completedFiles?.map((entry) => entry.file) || []);
    files = files.filter((file) => !completed.has(file));
  }

  return files;
}

export function createOrResumeState(manifest, existingState, options, executionFiles = manifest.files) {
  if ((options.resume || options.retryFailed) && existingState) {
    assertStateIdentity(manifest, existingState);
    return { ...existingState, failedFile: null, updatedAt: new Date().toISOString() };
  }
  return {
    schemaVersion: STATE_SCHEMA_VERSION,
    identity: executionIdentity(manifest),
    startedAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    completedFiles: [],
    plannedFiles: executionFiles,
    failedFile: null,
  };
}

export function recordSuccess(state, manifest, file, durationMs, summary) {
  const completedFiles = (state.completedFiles || []).filter((entry) => entry.file !== file);
  completedFiles.push({
    file,
    fileHash: manifest.fileHashes[file],
    durationMs,
    completedAt: new Date().toISOString(),
    summary: summary ? summary.slice(0, 500) : null,
  });
  return { ...state, completedFiles, failedFile: null, updatedAt: new Date().toISOString() };
}

export function recordFailure(state, file, durationMs, error) {
  return {
    ...state,
    failedFile: file,
    failure: {
      message: error instanceof Error ? error.message : String(error),
      durationMs,
      failedAt: new Date().toISOString(),
    },
    updatedAt: new Date().toISOString(),
  };
}

export function executionIdentity(manifest) {
  return {
    modelHash: manifest.modelHash,
    gitSha: manifest.gitSha,
    writerHash: manifest.writerHash,
    transportHash: manifest.transportHash,
    manifestHash: manifest.manifestHash,
  };
}

export function assertStateIdentity(manifest, state) {
  const expected = executionIdentity(manifest);
  for (const [key, value] of Object.entries(expected)) {
    if (state.identity?.[key] !== value) {
      throw new Error(`Checkpoint ${key} mismatch: ${state.identity?.[key] ?? "<missing>"} != ${value}.`);
    }
  }
}

export function assertCompletedVisualState(metadataManifest, visualState) {
  if (!visualState) {
    throw new Error("--reuse-staging requires --visual-state from the completed visual runner.");
  }
  for (const key of ["modelHash", "gitSha", "writerHash", "transportHash"]) {
    if (visualState.identity?.[key] !== metadataManifest[key]) {
      throw new Error(`Visual checkpoint ${key} does not match the metadata manifest.`);
    }
  }
  const completedVisualFiles = new Set(
    (visualState.completedFiles || []).map((entry) => entry.file).filter((file) => file.startsWith("99-"))
  );
  const plannedVisualFiles = (visualState.plannedFiles || []).filter((file) => file.startsWith("99-"));
  if (
    plannedVisualFiles.length === 0 ||
    plannedVisualFiles.some((file) => !completedVisualFiles.has(file)) ||
    visualState.failedFile
  ) {
    throw new Error("Visual checkpoint is not complete enough to authorize staging reuse.");
  }
}

function parseArgs(argv) {
  const result = {
    endpoint: DEFAULT_ENDPOINT,
    fileKey: DEFAULT_FILE_KEY,
    probe: false,
    dryRun: false,
    resume: false,
    retryFailed: false,
    reuseStaging: false,
    verbose: false,
    next: false,
    recordSuccess: null,
    recordFailure: null,
  };
  const booleanFlags = new Set(["probe", "dry-run", "resume", "retry-failed", "reuse-staging", "verbose", "next"]);
  for (let index = 0; index < argv.length; index += 1) {
    const argument = argv[index];
    if (!argument.startsWith("--")) throw new Error(`Unexpected argument '${argument}'.`);
    const [key, inlineValue] = argument.slice(2).split("=", 2);
    if (booleanFlags.has(key)) {
      result[toCamelCase(key)] = inlineValue === undefined ? true : inlineValue === "true";
      continue;
    }
    const value = inlineValue ?? argv[++index];
    if (!value || value.startsWith("--")) throw new Error(`Missing value for --${key}.`);
    result[toCamelCase(key)] = value;
  }
  if (!result.probe && !result.manifest) throw new Error("Missing --manifest.");
  if (result.retryFailed) result.resume = true;
  if (result.next) result.resume = true;
  return result;
}

function toCamelCase(value) {
  return value.replace(/-([a-z])/g, (_, character) => character.toUpperCase());
}

async function createSdkClient(endpoint) {
  const client = new Client({ name: "water-my-plants-figma-sync", version: "1.0.0" });
  const transport = new StreamableHTTPClientTransport(new URL(endpoint));
  await client.connect(transport);
  return {
    async listTools() {
      const result = await client.listTools();
      return result.tools;
    },
    async callTool(name, args) {
      return client.callTool({ name, arguments: args });
    },
    async readResource(uri) {
      return client.readResource({ uri });
    },
    async close() {
      await transport.close();
    },
  };
}

async function uploadPayload(client, manifest, runnerDirectory, fileKey) {
  if (!manifest.payloadImage?.fileName) {
    throw new Error("PNG runner manifest does not declare payloadImage.fileName.");
  }
  const response = await client.callTool(REQUIRED_UPLOAD_TOOL, { fileKey, count: 1 });
  if (response?.isError === true) throw new Error(toolResultText(response));
  const uploadUrl = findUploadUrls(response)[0];
  if (!uploadUrl) throw new Error("upload_assets did not return an upload URL.");
  const bytes = await readFile(join(runnerDirectory, manifest.payloadImage.fileName));
  assertContentHash(manifest.payloadImage.sha256, bytes, manifest.payloadImage.fileName);
  const uploadResponse = await fetch(uploadUrl, {
    method: "POST",
    headers: { "Content-Type": "image/png" },
    body: bytes,
  });
  if (!uploadResponse.ok) {
    throw new Error(`Payload upload failed with HTTP ${uploadResponse.status}.`);
  }
}

function findUploadUrls(value, urls = []) {
  if (typeof value === "string") {
    const matches = value.match(/https:\/\/[^\s"']+/g) || [];
    urls.push(...matches);
  } else if (Array.isArray(value)) {
    value.forEach((item) => findUploadUrls(item, urls));
  } else if (value && typeof value === "object") {
    Object.values(value).forEach((item) => findUploadUrls(item, urls));
  }
  return [...new Set(urls)];
}

function needsPayloadUpload(manifest, executionFiles) {
  return manifest.transport === "png" && executionFiles.includes("10-stage-payload-from-png.mcp.js");
}

function toolResultText(result) {
  return (result?.content || [])
    .filter((item) => item?.type === "text")
    .map((item) => item.text)
    .join("\n")
    .trim();
}

function reportProgress(completed, total, file, verbose) {
  if (verbose || completed === total || completed === 1 || completed % 5 === 0) {
    console.log(`[${completed}/${total}] ${file}`);
  }
}

async function readOptionalJson(path) {
  if (!path) return null;
  try {
    return JSON.parse(await readFile(path, "utf8"));
  } catch (error) {
    if (error?.code === "ENOENT") return null;
    throw error;
  }
}

async function readRequiredJson(path) {
  return JSON.parse(await readFile(path, "utf8"));
}

async function writeJsonAtomic(path, value) {
  const temporaryPath = `${path}.${process.pid}.${Date.now()}.tmp`;
  await writeFile(temporaryPath, `${JSON.stringify(value, null, 2)}\n`, "utf8");
  try {
    await rename(temporaryPath, path);
  } catch (error) {
    if (error?.code !== "EEXIST" && error?.code !== "EPERM") throw error;
    await rm(path, { force: true });
    await rename(temporaryPath, path);
  }
}

export function hashText(value) {
  return `sha256:${createHash("sha256").update(value).digest("hex")}`;
}

function stableJson(value) {
  return JSON.stringify(sortJson(value ?? null));
}

function sortJson(value) {
  if (Array.isArray(value)) return value.map(sortJson);
  if (value && typeof value === "object") {
    return Object.fromEntries(Object.keys(value).sort().map((key) => [key, sortJson(value[key])]));
  }
  return value;
}

const isMainModule = process.argv[1] && basename(process.argv[1]) === "execute-mcp-runner.mjs";
if (isMainModule) {
  process.exitCode = await main();
}
