import assert from "node:assert/strict";
import test from "node:test";
import {
  assertContentHash,
  assertStateIdentity,
  calculateManifestHash,
  createOrResumeState,
  executionIdentity,
  hashText,
  loadFigmaUseGuidance,
  probeCapabilities,
  recordFailure,
  recordSuccess,
  requireWriteCapabilities,
  selectExecutionFiles,
  validateManifest,
} from "../scripts/execute-mcp-runner";

const manifestBody = {
  schemaVersion: 2,
  mode: "official",
  targets: ["preflight", "versions"],
  writeMetadata: false,
  transport: "png",
  modelHash: "sha256:model",
  gitSha: "abc123",
  writerHash: "sha256:writer",
  transportHash: "sha256:transport",
  payloadImage: {
    fileName: "10-official-sync-payload.png",
    sha256: "sha256:payload",
  },
  files: [
    "00-clear-staging.mcp.js",
    "10-stage-payload-from-png.mcp.js",
    "90-finalize-staging.mcp.js",
    "99-00-preflight.mcp.js",
    "99-01-versions.mcp.js",
  ],
  fileHashes: {
    "00-clear-staging.mcp.js": hashText("clear"),
    "10-stage-payload-from-png.mcp.js": "sha256:10",
    "90-finalize-staging.mcp.js": "sha256:90",
    "99-00-preflight.mcp.js": "sha256:99-00",
    "99-01-versions.mcp.js": "sha256:99-01",
  },
};
const manifest = {
  ...manifestBody,
  manifestHash: calculateManifestHash(manifestBody),
};

const defaultOptions = {
  resume: false,
  retryFailed: false,
  reuseStaging: false,
  from: null,
};

test("capability probe distinguishes read-only and write-capable MCP endpoints", async () => {
  const readOnly = await probeCapabilities(fakeClient(["get_metadata", "get_screenshot"]));
  const writable = await probeCapabilities(fakeClient(["get_metadata", "use_figma", "upload_assets"]));

  assert.equal(readOnly.writeCapable, false);
  assert.equal(writable.writeCapable, true);
  assert.throws(
    () => requireWriteCapabilities(readOnly, manifest, manifest.files),
    /read-only.*use_figma.*upload_assets/
  );
});

test("write execution requires the official figma-use guidance resource", async () => {
  const guidance = await loadFigmaUseGuidance({
    async readResource(uri) {
      assert.equal(uri, "skill://figma/figma-use/SKILL.md");
      return { contents: [{ text: "Use Figma safely." }] };
    },
  });

  assert.equal(guidance, "Use Figma safely.");
  await assert.rejects(
    () => loadFigmaUseGuidance({ async readResource() { throw new Error("not found"); } }),
    /required skill:\/\/figma\/figma-use\/SKILL\.md/
  );
});

test("manifest validation requires cryptographic execution identity", () => {
  assert.equal(validateManifest(manifest), manifest);
  assert.throws(() => validateManifest({ ...manifest, writerHash: undefined }), /missing 'writerHash'/);
  assert.throws(() => validateManifest({ ...manifest, manifestHash: "sha256:stale" }), /manifest hash mismatch/);
  assert.doesNotThrow(() => assertContentHash(manifest.fileHashes["00-clear-staging.mcp.js"], "clear", "clear"));
  assert.throws(
    () => assertContentHash(manifest.fileHashes["00-clear-staging.mcp.js"], "changed", "clear"),
    /content hash mismatch/
  );
});

