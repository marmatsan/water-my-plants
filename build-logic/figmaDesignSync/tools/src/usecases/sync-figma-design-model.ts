import type { DesignModel } from "../domain/design-model";
import type {
  CatalogTreeSyncGateway,
  MetadataSyncGateway,
  VersionSyncGateway,
} from "../ports/sync-gateways";

export type SyncFigmaDesignModelDependencies = {
  versionSyncGateway: VersionSyncGateway;
  catalogTreeSyncGateway: CatalogTreeSyncGateway;
  metadataSyncGateway: MetadataSyncGateway;
};

export async function syncFigmaDesignModel(
  designModel: DesignModel | undefined,
  dependencies: SyncFigmaDesignModelDependencies
) {
  if (!designModel) {
    throw new Error("Replace DESIGN_MODEL with build/reports/figma-sync/design-model.json.");
  }

  const versionSyncResult = await dependencies.versionSyncGateway.syncVersions(designModel);
  const catalogSyncResult = await dependencies.catalogTreeSyncGateway.syncCatalogTrees(designModel);
  const metadataSyncResult = await dependencies.metadataSyncGateway.writeMetadata(designModel);

  return {
    updatedVersions: versionSyncResult.updatedVersions,
    createdVariables: versionSyncResult.createdVariables,
    createdInstances: versionSyncResult.createdInstances,
    updatedCatalogNodes: catalogSyncResult.updatedCatalogNodes,
    createdCatalogNodes: catalogSyncResult.createdCatalogNodes,
    createdCatalogConnectors: catalogSyncResult.createdCatalogConnectors,
    metadata: metadataSyncResult.metadata,
    mutatedNodeIds: [
      ...new Set([
        ...versionSyncResult.mutatedNodeIds,
        ...catalogSyncResult.mutatedNodeIds,
        ...metadataSyncResult.mutatedNodeIds,
      ]),
    ],
  };
}
