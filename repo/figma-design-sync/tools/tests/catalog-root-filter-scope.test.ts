import assert from "node:assert/strict";
import test from "node:test";
import {
  buildPartialCatalogSyncScope,
  constrainCatalogLayoutToPadding,
  removeEmptyStaleCatalogRootSections,
  removeStaleCatalogNodes,
} from "../src/figma/figma-catalog-tree-sync-gateway";
import { stackChildSectionsFromPadding } from "../src/figma/figma-node-gateway";

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

test("stale empty root section cleanup removes roots that are no longer in the model", () => {
  const mutatedNodeIds = [];
  const section = parentSection([
    childSection("androidx", ["tree-node"]),
    childSection("legacy", []),
  ]);

  const result = removeEmptyStaleCatalogRootSections(
    target,
    section,
    ["androidx"],
    mutatedNodeIds
  );

  assert.deepEqual(result, ["waterMyPlants.libraries/legacy"]);
  assert.deepEqual(mutatedNodeIds, ["legacy-id"]);
  assert.equal(section.children.length, 1);
  assert.equal(section.children[0].name, "androidx");
});

test("stale empty root section cleanup keeps expected roots and non-empty roots", () => {
  const mutatedNodeIds = [];
  const section = parentSection([
    childSection("androidx", []),
    childSection("legacy", ["manual-content"]),
  ]);

  const result = removeEmptyStaleCatalogRootSections(
    target,
    section,
    ["androidx"],
    mutatedNodeIds
  );

  assert.deepEqual(result, []);
  assert.deepEqual(mutatedNodeIds, []);
  assert.deepEqual(section.children.map((child) => child.name), ["androidx", "legacy"]);
});

test("stale empty root section cleanup can remove empty roots outside a partial sync filter", () => {
  const mutatedNodeIds = [];
  const section = parentSection([
    childSection("com", ["tree-node"]),
    childSection("io", ["tree-node"]),
    childSection("me", []),
  ]);

  const result = removeEmptyStaleCatalogRootSections(
    target,
    section,
    ["com", "io"],
    mutatedNodeIds
  );

  assert.deepEqual(result, ["waterMyPlants.libraries/me"]);
  assert.deepEqual(mutatedNodeIds, ["me-id"]);
  assert.deepEqual(section.children.map((child) => child.name), ["com", "io"]);
});

test("child section reflow normalizes the first remaining root to container padding", () => {
  const mutatedNodeIds = [];
  const section = parentSection([
    positionedChildSection("io", 0, 1458, 2353, 1898),
    positionedChildSection("me", 0, 3470, 948, 1445),
    positionedChildSection("org", 0, 5029, 1690, 1445),
  ]);

  stackChildSectionsFromPadding(section, mutatedNodeIds);

  assert.deepEqual(section.children.map((child) => child.x), [100, 100, 100]);
  assert.equal(section.children[0].y, 100);
  assert.equal(section.children[1].y, 2112);
  assert.equal(section.children[2].y, 3671);
  assert.deepEqual(mutatedNodeIds, ["io-id", "me-id", "org-id"]);
});

test("child section reflow normalizes a single remaining root", () => {
  const mutatedNodeIds = [];
  const section = parentSection([
    positionedChildSection("org", 0, 5029, 1690, 1445),
  ]);

  stackChildSectionsFromPadding(section, mutatedNodeIds);

  assert.equal(section.children[0].x, 100);
  assert.equal(section.children[0].y, 100);
  assert.deepEqual(mutatedNodeIds, ["org-id"]);
});

test("partial root layout shifts a wide subtree inside its section padding", () => {
  const placements = new Map([
    ["root", { x: 824, y: 100 }],
    ["wide-leaf", { x: -280.5, y: 542 }],
    ["right-leaf", { x: 1329.5, y: 321 }],
  ]);
  const layout = {
    placements,
    minX: -280.5,
    maxX: 1986.5,
    nextX: 2106.5,
  };

  const offset = constrainCatalogLayoutToPadding(layout, 100);

  assert.equal(offset, 380.5);
  assert.equal(layout.minX, 100);
  assert.equal(layout.maxX, 2367);
  assert.equal(layout.nextX, 2487);
  assert.equal(placements.get("root").x, 1204.5);
  assert.equal(placements.get("wide-leaf").x, 100);
  assert.equal(placements.get("right-leaf").x, 1710);
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

function parentSection(children) {
  const section = {
    children,
  };

  for (const child of children) {
    child.parent = section;
  }

  return section;
}

function childSection(name: string, childIds: string[]) {
  return {
    id: `${name}-id`,
    name,
    type: "SECTION",
    children: childIds.map((id) => ({ id })),
    removed: false,
    parent: null,
    remove() {
      this.removed = true;
      const index = this.parent.children.indexOf(this);
      if (index >= 0) {
        this.parent.children.splice(index, 1);
      }
    },
  };
}

function positionedChildSection(name: string, x: number, y: number, width: number, height: number) {
  return {
    id: `${name}-id`,
    name,
    type: "SECTION",
    visible: true,
    x,
    y,
    width,
    height,
    children: [],
    parent: null,
  };
}
