import { FigmaCatalogTreeSyncGateway } from "../figma/figma-catalog-tree-sync-gateway";
import type { DesignModel, SyncTargetName } from "../domain/design-model";

export async function main(
  designModel: DesignModel | undefined,
  options?: {
    target?: SyncTargetName;
    sectionNodeId?: string;
  }
) {
  if (!designModel) {
    throw new Error("Replace DESIGN_MODEL with a preview design model fixture.");
  }
  if (designModel.branch !== "main") {
    throw new Error(`Preview catalog sync requires a main-compatible model. Found '${designModel.branch ?? "<missing>"}'.`);
  }
  if (!options?.target || options.target === "headers" || options.target === "versions" || options.target === "metadata") {
    throw new Error("Preview catalog sync requires one catalog target.");
  }
  if (!options.sectionNodeId) {
    throw new Error("Preview catalog sync requires a sandbox section node id.");
  }

  const gateway = new FigmaCatalogTreeSyncGateway();
  return gateway.syncCatalogTrees(
    designModel,
    {
      targetNames: [options.target],
      sectionNodeOverrides: {
        [options.target]: options.sectionNodeId,
      },
    }
  );
}
