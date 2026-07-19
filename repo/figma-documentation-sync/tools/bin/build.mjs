#!/usr/bin/env node

import { cp, mkdir } from "node:fs/promises";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { build } from "esbuild";
import { createProjectConfigBuildOptions } from "./project-config-build-options.mjs";

const packageRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const options = parseOptions(process.argv.slice(2));
const outputRoot = resolve(process.cwd(), options.outputDirectory);
const outputDist = join(outputRoot, "dist");
const projectConfigBuildOptions = await createProjectConfigBuildOptions(options.projectConfigJson);

await mkdir(outputDist, { recursive: true });
await copyRuntimeSources(outputRoot);

await bundleFigmaEntrypoint(
  "src/app/sync-trunk-design-model.mcp.ts",
  "sync-trunk-design-model.mcp.js",
  "FigmaTrunkSync"
);
await bundleFigmaEntrypoint(
  "src/app/sync-catalog-tree-preview.mcp.ts",
  "sync-catalog-tree-preview.mcp.js",
  "FigmaCatalogTreePreview"
);

for (const [entrypoint, output] of [
  ["scripts/write-mcp-js.ts", "write-mcp-js.mjs"],
  ["scripts/write-mcp-preview.ts", "write-mcp-preview.mjs"],
]) {
  await build({
    absWorkingDir: packageRoot,
    ...projectConfigBuildOptions,
    bundle: true,
    entryPoints: [entrypoint],
    format: "esm",
    outfile: join(outputDist, output),
    platform: "node",
    target: "node18",
  });
}

await import(pathToFileURL(join(outputDist, "write-mcp-js.mjs")).href);

console.log(`Prepared Figma Documentation Sync tools in ${outputRoot}`);

async function bundleFigmaEntrypoint(entrypoint, output, globalName) {
  await build({
    absWorkingDir: packageRoot,
    ...projectConfigBuildOptions,
    bundle: true,
    entryPoints: [entrypoint],
    format: "iife",
    globalName,
    minify: true,
    outfile: join(outputDist, output),
    target: "es2022",
  });
}

async function copyRuntimeSources(targetRoot) {
  if (resolve(targetRoot) === packageRoot) {
    return;
  }

  await cp(join(packageRoot, "src"), join(targetRoot, "src"), {
    recursive: true,
    force: true,
  });
  await cp(join(packageRoot, "fixtures"), join(targetRoot, "fixtures"), {
    recursive: true,
    force: true,
  });
}

function parseOptions(args) {
  const supportedOptions = new Set(["project-config-json", "output-dir"]);
  const values = {};
  for (const argument of args) {
    const [key, value] = argument.replace(/^--/, "").split("=", 2);
    if (!argument.startsWith("--") || !key || !value) {
      throw new Error(`Expected --name=value, received '${argument}'.`);
    }
    if (!supportedOptions.has(key)) {
      throw new Error(`Unsupported option '--${key}'.`);
    }
    values[key] = value;
  }

  const projectConfigJson = values["project-config-json"]
    || process.env.FIGMA_DOCUMENTATION_SYNC_PROJECT_CONFIG;
  if (!projectConfigJson) {
    throw new Error(
      "Provide --project-config-json=PATH or FIGMA_DOCUMENTATION_SYNC_PROJECT_CONFIG."
    );
  }

  return {
    projectConfigJson,
    outputDirectory: values["output-dir"] || ".",
  };
}
