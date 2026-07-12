import assert from "node:assert/strict";
import test from "node:test";
import { syncFigmaDesignModel } from "../src/usecases/sync-figma-design-model";

test("preflight target validates the visual contract without mutating visual targets", async () => {
  const calls: string[] = [];
  const dependencies = fakeDependencies(calls);

  const result = await syncFigmaDesignModel(
    mainDesignModel(),
    dependencies,
    {
      targets: ["preflight"],
      writeMetadata: false,
    }
  );

  assert.deepEqual(calls, ["preflight"]);
  assert.deepEqual(result.completedTargets, ["preflight"]);
  assert.deepEqual(result.checkedComponents, ["component:.tree node"]);
  assert.deepEqual(result.mutatedNodeIds, []);
});

test("preflight runs before a requested visual target and scopes the contract check", async () => {
  const calls: string[] = [];
  const dependencies = fakeDependencies(calls);

  const result = await syncFigmaDesignModel(
    mainDesignModel(),
    dependencies,
    {
      targets: ["preflight", "waterMyPlants.libraries"],
      writeMetadata: false,
      catalogRootFilters: {
        "waterMyPlants.libraries": ["androidx"],
      },
    }
  );

  assert.deepEqual(calls, ["preflight", "catalog"]);
  assert.deepEqual(result.completedTargets, ["preflight", "waterMyPlants.libraries"]);
  assert.deepEqual(result.checkedTargets, ["waterMyPlants.libraries"]);
  assert.deepEqual(result.updatedCatalogNodes, ["waterMyPlants.libraries/androidx"]);
});

test("headers can be synchronized as an independent visual target", async () => {
  const calls: string[] = [];
  const result = await syncFigmaDesignModel(
    mainDesignModel(),
    fakeDependencies(calls),
    {
      targets: ["headers"],
      writeMetadata: false,
    }
  );

  assert.deepEqual(calls, ["headers"]);
  assert.deepEqual(result.completedTargets, ["headers"]);
  assert.deepEqual(result.updatedHeaders, ["parent-section"]);
});

function mainDesignModel() {
  return {
    branch: "main",
    content: {},
  };
}

function fakeDependencies(calls: string[]) {
  return {
    headerSyncGateway: {
      async syncHeaders() {
        calls.push("headers");
        return {
          updatedHeaders: ["parent-section"],
          mutatedNodeIds: ["header"],
        };
      },
    },
    versionSyncGateway: {
      async syncVersions() {
        calls.push("versions");
        return {
          updatedVersions: [],
          createdVariables: [],
          createdInstances: [],
          mutatedNodeIds: ["versions"],
        };
      },
    },
    catalogTreeSyncGateway: {
      async syncCatalogTrees(_designModel, options) {
        calls.push("catalog");
        return {
          updatedCatalogNodes: (options?.targetNames || []).map((target) =>
            `${target}/${options?.rootFilters?.[target]?.[0] || "all"}`
          ),
          createdCatalogNodes: [],
          createdCatalogConnectors: [],
          removedCatalogNodes: [],
          removedCatalogConnectors: [],
          mutatedNodeIds: ["catalog"],
        };
      },
    },
    metadataSyncGateway: {
      async writeMetadata() {
        calls.push("metadata");
        return {
          metadata: {
            pageId: "page",
            namespace: "namespace",
            gitSha: "sha",
            modelHash: "hash",
          },
          mutatedNodeIds: ["metadata"],
        };
      },
    },
    visualContractCheckGateway: {
      async checkVisualContract(_designModel, options) {
        calls.push("preflight");
        return {
          checkedComponents: ["component:.tree node"],
          checkedSections: [],
          checkedVariables: [],
          checkedTargets: options?.targetNames || [],
          mutatedNodeIds: [],
        };
      },
    },
  };
}
