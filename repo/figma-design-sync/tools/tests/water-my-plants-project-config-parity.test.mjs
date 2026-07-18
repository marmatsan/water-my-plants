import assert from "node:assert/strict";
import { spawnSync } from "node:child_process";
import { mkdtemp, readFile, rm } from "node:fs/promises";
import { tmpdir } from "node:os";
import { dirname, join, resolve } from "node:path";
import test from "node:test";
import { fileURLToPath, pathToFileURL } from "node:url";
import { build } from "esbuild";

const toolRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const repositoryRoot = resolve(toolRoot, "..", "..", "..");
const typescriptConfigPath = resolve(
  toolRoot,
  "..",
  "project-config",
  "water-my-plants",
  "figma-config.ts"
);
const jsonConfigPath = resolve(
  repositoryRoot,
  "build",
  "generated",
  "figma-design-sync",
  "writer-project-config.json"
);

test("keeps the Kotlin JSON projection equivalent to the transitional TypeScript config", async () => {
  generateKotlinProjectConfig();
  const temporaryDirectory = await mkdtemp(join(tmpdir(), "figma-project-config-parity-"));
  try {
    const bundledConfig = join(temporaryDirectory, "figma-config.mjs");
    await build({
      bundle: true,
      entryPoints: [typescriptConfigPath],
      format: "esm",
      outfile: bundledConfig,
      platform: "node",
      target: "node18",
    });
    const typescriptConfig = await import(pathToFileURL(bundledConfig).href);
    const expected = JSON.parse(JSON.stringify({
      ...typescriptConfig,
      CATALOG_TREE_TARGETS: typescriptConfig.CATALOG_TREE_TARGETS.map(
        ({ nodes: _nodes, ...target }) => ({
          ...target,
          nodesPath: ["content", "catalogs", ...target.name.split(".")],
        })
      ),
    }));
    const actual = JSON.parse(await readFile(jsonConfigPath, "utf8"));
    delete actual.schemaVersion;

    assert.deepEqual(actual, expected);
  } finally {
    await rm(temporaryDirectory, { recursive: true, force: true });
  }
});

function generateKotlinProjectConfig() {
  const gradleCommand = process.platform === "win32"
    ? {
        executable: "cmd.exe",
        args: ["/d", "/c", join(repositoryRoot, "gradlew.bat"), "writeFigmaWriterProjectConfig"],
      }
    : {
        executable: join(repositoryRoot, "gradlew"),
        args: ["writeFigmaWriterProjectConfig"],
      };
  const result = spawnSync(gradleCommand.executable, gradleCommand.args, {
    cwd: repositoryRoot,
    encoding: "utf8",
  });
  assert.equal(result.status, 0, result.stderr || result.stdout);
}
