#!/usr/bin/env node

import { cp, mkdir, readFile } from "node:fs/promises";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath, pathToFileURL } from "node:url";
import { build } from "esbuild";

const packageRoot = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const options = parseOptions(process.argv.slice(2));
const outputRoot = resolve(process.cwd(), options.outputDirectory);
const outputDist = join(outputRoot, "dist");
const projectConfigBuildOptions = await createProjectConfigBuildOptions(options);

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
  ["scripts/write-mcp-runner.ts", "write-mcp-runner.mjs"],
  ["scripts/execute-mcp-runner.ts", "execute-mcp-runner.mjs"],
  ["scripts/write-visual-sync-plan.ts", "write-visual-sync-plan.mjs"],
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

console.log(`Prepared Figma Design Sync tools in ${outputRoot}`);

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
  const values = {};
  for (const argument of args) {
    const [key, value] = argument.replace(/^--/, "").split("=", 2);
    if (!argument.startsWith("--") || !key || !value) {
      throw new Error(`Expected --name=value, received '${argument}'.`);
    }
    values[key] = value;
  }

  const projectConfigInputs = [values["project-config"], values["project-config-json"]].filter(Boolean);
  if (projectConfigInputs.length !== 1) {
    throw new Error("Provide exactly one of --project-config=PATH or --project-config-json=PATH.");
  }

  return {
    projectConfig: values["project-config"] || null,
    projectConfigJson: values["project-config-json"] || null,
    outputDirectory: values["output-dir"] || ".",
  };
}

async function createProjectConfigBuildOptions(options) {
  if (options.projectConfig) {
    return {
      alias: {
        "@figma-design-sync/project-config": resolve(process.cwd(), options.projectConfig),
      },
    };
  }

  const projectConfigPath = resolve(process.cwd(), options.projectConfigJson);
  const projectConfig = JSON.parse(await readFile(projectConfigPath, "utf8"));
  if (projectConfig.schemaVersion !== 1) {
    throw new Error(
      `Unsupported project config schema '${projectConfig.schemaVersion}' in ${projectConfigPath}.`
    );
  }
  if (!Array.isArray(projectConfig.CATALOG_TREE_TARGETS)) {
    throw new Error(`Missing CATALOG_TREE_TARGETS in ${projectConfigPath}.`);
  }
  for (const target of projectConfig.CATALOG_TREE_TARGETS) {
    if (!Array.isArray(target.nodesPath) || target.nodesPath.length === 0) {
      throw new Error(`Catalog target '${target.name}' has no nodesPath in ${projectConfigPath}.`);
    }
  }

  return {
    plugins: [createJsonProjectConfigPlugin(projectConfig, projectConfigPath)],
  };
}

function createJsonProjectConfigPlugin(projectConfig, projectConfigPath) {
  const exportNames = Object.keys(projectConfig)
    .filter((name) => /^[A-Z][A-Z0-9_]+$/.test(name) && name !== "CATALOG_TREE_TARGETS");
  const moduleContents = [
    `const config = ${JSON.stringify(projectConfig)};`,
    "const valueAtPath = (root, path) => path.reduce((value, key) => value?.[key], root);",
    ...exportNames.map((name) => `export const ${name} = config.${name};`),
    "export const CATALOG_TREE_TARGETS = config.CATALOG_TREE_TARGETS.map(" +
      "({ nodesPath, ...target }) => ({ " +
      "...target, nodes: (designModel) => valueAtPath(designModel, nodesPath) " +
      "})" +
      ");",
  ].join("\n");

  return {
    name: "figma-design-sync-json-project-config",
    setup(buildContext) {
      buildContext.onResolve(
        { filter: /^@figma-design-sync\/project-config$/ },
        () => ({ path: projectConfigPath, namespace: "figma-project-config" })
      );
      buildContext.onLoad(
        { filter: /.*/, namespace: "figma-project-config" },
        () => ({ contents: moduleContents, loader: "js", resolveDir: dirname(projectConfigPath) })
      );
    },
  };
}
