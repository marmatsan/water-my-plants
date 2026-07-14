import { FigmaCatalogTreeSyncGateway } from "../figma/figma-catalog-tree-sync-gateway";
import { FigmaHeaderSyncGateway } from "../figma/figma-header-sync-gateway";
import { FigmaCiDocumentationSyncGateway } from "../figma/figma-ci-documentation-sync-gateway";
import { FigmaMetadataGateway } from "../figma/figma-metadata-gateway";
import { FigmaVersionSyncGateway } from "../figma/figma-version-sync-gateway";
import { FigmaVisualContractCheckGateway } from "../figma/figma-visual-contract-check-gateway";
import { syncFigmaDesignModel } from "../usecases/sync-figma-design-model";

export function createFigmaDesignModelSync() {
  return (designModel, options?) => syncFigmaDesignModel(
    designModel,
    {
      versionSyncGateway: new FigmaVersionSyncGateway(),
      headerSyncGateway: new FigmaHeaderSyncGateway(),
      catalogTreeSyncGateway: new FigmaCatalogTreeSyncGateway(),
      ciDocumentationSyncGateway: new FigmaCiDocumentationSyncGateway(),
      metadataSyncGateway: new FigmaMetadataGateway(),
      visualContractCheckGateway: new FigmaVisualContractCheckGateway(),
    },
    options
  );
}
