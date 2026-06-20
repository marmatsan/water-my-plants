import { METADATA_NAMESPACE, METADATA_PAGE_ID } from "../config/figma-config";
import type { DesignModel } from "../domain/design-model";
import type { MetadataSyncGateway } from "../ports/sync-gateways";
import { requirePage } from "./figma-node-gateway";

export class FigmaMetadataGateway implements MetadataSyncGateway {
  async writeMetadata(designModel: DesignModel) {
    const page = await requirePage(METADATA_PAGE_ID);
    await figma.setCurrentPageAsync(page);

    page.setSharedPluginData(METADATA_NAMESPACE, "schemaVersion", String(designModel.schemaVersion));
    page.setSharedPluginData(METADATA_NAMESPACE, "branch", designModel.branch);
    page.setSharedPluginData(METADATA_NAMESPACE, "gitSha", designModel.gitSha);
    page.setSharedPluginData(METADATA_NAMESPACE, "modelHash", designModel.modelHash);
    page.setSharedPluginData(METADATA_NAMESPACE, "syncedAt", new Date().toISOString());

    return {
      metadata: {
        pageId: page.id,
        namespace: METADATA_NAMESPACE,
        gitSha: page.getSharedPluginData(METADATA_NAMESPACE, "gitSha"),
        modelHash: page.getSharedPluginData(METADATA_NAMESPACE, "modelHash"),
      },
      mutatedNodeIds: [page.id],
    };
  }
}
