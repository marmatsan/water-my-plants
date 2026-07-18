import { createHash } from "node:crypto";
import { readFile, writeFile } from "node:fs/promises";
import { basename, resolve } from "node:path";

const FIGMA_FILE_KEY = "YBZXsd8oyGLbcI2KWxJvRK";
const METADATA_PAGE_ID = "62934:908";
const METADATA_NAMESPACE = "water_my_plants_sync";

export async function main(argv = process.argv.slice(2), dependencies = {}) {
  const options = parseArgs(argv);
  const manifest = JSON.parse(await readFile(resolve(options.manifest), "utf8"));
  const metadata = await readPreviousMetadata(options, dependencies.fetch || fetch);
  const plan = createVisualSyncPlan(manifest, metadata);
  await writeFile(resolve(options.out), `${JSON.stringify(plan, null, 2)}\n`, "utf8");
  console.log(`Visual sync plan: ${plan.decision} (${plan.reason}).`);
  return plan;
}

export function createVisualSyncPlan(manifest, previousMetadata) {
  const allScopes = Object.values(manifest.executionScopes || {});
  const requiredWriterScopes = [...new Set([...allScopes, "metadata"])];
  if (
    !manifest.targetFingerprints ||
    !manifest.writerScopeFingerprintSchemaVersion ||
    !hasFingerprintEntries(manifest.writerScopeFingerprints, requiredWriterScopes)
  ) {
    throw new Error(
      "Current MCP manifest is missing complete model-target or writer-scope fingerprints."
    );
  }
  const identity = {
    modelHash: manifest.modelHash,
    writerHash: manifest.writerHash,
    transportHash: manifest.transportHash,
    writerScopeFingerprintSchemaVersion: manifest.writerScopeFingerprintSchemaVersion,
  };

  if (!previousMetadata) {
    return plan("full", "figma-metadata-unavailable", identity, allScopes, manifest);
  }
  if (
    !previousMetadata.writerHash ||
    !previousMetadata.targetFingerprints ||
    !previousMetadata.writerScopeFingerprintSchemaVersion ||
    !hasFingerprintEntries(previousMetadata.writerScopeFingerprints, requiredWriterScopes)
  ) {
    return plan("full", "legacy-metadata-without-execution-fingerprints", identity, allScopes, manifest);
  }
  if (
    previousMetadata.writerScopeFingerprintSchemaVersion !==
    manifest.writerScopeFingerprintSchemaVersion
  ) {
    return plan("full", "writer-scope-fingerprint-schema-changed", identity, allScopes, manifest);
  }
  const modelChanged = previousMetadata.modelHash !== manifest.modelHash;
  const writerChanged = previousMetadata.writerHash !== manifest.writerHash;
  if (!modelChanged && !writerChanged) {
    return plan("none", "visual-input-unchanged", identity, [], manifest);
  }

  const modelChangedScopes = modelChanged
    ? allScopes.filter((scope) =>
        scope !== "preflight" &&
        manifest.targetFingerprints?.[scope] !== previousMetadata.targetFingerprints?.[scope]
      )
    : [];
  if (modelChanged && modelChangedScopes.length === 0) {
    return plan(
      "full",
      "model-changed-outside-known-target-fingerprints",
      identity,
      allScopes,
      manifest
    );
  }

  const writerChangedScopes = writerChanged
    ? allScopes.filter((scope) =>
        manifest.writerScopeFingerprints?.[scope] !==
        previousMetadata.writerScopeFingerprints?.[scope]
      )
    : [];
  const metadataWriterChanged = writerChanged &&
    manifest.writerScopeFingerprints?.metadata !==
      previousMetadata.writerScopeFingerprints?.metadata;
  if (writerChanged && writerChangedScopes.length === 0 && !metadataWriterChanged) {
    return plan(
      "full",
      "writer-changed-outside-known-scope-fingerprints",
      identity,
      allScopes,
      manifest
    );
  }
  if (writerChangedScopes.length === allScopes.length) {
    return plan("full", "shared-visual-writer-changed", identity, allScopes, manifest);
  }

  const scopes = [...new Set(["preflight", ...modelChangedScopes, ...writerChangedScopes])];
  const reason = modelChanged && writerChanged
    ? "target-model-and-writer-fingerprints-changed"
    : writerChanged
      ? metadataWriterChanged && writerChangedScopes.length === 0
        ? "metadata-writer-fingerprint-changed"
        : "writer-scope-fingerprints-changed"
      : "target-model-fingerprints-changed";
  return plan("partial", reason, identity, scopes, manifest);
}

