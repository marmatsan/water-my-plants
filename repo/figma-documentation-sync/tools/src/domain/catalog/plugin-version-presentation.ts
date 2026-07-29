import type { CatalogTreeTarget, DesignModel, FlattenedCatalogNode } from "../design-model";

/** Adds an explanatory display value without changing the source version reference. */
export function presentPluginVersions(
  nodes: FlattenedCatalogNode[],
  target: CatalogTreeTarget,
  designModel: DesignModel
): FlattenedCatalogNode[] {
  if (target.type !== "Plugin" || !target.versionValuesPath?.length) return nodes;

  const resolvedVersions = valueAtPath(designModel, target.versionValuesPath) || {};
  const sharedVersionKeys = new Set(target.sharedVersionKeys || []);
  return nodes.map((node) => ({
    ...node,
    version: presentedVersion(node.version, resolvedVersions, sharedVersionKeys),
  }));
}

/** Formats one version reference, its resolved value, and optional release policy. */
export function presentedVersion(
  version,
  resolvedVersions: Record<string, unknown>,
  sharedVersionKeys: Set<string>
) {
  if (version?.visible !== true || !version.value) return version;

  const reference = String(version.value);
  const resolvedValue = resolvedVersions[reference];
  if (resolvedValue == null || String(resolvedValue).length === 0) return version;

  const policy = sharedVersionKeys.has(reference) ? "\npolicy shared" : "";
  return {
    ...version,
    displayValue: `ref ${reference}\nresolved ${String(resolvedValue)}${policy}`,
  };
}

function valueAtPath(
  root: DesignModel,
  path: string[]
) {
  return path.reduce((value, key) => value?.[key], root);
}
