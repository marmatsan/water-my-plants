import assert from "node:assert/strict";
import { mkdtemp, readFile, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { dirname, join, resolve } from "node:path";
import { spawnSync } from "node:child_process";
import test from "node:test";
import { fileURLToPath } from "node:url";

const toolRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const projectConfig = process.env.FIGMA_DESIGN_SYNC_PROJECT_CONFIG;

test("materializes a writer from the Kotlin-generated JSON project config", async () => {
  assert.ok(projectConfig, "FIGMA_DESIGN_SYNC_PROJECT_CONFIG is required.");
  const outputRoot = await mkdtemp(join(tmpdir(), "figma-design-sync-tools-"));
  try {
    const childEnvironment = { ...process.env };
    delete childEnvironment.FIGMA_DESIGN_SYNC_PROJECT_CONFIG;
    const result = spawnSync(
      process.execPath,
      [
        join(toolRoot, "bin", "build.mjs"),
        `--project-config-json=${projectConfig}`,
        `--output-dir=${outputRoot}`,
      ],
      { cwd: toolRoot, encoding: "utf8", env: childEnvironment }
    );

    assert.equal(result.status, 0, result.stderr || result.stdout);
    const generatedWriter = await readFile(
      join(outputRoot, "sync-trunk-design-model.mcp.js"),
      "utf8"
    );
    assert.match(generatedWriter, /Generated from sync-trunk-design-model\.mcp\.ts/);
    assert.doesNotMatch(generatedWriter, /@figma-design-sync\/project-config/);

    const runner = await readFile(join(outputRoot, "dist", "write-mcp-runner.mjs"), "utf8");
    assert.match(runner, /water_my_plants_sync/);
    assert.doesNotMatch(runner, /@figma-design-sync\/project-config/);
  } finally {
    await rm(outputRoot, { recursive: true, force: true });
  }
});
