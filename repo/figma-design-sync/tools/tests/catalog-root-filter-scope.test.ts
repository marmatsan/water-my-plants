import assert from "node:assert/strict";
import test from "node:test";
import {
  buildPartialCatalogSyncScope,
  removeStaleCatalogNodes,
} from "../src/figma/figma-catalog-tree-sync-gateway";

const target = { name: "waterMyPlants.libraries" };

test("partial catalog scope reaches stale descendants through managed connectors", () => {
  const instancesByLabel = new Map([
    ["androidx", instance("androidx-id")],
    ["legacy-androidx", instance("legacy-androidx-id")],
    ["com", instance("com-id")],
    ["legacy-com", instance("legacy-com-id")],
  ]);
  const connectors = [
    connector("androidx-legacy-edge", "androidx-id", "legacy-androidx-id"),
    connector("com-legacy-edge", "com-id", "legacy-com-id"),
  ];

  const scope = buildPartialCatalogSyncScope({
    expectedNodes: [catalogNode("androidx")],
    rootLabels: ["androidx"],
    instancesByLabel,
    connectors,
  });

  assert.deepEqual([...scope.labels].sort(), ["androidx", "legacy-androidx"]);
});

test("partial stale cleanup removes only scoped stale nodes and connectors", () => {
  const instancesByLabel = new Map([
    ["androidx", instance("androidx-id")],
    ["legacy-androidx", instance("legacy-androidx-id")],
    ["com", instance("com-id")],
    ["legacy-com", instance("legacy-com-id")],
  ]);
  const connectors = [
    connector("androidx-legacy-edge", "androidx-id", "legacy-androidx-id"),
    connector("com-legacy-edge", "com-id", "legacy-com-id"),
  ];
  const scope = buildPartialCatalogSyncScope({
    expectedNodes: [catalogNode("androidx")],
    rootLabels: ["androidx"],
    instancesByLabel,
    connectors,
  });

  const result = removeStaleCatalogNodes(
    target,
    [catalogNode("androidx")],
    instancesByLabel,
    connectors,
    scope.labels
  );

  assert.deepEqual(result.removedCatalogNodes, ["waterMyPlants.libraries/legacy-androidx"]);
  assert.deepEqual(result.removedCatalogConnectors, ["waterMyPlants.libraries/androidx-legacy-edge"]);
  assert.equal(instancesByLabel.has("androidx"), true);
  assert.equal(instancesByLabel.has("com"), true);
  assert.equal(instancesByLabel.has("legacy-com"), true);
  assert.equal(connectors[0].removed, true);
  assert.equal(connectors[1].removed, false);
});

test("partial scope includes expected missing descendants even before Figma nodes exist", () => {
  const instancesByLabel = new Map([
    ["androidx", instance("androidx-id")],
    ["com", instance("com-id")],
  ]);

  const scope = buildPartialCatalogSyncScope({
    expectedNodes: [
      catalogNode("androidx"),
      catalogNode("activity", ["androidx"]),
    ],
    rootLabels: ["androidx"],
    instancesByLabel,
    connectors: [],
  });

  assert.equal(scope.labels.has("androidx"), true);
  assert.equal(scope.labels.has("activity"), true);
  assert.equal(scope.labels.has("com"), false);
});

function catalogNode(label: string, parentPath: string[] = []) {
  return {
    label,
    parentPath,
    path: [...parentPath, label],
  };
}

function instance(id: string) {
  return {
    id,
    parent: null,
    removed: false,
    remove() {
      this.removed = true;
    },
  };
}

function connector(id: string, parentInstanceId: string, childInstanceId: string) {
  return {
    id,
    removed: false,
    getSharedPluginData(_namespace: string, key: string) {
      return key === "treeConnectorEdge"
        ? `${parentInstanceId}->${childInstanceId}`
        : "";
    },
    remove() {
      this.removed = true;
    },
  };
}