function plan(decision, reason, identity, executionScopes, manifest) {
  const body = {
    schemaVersion: 1,
    decision,
    reason,
    requiresVisualWrite: decision !== "none",
    requiresMetadataWrite: decision !== "none",
    executionScopes,
    identity,
    manifestHash: manifest.manifestHash,
  };
  return { ...body, planHash: hash(JSON.stringify(sortJson(body))) };
}

async function readPreviousMetadata(options, fetchFn) {
  if (options.previousMetadata) {
    return normalizeMetadata(JSON.parse(await readFile(resolve(options.previousMetadata), "utf8")));
  }

  const token = process.env[options.figmaTokenEnv];
  if (!token) return null;
  const url = new URL(`https://api.figma.com/v1/files/${FIGMA_FILE_KEY}/nodes`);
  url.searchParams.set("ids", METADATA_PAGE_ID);
  url.searchParams.set("plugin_data", "shared");
  try {
    const response = await fetchFn(url, { headers: { "X-Figma-Token": token } });
    if (!response.ok) return null;
    const body = await response.json();
    const metadata = body.nodes?.[METADATA_PAGE_ID]?.document?.sharedPluginData?.[METADATA_NAMESPACE];
    return normalizeMetadata(metadata);
  } catch {
    return null;
  }
}

function normalizeMetadata(metadata) {
  if (!metadata) return null;
  return {
    ...metadata,
    targetFingerprints: parseJsonObject(metadata.targetFingerprints),
    writerScopeFingerprints: parseJsonObject(metadata.writerScopeFingerprints),
    writerScopeFingerprintSchemaVersion: Number(
      metadata.writerScopeFingerprintSchemaVersion
    ) || null,
  };
}

function parseJsonObject(value) {
  if (typeof value !== "string") return value;
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function hasFingerprintEntries(fingerprints, scopes) {
  return Boolean(
    fingerprints &&
    typeof fingerprints === "object" &&
    !Array.isArray(fingerprints) &&
    scopes.every((scope) => typeof fingerprints[scope] === "string" && fingerprints[scope])
  );
}

function parseArgs(argv) {
  const result = { figmaTokenEnv: "FIGMA_FILE_CONTENT_ACCESS_TOKEN" };
  for (let index = 0; index < argv.length; index += 1) {
    const [key, inlineValue] = argv[index].replace(/^--/, "").split("=", 2);
    const value = inlineValue ?? argv[++index];
    if (!value || value.startsWith("--")) throw new Error(`Missing value for --${key}.`);
    result[toCamelCase(key)] = value;
  }
  if (!result.manifest) throw new Error("Missing --manifest.");
  if (!result.out) throw new Error("Missing --out.");
  return result;
}

function toCamelCase(value) {
  return value.replace(/-([a-z])/g, (_, character) => character.toUpperCase());
}

function hash(value) {
  return `sha256:${createHash("sha256").update(value).digest("hex")}`;
}

function sortJson(value) {
  if (Array.isArray(value)) return value.map(sortJson);
  if (value && typeof value === "object") {
    return Object.fromEntries(Object.keys(value).sort().map((key) => [key, sortJson(value[key])]));
  }
  return value;
}

if (process.argv[1] && basename(process.argv[1]) === "write-visual-sync-plan.mjs") {
  await main();
}
