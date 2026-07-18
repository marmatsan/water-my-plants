import assert from "node:assert/strict";
import test from "node:test";
import { METADATA_NAMESPACE, METADATA_PAGE_ID } from "../src/config/figma-config";
import { FigmaMetadataGateway } from "../src/figma/figma-metadata-gateway";

test("metadata sync persists model and scoped writer fingerprints", async () => {
  const values = new Map<string, string>();
  const page = {
    id: METADATA_PAGE_ID,
    type: "PAGE",
    setSharedPluginData(namespace, key, value) {
      assert.equal(namespace, METADATA_NAMESPACE);
      values.set(key, value);
    },
    getSharedPluginData(namespace, key) {
      assert.equal(namespace, METADATA_NAMESPACE);
      return values.get(key) || "";
    },
  };
  globalThis.figma = {
    async getNodeByIdAsync(nodeId) {
      assert.equal(nodeId, METADATA_PAGE_ID);
      return page;
    },
    async setCurrentPageAsync(selectedPage) {
      assert.equal(selectedPage, page);
    },
  } as never;

  const result = await new FigmaMetadataGateway().writeMetadata(
    {
      schemaVersion: 1,
      branch: "main",
      gitSha: "abc123",
      modelHash: "sha256:model",
    } as never,
    {
      writerHash: "sha256:writer",
      transportHash: "sha256:transport",
      targetFingerprints: { versions: "sha256:model-versions" },
      writerScopeFingerprints: {
        versions: "sha256:writer-versions",
        metadata: "sha256:writer-metadata",
      },
      writerScopeFingerprintSchemaVersion: 1,
    }
  );

  assert.deepEqual(
    JSON.parse(values.get("writerScopeFingerprints") || ""),
    {
      versions: "sha256:writer-versions",
      metadata: "sha256:writer-metadata",
    }
  );
  assert.equal(values.get("writerScopeFingerprintSchemaVersion"), "1");
  assert.equal(result.metadata.writerScopeFingerprintSchemaVersion, "1");
  assert.deepEqual(result.mutatedNodeIds, [METADATA_PAGE_ID]);
});
