import { FigmaCatalogTreeSyncGateway } from "../figma/figma-catalog-tree-sync-gateway";
import { FigmaMetadataGateway } from "../figma/figma-metadata-gateway";
import { FigmaModuleDependencySyncGateway } from "../figma/figma-module-dependency-sync-gateway";
import { FigmaVersionSyncGateway } from "../figma/figma-version-sync-gateway";
import { syncFigmaDesignModel } from "../usecases/sync-figma-design-model";

export function createFigmaDesignModelSync() {
  return (designModel) => syncFigmaDesignModel(
    designModel,
    {
      versionSyncGateway: new FigmaVersionSyncGateway(),
      catalogTreeSyncGateway: new FigmaCatalogTreeSyncGateway(),
      moduleDependencySyncGateway: new FigmaModuleDependencySyncGateway(),
      metadataSyncGateway: new FigmaMetadataGateway(),
    }
  );
}
