import { readFile } from "node:fs/promises";
import { dirname, resolve } from "node:path";

export async function createProjectConfigBuildOptions(projectConfigJson) {
  const projectConfigPath = resolve(process.cwd(), projectConfigJson);
  const projectConfig = JSON.parse(await readFile(projectConfigPath, "utf8"));
  if (projectConfig.schemaVersion !== 3) {
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
    name: "figma-documentation-sync-json-project-config",
    setup(buildContext) {
      buildContext.onResolve(
        { filter: /^@figma-documentation-sync\/project-config$/ },
        () => ({ path: projectConfigPath, namespace: "figma-project-config" })
      );
      buildContext.onLoad(
        { filter: /.*/, namespace: "figma-project-config" },
        () => ({ contents: moduleContents, loader: "js", resolveDir: dirname(projectConfigPath) })
      );
    },
  };
}
