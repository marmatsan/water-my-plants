import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";
import { mkdtempSync, readFileSync, writeFileSync } from "node:fs";
import { rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";

const toolRoot = process.cwd();
const runnerPath = join(toolRoot, "dist", "write-mcp-runner.mjs");
const contract = JSON.parse(
  readFileSync(join(toolRoot, "fixtures", "contracts", "writer-runtime-contract.json"), "utf8")
);

test("official visual and metadata runners satisfy the language-neutral runtime contract", async () => {
  const workspace = createWorkspace();

  try {
    const visualManifest = generateManifest(workspace, []);
    const metadataManifest = generateManifest(workspace, ["--target=metadata"]);

    assertManifestContract(visualManifest, contract.visual);
    assertManifestContract(metadataManifest, contract.metadata);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

function assertManifestContract(manifest, expectedExecution) {
  assert.equal(contract.schemaVersion, 1);
  assert.equal(manifest.schemaVersion, contract.manifestSchemaVersion);
  assert.equal(
    manifest.writerScopeFingerprintSchemaVersion,
    contract.writerScopeFingerprintSchemaVersion
  );
  assert.deepEqual(manifest.targets, expectedExecution.targets);
  assert.equal(manifest.transport, expectedExecution.transport);
  assert.equal(manifest.writeMetadata, expectedExecution.writeMetadata);
  assert.equal(manifest.fullVisualSync, expectedExecution.fullVisualSync);
  assert.equal(manifest.allowPartial, expectedExecution.allowPartial);

  for (const field of contract.requiredManifestFields) {
    assert.ok(Object.hasOwn(manifest, field), `Missing runner manifest field '${field}'.`);
  }
  for (const field of contract.hashFields) {
    assert.match(manifest[field], /^sha256:[a-f0-9]{64}$/, `Invalid ${field}.`);
  }
  for (const fileName of contract.sharedFiles) {
    assert.ok(manifest.files.includes(fileName), `Missing shared runner file '${fileName}'.`);
  }

  assert.equal(
    manifest.files.filter((fileName) => fileName.startsWith("99-")).length,
    expectedExecution.targetRunnerFiles.length
  );
  assert.deepEqual(Object.keys(manifest.executionScopes), expectedExecution.targetRunnerFiles);
  assert.deepEqual(Object.keys(manifest.targetFingerprints), contract.targetFingerprintScopes);
}

function generateManifest(workspace, extraArguments) {
  execFileSync(
    process.execPath,
    [
      runnerPath,
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      `--ci-visual-plan=${workspace.ciVisualPlanPath}`,
      `--out-dir=${workspace.outDir}`,
      ...extraArguments,
    ],
    { cwd: toolRoot, stdio: ["ignore", "pipe", "pipe"] }
  );

  const runName = extraArguments.length === 0 ? "all-visual" : "metadata";
  const runDirectory = join(
    workspace.outDir,
    `official-trunk-sync-${runName}-png-design-model-json`
  );
  return JSON.parse(readFileSync(join(runDirectory, "manifest.json"), "utf8"));
}

function createWorkspace() {
  const root = mkdtempSync(join(tmpdir(), "figma-writer-contract-"));
  const modelPath = join(root, "design-model.json");
  const scriptPath = join(root, "sync-script.mcp.js");
  const ciVisualPlanPath = join(root, "ci-visual-plan.json");
  const outDir = join(root, "out");

  writeFileSync(
    modelPath,
    JSON.stringify({
      branch: "main",
      gitSha: "contract-git-sha",
      modelHash: "sha256:contract-model-hash",
      content: {
        catalogs: {
          waterMyPlants: {
            libraries: [{ group: "androidx" }, { group: "com" }],
            plugins: [{ id: "com" }],
            customGradleConventionPlugins: [{ id: "com" }],
            customGradlePlugins: [{ id: "com" }],
          },
          gradlePlugins: {
            libraries: [{ group: "org" }],
          },
          figmaDesignSync: {
            libraries: [{ group: "io" }],
            plugins: [{ id: "com" }],
          },
        },
      },
    }),
    "utf8"
  );
  writeFileSync(
    ciVisualPlanPath,
    JSON.stringify({
      parentName: "Continuous Integration and Design Documentation",
      sections: contract.visual.targets
        .filter((target) => target.startsWith("ci."))
        .map((target) => ({
          target,
          name: target,
          description: target,
          orientation: "horizontal",
          headerSources: [],
          nodes: [],
          connections: [],
        })),
    }),
    "utf8"
  );
  writeFileSync(
    scriptPath,
    [
      "const DESIGN_MODEL = undefined;",
      "const SYNC_OPTIONS = undefined;",
      "return { modelHash: DESIGN_MODEL.modelHash, syncOptions: SYNC_OPTIONS };",
    ].join("\n"),
    "utf8"
  );

  return { root, modelPath, scriptPath, ciVisualPlanPath, outDir };
}
