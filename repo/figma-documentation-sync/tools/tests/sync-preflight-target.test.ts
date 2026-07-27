import assert from "node:assert/strict";
import test from "node:test";
import type { CiVisualPlan } from "../src/domain/ci/ci-visual-plan";
import { syncFigmaDesignModel } from "../src/usecases/sync-figma-design-model";

const FULL_VISUAL_TARGETS = [
  "preflight",
  "headers",
  "versions",
  "waterMyPlants.libraries",
  "waterMyPlants.plugins",
  "ci.overview",
  "ci.pullRequestIntegration",
  "ci.postMergeDesignDocumentation",
  "ci.jobTasks",
  "ci.infrastructureAndAccess",
  "ci.windowsRuntime",
];

test("default sync executes the complete visual contract without metadata", async () => {
  const calls: string[] = [];

  const result = await syncFigmaDesignModel(mainDesignModel(), fakeDependencies(calls), {
    ciVisualPlan: ciVisualPlan(...FULL_VISUAL_TARGETS.filter((target) => target.startsWith("ci."))),
  });

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
    executionMetadata: {
      writerHash: "sha256:writer",
      transportHash: "sha256:transport",
      targetFingerprints: { versions: "sha256:versions" },
      writerScopeFingerprints: {
        versions: "sha256:versions-writer",
        metadata: "sha256:metadata-writer",
      },
      writerScopeFingerprintSchemaVersion: 1,
    },
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
      ciVisualPlan: ciVisualPlan("ci.overview"),
    }
  );

  assert.deepEqual(calls, ["preflight", "ci"]);
  assert.deepEqual(result.completedTargets, ["preflight", "ci.overview"]);
  assert.deepEqual(result.checkedTargets, ["ci.overview"]);
  assert.deepEqual(result.updatedCiSections, ["ci.overview"]);
});

test("forwards the Kotlin-precomputed CI visual plan to the Figma boundary", async () => {
  const calls: string[] = [];
  const dependencies = fakeDependencies(calls);
  let receivedPlan;
  dependencies.ciDocumentationSyncGateway.syncCiDocumentation = async (targets, visualPlan) => {
    receivedPlan = visualPlan;
    return {
      updatedCiSections: targets,
      createdCiNodes: [],
      updatedCiSteps: [],
      createdCiConnectors: [],
      mutatedNodeIds: [],
    };
  };
  const visualPlan = ciVisualPlan("ci.overview");

  await syncFigmaDesignModel(mainDesignModel(), dependencies, {
    targets: ["ci.overview"],
    ciVisualPlan: visualPlan,
  });

  assert.equal(receivedPlan, visualPlan);
});

test("rejects CI sync without a Kotlin-generated visual plan", async () => {
  await assert.rejects(
    syncFigmaDesignModel(mainDesignModel(), fakeDependencies([]), {
      targets: ["ci.overview"],
    }),
    /requires a Kotlin-generated ciVisualPlan/
  );
});

test("rejects an obsolete CI visual plan before reaching Figma", async () => {
  const calls: string[] = [];
  const obsoletePlan = {
    schemaVersion: 1,
    parentName: "Continuous Integration and Documentation Automation",
    sections: [],
  } as unknown as CiVisualPlan;

  await assert.rejects(
    syncFigmaDesignModel(mainDesignModel(), fakeDependencies(calls), {
      targets: ["preflight", "ci.overview"],
      ciVisualPlan: obsoletePlan,
    }),
    /schemaVersion 4/
  );
  assert.deepEqual(calls, []);
});

test("rejects CI nodes without typed phases and outcomes before reaching Figma", async () => {
  const calls: string[] = [];
  const nodeWithoutTypedSteps = {
    schemaVersion: 4,
    parentName: "Continuous Integration and Documentation Automation",
    sections: [
      {
        target: "ci.overview",
        name: "Overview",
        description: "Overview",
        orientation: "horizontal",
        headerSources: [],
        nodes: [{ id: "verify" }],
        connections: [],
      },
    ],
  } as unknown as CiVisualPlan;

  await assert.rejects(
    syncFigmaDesignModel(mainDesignModel(), fakeDependencies(calls), {
      targets: ["preflight", "ci.overview"],
      ciVisualPlan: nodeWithoutTypedSteps,
    }),
    /typed phases and outcomes arrays/
  );
  assert.deepEqual(calls, []);
});

test("rejects CI nodes that exceed the 8 reserved phase slots before reaching Figma", async () => {
  const calls: string[] = [];
  const oversizedPlan = ciVisualPlan("ci.overview");
  oversizedPlan.sections[0].nodes.push({
    id: "verify",
    phases: Array.from({ length: 9 }, (_, index) => ({
      order: String(index + 1),
      title: `Phase ${index + 1}`,
      steps: [],
    })),
    outcomes: [],
  } as CiVisualPlan["sections"][number]["nodes"][number]);

  await assert.rejects(
    syncFigmaDesignModel(mainDesignModel(), fakeDependencies(calls), {
      targets: ["preflight", "ci.overview"],
      ciVisualPlan: oversizedPlan,
    }),
    /reserves only 8 slots/
  );
  assert.deepEqual(calls, []);
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
      async syncCiDocumentation(targetNames, _visualPlan) {
        calls.push("ci");
        return {
          updatedCiSections: targetNames,
          createdCiNodes: ["ci-node"],
          updatedCiSteps: ["ci-step"],
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
            writerHash: "sha256:writer",
            transportHash: "sha256:transport",
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

function ciVisualPlan(...targets: string[]): CiVisualPlan {
  return {
    schemaVersion: 4,
    parentName: "Continuous Integration and Documentation Automation",
    sections: targets.map((target) => ({
      target,
      name: target,
      description: target,
      orientation: "horizontal",
      headerSources: [],
      nodes: [],
      connections: [],
    })),
  };
}
