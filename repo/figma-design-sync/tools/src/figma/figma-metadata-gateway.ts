import { METADATA_NAMESPACE, METADATA_PAGE_ID } from "@figma-design-sync/project-config";
import type { DesignModel, SyncExecutionMetadata } from "../domain/design-model";
import type { MetadataSyncGateway } from "../ports/sync-gateways";
import { requirePage } from "./figma-node-gateway";

export class FigmaMetadataGateway implements MetadataSyncGateway {
  async writeMetadata(designModel: DesignModel, executionMetadata: SyncExecutionMetadata) {
    const page = await requirePage(METADATA_PAGE_ID);
    await figma.setCurrentPageAsync(page);

    page.setSharedPluginData(METADATA_NAMESPACE, "schemaVersion", String(designModel.schemaVersion));
    page.setSharedPluginData(METADATA_NAMESPACE, "branch", designModel.branch);
    page.setSharedPluginData(METADATA_NAMESPACE, "gitSha", designModel.gitSha);
    page.setSharedPluginData(METADATA_NAMESPACE, "modelHash", designModel.modelHash);
    page.setSharedPluginData(METADATA_NAMESPACE, "writerHash", executionMetadata.writerHash);
    page.setSharedPluginData(METADATA_NAMESPACE, "transportHash", executionMetadata.transportHash);
    page.setSharedPluginData(
      METADATA_NAMESPACE,
      "targetFingerprints",
      JSON.stringify(executionMetadata.targetFingerprints)
    );
    page.setSharedPluginData(
      METADATA_NAMESPACE,
      "writerScopeFingerprints",
      JSON.stringify(executionMetadata.writerScopeFingerprints)
    );
    page.setSharedPluginData(
      METADATA_NAMESPACE,
      "writerScopeFingerprintSchemaVersion",
      String(executionMetadata.writerScopeFingerprintSchemaVersion)
    );
    page.setSharedPluginData(METADATA_NAMESPACE, "syncedAt", new Date().toISOString());

    return {
      metadata: {
        pageId: page.id,
        namespace: METADATA_NAMESPACE,
        gitSha: page.getSharedPluginData(METADATA_NAMESPACE, "gitSha"),
        modelHash: page.getSharedPluginData(METADATA_NAMESPACE, "modelHash"),
        writerHash: page.getSharedPluginData(METADATA_NAMESPACE, "writerHash"),
        transportHash: page.getSharedPluginData(METADATA_NAMESPACE, "transportHash"),
        writerScopeFingerprintSchemaVersion: page.getSharedPluginData(
          METADATA_NAMESPACE,
          "writerScopeFingerprintSchemaVersion"
        ),
      },
      mutatedNodeIds: [page.id],
    };
  }
}
