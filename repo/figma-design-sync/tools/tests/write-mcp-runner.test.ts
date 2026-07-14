import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";
import { mkdtempSync, readFileSync, writeFileSync } from "node:fs";
import { readdir, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";

const runnerPath = join(process.cwd(), "dist", "write-mcp-runner.mjs");

test("official runner defaults to PNG transport and supports atomic preflight plus visual target", async () => {
  const workspace = createRunnerFixture();

  try {
    runRunner([
      "--mode=official",
      `--model=${workspace.modelPath}`,
      `--script=${workspace.scriptPath}`,
      "--targets=preflight,waterMyPlants.libraries",
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

    const runTargetSource = readFileSync(join(runDir, "99-run-target.mcp.js"), "utf8");
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
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDir = join(workspace.outDir, "official-trunk-sync-preflight-ci-overview-png-design-model-json");
    const manifest = readManifest(runDir);
    assert.deepEqual(manifest.targets, ["preflight", "ci.overview"]);
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
    assert.ok(files.some((fileName) => fileName.startsWith("20-scriptBase64-")));
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
      content: {},
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
