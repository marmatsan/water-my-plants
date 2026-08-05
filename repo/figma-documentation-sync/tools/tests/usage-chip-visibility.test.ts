import assert from "node:assert/strict";
import test from "node:test";
import {
  gradleProjectDisplayName,
  updateUsageChipInstances,
} from "../src/figma/figma-consumer-modules-gateway";

test("Gradle root project paths use an explicit visual label", () => {
  assert.equal(gradleProjectDisplayName(":"), "Water My Plants — root project (:)");
  assert.equal(gradleProjectDisplayName(":app"), ":app");
});

test("usage chip synchronization reveals hidden reserved slots and restores traversal", async () => {
  const previousFigma = (globalThis as { figma?: unknown }).figma;
  const figmaApi = {
    skipInvisibleInstanceChildren: true,
    mixed: Symbol("mixed"),
    loadFontAsync: async () => undefined,
  };
  (globalThis as { figma?: unknown }).figma = figmaApi;
  const root = usageChipTree(figmaApi, 4);
  const mutatedNodeIds = [];

  try {
    await updateUsageChipInstances(
      root,
      "Applied by module",
      [":app", ":core:ui", ":onboarding:ui"].map((name) => ({ kind: "module", name })),
      mutatedNodeIds
    );

    const chips = root.children[0].children[1].children;
    assert.deepEqual(chips.map((chip) => chip.visible), [true, true, true, false]);
    assert.deepEqual(
      chips.slice(0, 3).map((chip) => chip.componentProperties.name.value),
      [":app", ":core:ui", ":onboarding:ui"]
    );
    assert.equal(figmaApi.skipInvisibleInstanceChildren, true);
    assert.ok(mutatedNodeIds.includes("chip-2"));
  } finally {
    (globalThis as { figma?: unknown }).figma = previousFigma;
  }
});

function usageChipTree(figmaApi, slotCount: number) {
  const root = containerNode("tree-node", "INSTANCE", ".tree node", true, figmaApi);
  const block = containerNode("applied-block", "INSTANCE", ".usage block", true, figmaApi);
  const heading = textNode("heading", "Applied by module");
  const slots = containerNode("module-slots", "FRAME", "modules", true, figmaApi);
  const chips = Array.from({ length: slotCount }, (_, index) => usageChip(index, figmaApi));

  attach(slots, chips);
  attach(block, [heading, slots]);
  attach(root, [block]);
  return root;
}

function usageChip(index: number, figmaApi) {
  const chip = containerNode(`chip-${index}`, "INSTANCE", ".usage chip", false, figmaApi);
  chip.componentProperties = {
    kind: { type: "VARIANT", value: "module" },
    name: { type: "TEXT", value: "module" },
  };
  chip.setProperties = (properties) => {
    for (const [key, value] of Object.entries(properties)) {
      chip.componentProperties[key].value = value;
    }
  };
  attach(chip, [textNode(`label-${index}`, "module")]);
  return chip;
}

function containerNode(id: string, type: string, name: string, visible: boolean, figmaApi) {
  return {
    id,
    type,
    name,
    visible,
    x: 0,
    y: 0,
    width: 200,
    height: 48,
    children: [],
    findAllWithCriteria(criteria) {
      return descendants(this, figmaApi)
        .filter((node) => criteria.types.includes(node.type));
    },
    resizeWithoutConstraints(width, height) {
      this.width = width;
      this.height = height;
    },
  };
}

function textNode(id: string, characters: string) {
  return {
    id,
    type: "TEXT",
    name: "label",
    visible: true,
    characters,
    x: 0,
    y: 0,
    width: 40,
    height: 16,
    getStyledTextSegments: () => [],
  };
}

function attach(parent, children) {
  parent.children = children;
  for (const child of children) child.parent = parent;
}

function descendants(root, figmaApi) {
  return root.children.flatMap((child) => {
    if (figmaApi.skipInvisibleInstanceChildren && child.visible === false) return [];
    return [child, ...("children" in child ? descendants(child, figmaApi) : [])];
  });
}
