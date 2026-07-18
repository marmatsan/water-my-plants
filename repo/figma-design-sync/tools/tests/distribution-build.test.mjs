import assert from "node:assert/strict";
import { mkdtemp, readFile, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { dirname, join, resolve } from "node:path";
import { spawnSync } from "node:child_process";
import test from "node:test";
import { fileURLToPath, pathToFileURL } from "node:url";
import { build } from "esbuild";

const toolRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const projectConfig = resolve(
  toolRoot,
  "..",
  "project-config",
  "water-my-plants",
  "figma-config.ts"
);

test("materializes a project-configured writer outside the package", async () => {
  const outputRoot = await mkdtemp(join(tmpdir(), "figma-design-sync-tools-"));
  try {
    const result = spawnSync(
      process.execPath,
      [
        join(toolRoot, "bin", "build.mjs"),
        `--project-config=${projectConfig}`,
        `--output-dir=${outputRoot}`,
      ],
      { cwd: toolRoot, encoding: "utf8" }
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

test("materializes a writer from a language-neutral JSON project config", async () => {
  const outputRoot = await mkdtemp(join(tmpdir(), "figma-design-sync-json-tools-"));
  try {
    const jsonConfig = join(outputRoot, "writer-project-config.json");
    const bundledConfig = join(outputRoot, "writer-project-config.mjs");
    await build({
      bundle: true,
      entryPoints: [projectConfig],
      format: "esm",
      outfile: bundledConfig,
      platform: "node",
      target: "node18",
    });
    const typescriptConfig = await import(pathToFileURL(bundledConfig).href);
    const catalogTreeTargets = typescriptConfig.CATALOG_TREE_TARGETS.map(
      ({ nodes: _nodes, ...target }) => ({
        ...target,
        nodesPath: ["content", "catalogs", ...target.name.split(".")],
      })
    );
    await writeFile(
      jsonConfig,
      JSON.stringify({ ...typescriptConfig, schemaVersion: 1, CATALOG_TREE_TARGETS: catalogTreeTargets })
    );

    const result = spawnSync(
      process.execPath,
      [
        join(toolRoot, "bin", "build.mjs"),
        `--project-config-json=${jsonConfig}`,
        `--output-dir=${outputRoot}`,
      ],
      { cwd: toolRoot, encoding: "utf8" }
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
