import type { CatalogTreeTarget, CatalogTreeType, FlattenedCatalogNode } from "../design-model";

export function flattenCatalogNodes(
  nodes,
  type: CatalogTreeType,
  parentPath: string[] = []
): FlattenedCatalogNode[] {
  return nodes.flatMap((node) => {
    const label = type === "Library" ? node.group : node.id;
    const path = [...parentPath, label];
    const current = type === "Library"
      ? {
          type,
          label,
          parentPath,
          path,
          artifactsVisible: node.artifactsVisible,
          entries: node.entries || [],
          children: node.children || [],
        }
      : {
          type,
          label,
          parentPath,
          path,
          version: node.version,
          appliedToModules: node.appliedToModules || [],
          providedByConventionPlugins: node.providedByConventionPlugins || [],
          children: node.children || [],
        };

    return [
      current,
      ...flattenCatalogNodes(current.children, type, path),
    ];
  });
}

export function requireUniquePaths(
  target: Pick<CatalogTreeTarget, "name">,
  nodes: Pick<FlattenedCatalogNode, "path">[]
) {
  const paths = new Set();
  const duplicatePaths = new Set();

  for (const node of nodes) {
    const path = JSON.stringify(node.path);
    if (paths.has(path)) duplicatePaths.add(node.path.join("/"));
    paths.add(path);
  }

  if (duplicatePaths.size > 0) {
    throw new Error(
      `${target.name} contains duplicate catalog paths (${[...duplicatePaths].join(", ")}).`
    );
  }
}
