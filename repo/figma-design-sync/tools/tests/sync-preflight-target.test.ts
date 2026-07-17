import assert from "node:assert/strict";
import test from "node:test";
import { syncFigmaDesignModel } from "../src/usecases/sync-figma-design-model";

const FULL_VISUAL_TARGETS = [
  "preflight",
  "headers",
  "versions",
  "waterMyPlants.libraries",
  "waterMyPlants.plugins",
  "waterMyPlants.customGradleConventionPlugins",
  "waterMyPlants.customGradlePlugins",
  "gradlePlugins.libraries",
  "gradlePlugins.plugins",
  "figmaDesignSync.libraries",
  "figmaDesignSync.plugins",
  "ci.overview",
  "ci.pullRequestIntegration",
  "ci.postMergeDesignDocumentation",
  "ci.infrastructureAndAccess",
  "ci.windowsRuntime",
];

test("default sync executes the complete visual contract without metadata", async () => {
  const calls: string[] = [];

  const result = await syncFigmaDesignModel(mainDesignModel(), fakeDependencies(calls));

  assert.deepEqual(calls, ["preflight", "headers", "versions", "catalog", "ci"]);
  assert.deepEqual(result.requestedTargets, FULL_VISUAL_TARGETS);
  assert.deepEqual(result.completedTargets, FULL_VISUAL_TARGETS);
  assert.equal(result.metadata, null);
});

test("metadata must be requested independently from visual targets", async () => {
  const calls: string[] = [];

  await assert.rejects(
    syncFigmaDesignModel(mainDesignModel(), fakeDependencies(calls), {
      targets: ["preflight", "metadata"],
    }),
    /metadata must run alone/
  );
  assert.deepEqual(calls, []);
});

test("metadata writes after being requested as the only target", async () => {
  const calls: string[] = [];

  const result = await syncFigmaDesignModel(mainDesignModel(), fakeDependencies(calls), {
    targets: ["metadata"],
  });

  assert.deepEqual(calls, ["metadata"]);
  assert.deepEqual(result.completedTargets, ["metadata"]);
  assert.equal(result.metadata.modelHash, "hash");
});

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

test("catalog cleanup-only execution is forwarded independently from root filters", async () => {
  const calls: string[] = [];
  const dependencies = fakeDependencies(calls);
  let receivedOptions;
  dependencies.catalogTreeSyncGateway.syncCatalogTrees = async (_designModel, options) => {
    receivedOptions = options;
    return {
      updatedCatalogNodes: [],
      createdCatalogNodes: [],
      createdCatalogConnectors: [],
      removedCatalogNodes: [],
      removedCatalogConnectors: [],
      mutatedNodeIds: [],
    };
  };

  await syncFigmaDesignModel(mainDesignModel(), dependencies, {
    targets: ["waterMyPlants.libraries"],
    writeMetadata: false,
    catalogCleanupOnlyTargets: ["waterMyPlants.libraries"],
  });

  assert.deepEqual(receivedOptions.cleanupOnlyTargetNames, ["waterMyPlants.libraries"]);
  assert.equal(receivedOptions.rootFilters, undefined);
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

test("preflight validates and synchronizes a granular CI documentation target", async () => {
  const calls: string[] = [];
  const result = await syncFigmaDesignModel(
    mainDesignModel(),
    fakeDependencies(calls),
    {
      targets: ["preflight", "ci.overview"],
      writeMetadata: false,
    }
  );

  assert.deepEqual(calls, ["preflight", "ci"]);
  assert.deepEqual(result.completedTargets, ["preflight", "ci.overview"]);
  assert.deepEqual(result.checkedTargets, ["ci.overview"]);
  assert.deepEqual(result.updatedCiSections, ["ci.overview"]);
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
    ciDocumentationSyncGateway: {
      async syncCiDocumentation(_designModel, targetNames) {
        calls.push("ci");
        return {
          updatedCiSections: targetNames,
          createdCiNodes: ["ci-node"],
          createdCiConnectors: ["ci-connector"],
          mutatedNodeIds: ["ci-section"],
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