test("manifest schema 3 requires scoped writer fingerprint identity", () => {
  const schema3Body = {
    ...manifestBody,
    schemaVersion: 3,
    writerScopeFingerprints: {
      preflight: "sha256:preflight-writer",
      versions: "sha256:versions-writer",
      metadata: "sha256:metadata-writer",
    },
    writerScopeFingerprintSchemaVersion: 1,
    targetFingerprints: {
      preflight: "sha256:model-preflight",
      versions: "sha256:model-versions",
    },
    executionScopes: {
      "99-00-preflight.mcp.js": "preflight",
      "99-01-versions.mcp.js": "versions",
    },
  };
  const schema3Manifest = {
    ...schema3Body,
    manifestHash: calculateManifestHash(schema3Body),
  };

  assert.equal(validateManifest(schema3Manifest), schema3Manifest);
  assert.throws(
    () => validateManifest({ ...schema3Manifest, writerScopeFingerprints: undefined }),
    /missing 'writerScopeFingerprints'/
  );
  const incompleteBody = {
    ...schema3Body,
    writerScopeFingerprints: { ...schema3Body.writerScopeFingerprints, versions: undefined },
  };
  assert.throws(
    () => validateManifest({
      ...incompleteBody,
      manifestHash: calculateManifestHash(incompleteBody),
    }),
    /missing the writer fingerprint for scope 'versions'/
  );
});

test("resume skips completed files with the same execution identity", () => {
  let state = createOrResumeState(manifest, null, defaultOptions);
  state = recordSuccess(state, manifest, "00-clear-staging.mcp.js", 10, "ok");

  const files = selectExecutionFiles(
    manifest,
    { ...defaultOptions, resume: true },
    state,
    null
  );

  assert.deepEqual(files, manifest.files.slice(1));
});

test("retry-failed selects only the atomic failed unit", () => {
  let state = createOrResumeState(manifest, null, defaultOptions);
  state = recordFailure(state, "99-00-preflight.mcp.js", 25, new Error("timeout"));

  const files = selectExecutionFiles(
    manifest,
    { ...defaultOptions, resume: true, retryFailed: true },
    state,
    null
  );

  assert.deepEqual(files, ["99-00-preflight.mcp.js"]);
});

test("checkpoint identity invalidates resume when the writer changes", () => {
  const state = {
    identity: executionIdentity(manifest),
    completedFiles: [],
  };

  assertStateIdentity(manifest, state);
  assert.throws(
    () => assertStateIdentity({ ...manifest, writerHash: "sha256:new-writer" }, state),
    /writerHash mismatch/
  );
});

test("metadata staging reuse executes only metadata after a completed matching visual state", () => {
  const metadataManifest = {
    ...manifest,
    targets: ["metadata"],
    writeMetadata: true,
    manifestHash: "sha256:metadata-manifest",
    files: [
      "00-clear-staging.mcp.js",
      "10-stage-payload-from-png.mcp.js",
      "90-finalize-staging.mcp.js",
      "99-run-target.mcp.js",
    ],
  };
  const visualState = {
    identity: executionIdentity(manifest),
    plannedFiles: manifest.files,
    completedFiles: [
      { file: "99-00-preflight.mcp.js" },
      { file: "99-01-versions.mcp.js" },
    ],
    failedFile: null,
  };

  const files = selectExecutionFiles(
    metadataManifest,
    { ...defaultOptions, reuseStaging: true },
    null,
    visualState
  );

  assert.deepEqual(files, ["99-run-target.mcp.js"]);
});

test("metadata staging reuse rejects missing or incompatible visual checkpoints", () => {
  const metadataManifest = {
    ...manifest,
    targets: ["metadata"],
    writeMetadata: true,
    files: ["99-run-target.mcp.js"],
  };

  assert.throws(
    () => selectExecutionFiles(metadataManifest, { ...defaultOptions, reuseStaging: true }, null, null),
    /requires --visual-state/
  );
});

test("partial visual plan keeps staging and only selected execution scopes", () => {
  const scopedManifest = {
    ...manifest,
    executionScopes: {
      "99-00-preflight.mcp.js": "preflight",
      "99-01-versions.mcp.js": "versions",
    },
  };
  const files = selectExecutionFiles(
    scopedManifest,
    defaultOptions,
    null,
    null,
    {
      decision: "partial",
      manifestHash: manifest.manifestHash,
      executionScopes: ["preflight"],
    }
  );

  assert.deepEqual(files, manifest.files.slice(0, 4));
});

function fakeClient(toolNames: string[]) {
  return {
    async listTools() {
      return toolNames.map((name) => ({ name }));
    },
  };
}
