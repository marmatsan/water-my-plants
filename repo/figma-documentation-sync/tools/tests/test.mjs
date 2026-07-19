#!/usr/bin/env node

import { mkdir } from "node:fs/promises";
import { dirname, join, resolve } from "node:path";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import { build } from "esbuild";
import { createProjectConfigBuildOptions } from "../bin/project-config-build-options.mjs";

const packageRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const projectConfigJson = process.env.FIGMA_DOCUMENTATION_SYNC_PROJECT_CONFIG;
if (!projectConfigJson) {
  throw new Error(
    "FIGMA_DOCUMENTATION_SYNC_PROJECT_CONFIG must point to the Kotlin-generated writer project config."
  );
}

const projectConfigBuildOptions = await createProjectConfigBuildOptions(projectConfigJson);
const outputDirectory = join(packageRoot, "dist", "tests");
await mkdir(outputDirectory, { recursive: true });

await bundle("scripts/write-mcp-preview.ts", join(packageRoot, "dist", "write-mcp-preview.mjs"));

runNodeTest(join(packageRoot, "tests", "distribution-build.test.mjs"));

for (const testName of [
  "sync-preflight-target",
  "version-sync-plan",
  "header-sync-plan",
  "metadata-sync",
  "write-mcp-preview",
  "catalog-root-filter-scope",
  "library-catalog-entries",
  "catalog-tree-targets",
  "ci-visual-plan",
]) {
  const output = join(outputDirectory, `${testName}.test.mjs`);
  await bundle(`tests/${testName}.test.ts`, output);
  runNodeTest(output);
}

async function bundle(entryPoint, outputFile) {
  await build({
    absWorkingDir: packageRoot,
    ...projectConfigBuildOptions,
    bundle: true,
    entryPoints: [entryPoint],
    format: "esm",
    outfile: outputFile,
    platform: "node",
    target: "node18",
  });
}

function runNodeTest(testFile) {
  const result = spawnSync(process.execPath, ["--test", testFile], {
    cwd: packageRoot,
    encoding: "utf8",
    env: process.env,
  });
  process.stdout.write(result.stdout || "");
  process.stderr.write(result.stderr || "");
  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}
