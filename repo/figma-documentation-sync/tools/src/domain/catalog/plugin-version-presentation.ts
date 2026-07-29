import type { CatalogTreeTarget, DesignModel, FlattenedCatalogNode } from "../design-model";

/** Adds a compact display value without changing the source version reference. */
export function presentPluginVersions(
  nodes: FlattenedCatalogNode[],
  target: CatalogTreeTarget,
  designModel: DesignModel
): FlattenedCatalogNode[] {
  if (target.type !== "Plugin" || !target.versionValuesPath?.length) return nodes;

  const resolvedVersions = valueAtPath(designModel, target.versionValuesPath) || {};
  return nodes.map((node) => ({
    ...node,
    version: presentedVersion(node.version, resolvedVersions),
  }));
}

/** Formats one resolved version reference as its source property name. */
export function presentedVersion(
  version,
  resolvedVersions: Record<string, unknown>
) {
  if (version?.visible !== true || !version.value) return version;

  const reference = String(version.value);
  const resolvedValue = resolvedVersions[reference];
  if (resolvedValue == null || String(resolvedValue).length === 0) return version;

  return {
    ...version,
    displayValue: reference,
  };
}

function valueAtPath(
  root: DesignModel,
  path: string[]
) {
  return path.reduce((value, key) => value?.[key], root);
}
