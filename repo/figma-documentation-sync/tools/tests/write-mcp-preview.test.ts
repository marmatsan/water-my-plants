import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";
import { mkdtempSync, readFileSync, writeFileSync } from "node:fs";
import { readdir, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";

const runnerPath = join(process.cwd(), "dist", "write-mcp-preview.mjs");

test("generates a sandboxed catalog preview runner with chunk transport", async () => {
  const workspace = createWorkspace("catalog-preview.js");
  try {
    runPreview([
      "--fixture=catalog-tree",
      "--target=waterMyPlants.plugins",
      "--section-node-id=1:2",
      `--script=${workspace.scriptPath}`,
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDirectory = join(
      workspace.outDir,
      "preview-preview-catalog-waterMyPlants-plugins-chunks-catalog-tree"
    );
    const manifest = JSON.parse(readFileSync(join(runDirectory, "manifest.json"), "utf8"));
    const targetSource = readFileSync(join(runDirectory, "99-run-target.mcp.js"), "utf8");
    const files = await readdir(runDirectory);

    assert.equal(manifest.mode, "preview");
    assert.equal(manifest.entrypoint, "preview-catalog");
    assert.equal(manifest.target, "waterMyPlants.plugins");
    assert.equal(manifest.transport, "chunks");
    assert.equal(manifest.writeMetadata, false);
    assert.equal(manifest.sectionNodeId, "1:2");
    assert.equal(manifest.payloadImage, null);
    assert.ok(files.some((file) => file.startsWith("10-designModelJson-")));
    assert.ok(files.some((file) => file.startsWith("20-script-")));
    assert.match(targetSource, /water_my_plants_sync_preview/);
    assert.match(targetSource, /FigmaCatalogTreePreview\.main/);
    assert.match(targetSource, /"waterMyPlants\.plugins":"1:2"/);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("generates a non-catalog preview without an official section override", async () => {
  const workspace = createWorkspace("versions-preview.js");
  try {
    runPreview([
      "--fixture=versions",
      "--target=versions",
      `--script=${workspace.scriptPath}`,
      `--out-dir=${workspace.outDir}`,
    ]);

    const runDirectory = join(
      workspace.outDir,
      "preview-trunk-sync-versions-chunks-versions"
    );
    const manifest = JSON.parse(readFileSync(join(runDirectory, "manifest.json"), "utf8"));
    const targetSource = readFileSync(join(runDirectory, "99-run-target.mcp.js"), "utf8");

    assert.equal(manifest.entrypoint, "trunk-sync");
    assert.equal(manifest.sectionNodeId, null);
    assert.match(targetSource, /"targets":\["versions"\]/);
    assert.doesNotMatch(targetSource, /writeMetadata":true/);
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

test("rejects official mode and unsafe catalog previews", async () => {
  const workspace = createWorkspace("rejected-preview.js");
  try {
    assert.throws(
      () => runPreview(["--mode=official"]),
      /Use the Kotlin prepareOfficialFigmaSync task for official runners/
    );
    assert.throws(
      () => runPreview([
        "--fixture=catalog-tree",
        "--target=waterMyPlants.plugins",
        `--script=${workspace.scriptPath}`,
        `--out-dir=${workspace.outDir}`,
      ]),
      /require --section-node-id for a sandbox section/
    );
    assert.throws(
      () => runPreview([
        "--fixture=versions",
        "--target=versions",
        "--transport=png",
        `--script=${workspace.scriptPath}`,
        `--out-dir=${workspace.outDir}`,
      ]),
      /support chunk transport only/
    );
  } finally {
    await rm(workspace.root, { recursive: true, force: true });
  }
});

function runPreview(arguments_: string[]) {
  return execFileSync(process.execPath, [runnerPath, ...arguments_], {
    cwd: process.cwd(),
    encoding: "utf8",
    stdio: ["ignore", "pipe", "pipe"],
  });
}

function createWorkspace(scriptName: string) {
  const root = mkdtempSync(join(tmpdir(), "figma-preview-test-"));
  const scriptPath = join(root, scriptName);
  const outDir = join(root, "out");
  writeFileSync(
    scriptPath,
    [
      "const DESIGN_MODEL = undefined;",
      "const SYNC_OPTIONS = undefined;",
      "globalThis.FigmaCatalogTreePreview = { main: async () => ({ preview: true }) };",
      "return { designModel: DESIGN_MODEL, options: SYNC_OPTIONS };",
    ].join("\n"),
    "utf8"
  );
  return { root, scriptPath, outDir };
}
