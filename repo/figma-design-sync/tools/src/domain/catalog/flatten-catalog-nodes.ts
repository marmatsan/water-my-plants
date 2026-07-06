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
          children: node.children || [],
        };

    return [
      current,
      ...flattenCatalogNodes(current.children, type, path),
    ];
  });
}

export function requireUniqueLabels(target: CatalogTreeTarget, nodes: FlattenedCatalogNode[]) {
  const labels = new Map();
  for (const node of nodes) {
    labels.set(node.label, (labels.get(node.label) || 0) + 1);
  }

  const duplicateLabels = [...labels.entries()]
    .filter(([, count]) => count > 1)
    .map(([label]) => label);

  if (duplicateLabels.length > 0) {
    throw new Error(
      `${target.name} contains duplicate labels (${duplicateLabels.join(", ")}). ` +
        "Catalog tree update-only sync needs stable Figma node path metadata before it can continue."
    );
  }
}
