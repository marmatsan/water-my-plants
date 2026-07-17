import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";
import { mkdtempSync, readFileSync, writeFileSync } from "node:fs";
import { readdir, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";

const runnerPath = join(process.cwd(), "dist", "write-mcp-runner.mjs");

const FULL_VISUAL_TARGETS = [
  "preflight",
  "headers",
  "versions",
  "waterMyPlants.libraries",
  "waterMyPlants.plugins",
  "waterMyPlants.customGradleConventionPlugins",
  "waterMyPlants.customGradlePlugins",
  "gradlePlugins.libraries",
  "gradlePlugins.plugins",
  "figmaDesignSync.libraries",
  "figmaDesignSync.plugins",
  "ci.overview",
  "ci.pullRequestIntegration",
  "ci.postMergeDesignDocumentation",
  "ci.infrastructureAndAccess",
  "ci.windowsRuntime",
];

test("official runner defaults to the complete visual sync without metadata", async () => {
  const workspace = createRunnerFixture();

  try {
    runRunner([
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDir = join(
      workspace.outDir,
      "official-trunk-sync-all-visual-png-design-model-json"
    );
    const files = await readdir(runDir);
    const manifest = readManifest(runDir);
    const preflightSource = readFileSync(join(runDir, "99-00-preflight.mcp.js"), "utf8");
    const versionsSource = readFileSync(join(runDir, "99-02-versions.mcp.js"), "utf8");
    const androidxSource = readFileSync(
      join(runDir, "99-03-00-waterMyPlants-libraries-androidx.mcp.js"),
      "utf8"
    );
    const libraryCleanupSource = readFileSync(
      join(runDir, "99-03-99-waterMyPlants-libraries-cleanup.mcp.js"),
      "utf8"
    );
    const runtimeSource = readFileSync(join(runDir, "99-15-ci-windowsRuntime.mcp.js"), "utf8");

    assert.deepEqual(manifest.targets, FULL_VISUAL_TARGETS);
    assert.equal(manifest.fullVisualSync, true);
    assert.equal(manifest.allowPartial, false);
    assert.equal(manifest.writeMetadata, false);
    assert.ok(!files.includes("99-run-target.mcp.js"));
    assert.equal(files.filter((fileName) => fileName.startsWith("99-")).length, 24);
    assert.match(preflightSource, /"targets":\["preflight"\]/);
    assert.match(versionsSource, /"targets":\["versions"\]/);
    assert.match(
      androidxSource,
      /"catalogRootFilters":\{"waterMyPlants\.libraries":\["androidx"\]\}/
    );
    assert.match(
      libraryCleanupSource,
      /"catalogCleanupOnlyTargets":\["waterMyPlants\.libraries"\]/
    );
    assert.match(runtimeSource, /"targets":\["ci\.windowsRuntime"\]/);
    assert.doesNotMatch(runtimeSource, /writeMetadata":true/);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("official all alias selects the same complete visual target set", async () => {
  const workspace = createRunnerFixture();

  try {
    runRunner([
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      "--target=all",
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDir = join(
      workspace.outDir,
      "official-trunk-sync-all-visual-png-design-model-json"
    );
    const manifest = readManifest(runDir);

    assert.deepEqual(manifest.targets, FULL_VISUAL_TARGETS);
    assert.equal(manifest.fullVisualSync, true);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("official runner requires an explicit override for partial visual diagnosis", async () => {
  const workspace = createRunnerFixture();

  try {
    assert.throws(
      () => runRunner([
        "--mode=official",
        `--model=${workspace.modelPath}`,
        `--script=${workspace.scriptPath}`,
        "--targets=preflight,waterMyPlants.libraries",
        `--out-dir=${workspace.outDir}`,
      ]),
      /must target the complete visual model/
    );
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("official runner supports an explicitly partial diagnostic target", async () => {
  const workspace = createRunnerFixture();

  try {
    runRunner([
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      "--targets=preflight,waterMyPlants.libraries",
      "--allow-partial=true",
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDir = join(
      workspace.outDir,
      "official-trunk-sync-preflight-waterMyPlants-libraries-png-design-model-json"
    );
    const files = await readdir(runDir);
    const manifest = readManifest(runDir);

    assert.equal(manifest.transport, "png");
    assert.deepEqual(manifest.targets, ["preflight", "waterMyPlants.libraries"]);
    assert.equal(manifest.writeMetadata, false);
    assert.equal(manifest.fullVisualSync, false);
    assert.equal(manifest.allowPartial, true);
    assert.equal(manifest.payloadImage.fileName, "10-official-sync-payload.png");
    assert.deepEqual(
      manifest.files,
      [
        "00-clear-staging.mcp.js",
        "10-stage-payload-from-png.mcp.js",
        "90-finalize-staging.mcp.js",
        "99-run-target.mcp.js",
      ]
    );
    assert.ok(files.includes("10-official-sync-payload.png"));

    const stageSource = readFileSync(join(runDir, "10-stage-payload-from-png.mcp.js"), "utf8");
    const runTargetSource = readFileSync(join(runDir, "99-run-target.mcp.js"), "utf8");
    assert.match(stageSource, /payload\.payloadSchemaVersion === expected\.payloadSchemaVersion/);
    assert.match(stageSource, /setSharedPluginData\(namespace, "script", payload\.script\)/);
    assert.match(stageSource, /for \(const documentPage of figma\.root\.children\)/);
    assert.match(stageSource, /for \(const node of documentPage\.children\)/);
    assert.doesNotMatch(stageSource, /figma\.root\.findAll/);
    assert.doesNotMatch(stageSource, /loadAllPagesAsync/);
    assert.doesNotMatch(stageSource, /setSharedPluginData\(namespace, "scriptBase64"/);
    assert.match(runTargetSource, /getSharedPluginData\(namespace, "script"\)/);
    assert.doesNotMatch(runTargetSource, /getSharedPluginData\(namespace, "scriptBase64"\)/);
    assert.match(runTargetSource, /"targets":\["preflight","waterMyPlants\.libraries"\]/);
    assert.doesNotMatch(runTargetSource, /writeMetadata":true/);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("metadata target cannot be combined with visual targets", async () => {
  const workspace = createRunnerFixture();

  try {
    assert.throws(
      () => runRunner([
        "--mode=official",
        `--model=${workspace.modelPath}`,
        `--script=${workspace.scriptPath}`,
        "--targets=preflight,metadata",
        `--out-dir=${workspace.outDir}`,
      ]),
      /metadata target must run alone/
    );
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("official runner accepts preflight plus the granular headers target", async () => {
  const workspace = createRunnerFixture();
  try {
    runRunner([
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      "--targets=preflight,headers",
      "--allow-partial=true",
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDir = join(workspace.outDir, "official-trunk-sync-preflight-headers-png-design-model-json");
    const manifest = readManifest(runDir);
    assert.deepEqual(manifest.targets, ["preflight", "headers"]);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("official runner accepts preflight plus a granular CI documentation target", async () => {
  const workspace = createRunnerFixture();
  try {
    runRunner([
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      "--targets=preflight,ci.overview",
      "--allow-partial=true",
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDir = join(workspace.outDir, "official-trunk-sync-preflight-ci-overview-png-design-model-json");
    const manifest = readManifest(runDir);
    assert.deepEqual(manifest.targets, ["preflight", "ci.overview"]);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("official runner accepts the granular Windows runtime target", async () => {
  const workspace = createRunnerFixture();
  try {
    runRunner([
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      "--targets=preflight,ci.windowsRuntime",
      "--allow-partial=true",
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDir = join(
      workspace.outDir,
      "official-trunk-sync-preflight-ci-windowsRuntime-png-design-model-json"
    );
    const manifest = readManifest(runDir);
    assert.deepEqual(manifest.targets, ["preflight", "ci.windowsRuntime"]);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("official runner accepts a root-qualified catalog execution scope", async () => {
  const workspace = createRunnerFixture();
  try {
    runRunner([
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      "--target=waterMyPlants.libraries.androidx",
      "--allow-partial=true",
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDir = join(workspace.outDir, "official-trunk-sync-waterMyPlants-libraries-png-design-model-json");
    const source = readFileSync(join(runDir, "99-run-target.mcp.js"), "utf8");
    assert.match(source, /"catalogRootFilters":\{"waterMyPlants\.libraries":\["androidx"\]\}/);
    assert.match(source, /executionScope: "waterMyPlants\.libraries\.androidx"/);
    assert.match(source, /modelTarget: "waterMyPlants\.libraries"/);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("official runner can explicitly use chunk transport fallback", async () => {
  const workspace = createRunnerFixture();

  try {
    runRunner([
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      "--target=preflight",
      "--allow-partial=true",
      "--transport=chunks",
      "--chunk-size=1000",
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDir = join(workspace.outDir, "official-trunk-sync-preflight-chunks-design-model-json");
    const manifest = readManifest(runDir);
    const files = await readdir(runDir);

    assert.equal(manifest.transport, "chunks");
    assert.equal(manifest.payloadImage, null);
    assert.ok(files.some((fileName) => fileName.startsWith("10-designModelJson-")));
    assert.ok(files.some((fileName) => fileName.startsWith("20-script-")));
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("runner rejects staging entries that exceed Figma shared plugin data limits", async () => {
  const workspace = createRunnerFixture();

  try {
    writeFileSync(workspace.scriptPath, "x".repeat(100_001), "utf8");

    assert.throws(
      () => runRunner([
        "--mode=official",
        `--model=${workspace.modelPath}`,
        `--script=${workspace.scriptPath}`,
        "--target=preflight",
        "--allow-partial=true",
        `--out-dir=${workspace.outDir}`,
      ]),
      /script is 100001 characters and exceeds the 100000-character sharedPluginData staging limit/
    );
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

function runRunner(args: string[]) {
  return execFileSync(process.execPath, [runnerPath, ...args], {
    cwd: process.cwd(),
    encoding: "utf8",
    stdio: ["ignore", "pipe", "pipe"],
  });
}

function createRunnerFixture() {
  const root = mkdtempSync(join(tmpdir(), "figma-runner-test-"));
  const modelPath = join(root, "design-model.json");
  const scriptPath = join(root, "sync-script.mcp.js");
  const outDir = join(root, "out");

  writeFileSync(
    modelPath,
    JSON.stringify({
      branch: "main",
      gitSha: "git-sha",
      modelHash: "sha256:model-hash",
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
    scriptPath,
    [
      "const DESIGN_MODEL = undefined;",
      "const SYNC_OPTIONS = undefined;",
      "return { modelHash: DESIGN_MODEL.modelHash, syncOptions: SYNC_OPTIONS };",
    ].join("\n"),
    "utf8"
  );

  return {
    root,
    modelPath,
    scriptPath,
    outDir,
  };
}

function readManifest(runDir: string) {
  return JSON.parse(readFileSync(join(runDir, "manifest.json"), "utf8"));
}
