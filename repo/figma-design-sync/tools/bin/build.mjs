#!/usr/bin/env node

import { cp, mkdir } from "node:fs/promises";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { build } from "esbuild";

const packageRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const options = parseOptions(process.argv.slice(2));
const projectConfig = resolve(process.cwd(), options.projectConfig);
const outputRoot = resolve(process.cwd(), options.outputDirectory);
const outputDist = join(outputRoot, "dist");

await mkdir(outputDist, { recursive: true });
await copyRuntimeSources(outputRoot);

const alias = {
  "@figma-design-sync/project-config": projectConfig,
};

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
  ["scripts/write-mcp-runner.ts", "write-mcp-runner.mjs"],
  ["scripts/execute-mcp-runner.ts", "execute-mcp-runner.mjs"],
  ["scripts/write-visual-sync-plan.ts", "write-visual-sync-plan.mjs"],
]) {
  await build({
    absWorkingDir: packageRoot,
    alias,
    bundle: true,
    entryPoints: [entrypoint],
    format: "esm",
    outfile: join(outputDist, output),
    platform: "node",
    target: "node18",
  });
}

await import(pathToFileURL(join(outputDist, "write-mcp-js.mjs")).href);

console.log(`Prepared Figma Design Sync tools in ${outputRoot}`);

async function bundleFigmaEntrypoint(entrypoint, output, globalName) {
  await build({
    absWorkingDir: packageRoot,
    alias,
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
  const values = {};
  for (const argument of args) {
    const [key, value] = argument.replace(/^--/, "").split("=", 2);
    if (!argument.startsWith("--") || !key || !value) {
      throw new Error(`Expected --name=value, received '${argument}'.`);
    }
    values[key] = value;
  }

  if (!values["project-config"]) {
    throw new Error("Missing --project-config=PATH.");
  }

  return {
    projectConfig: values["project-config"],
    outputDirectory: values["output-dir"] || ".",
  };
}
