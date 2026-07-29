import assert from "node:assert/strict";
import test from "node:test";
import { TREE_NODE_PROPS } from "@figma-documentation-sync/project-config";
import {
  libraryArtifacts,
  libraryBundles,
} from "../src/domain/catalog/library-catalog-entries";
import { directCatalogItemInstances } from "../src/figma/figma-consumer-modules-gateway";
import {
  canRepresentLibraryCatalogEntries,
  configureLibraryCatalogItemSlots,
} from "../src/figma/figma-library-tree-node-capacity";
import { findTreeNodeTemplate } from "../src/figma/figma-tree-node-gateway";
import { requireNestedTemplateInstance } from "../src/figma/figma-visual-contract-check-gateway";

test("direct artifact selection excludes rows owned by an artifacts bundle", () => {
  const root = {
    id: "tree-node",
    children: [
      {
        id: "artifacts-container",
        name: "artifacts",
        children: [],
      },
    ],
  };
  const bundle = {
    id: "bundle",
    type: "INSTANCE",
    name: ".artifacts bundle",
    parent: root,
  };
  const nestedArtifact = {
    id: "bundle-artifact",
    type: "INSTANCE",
    name: ".artifact",
    parent: bundle,
  };
  root.findAllWithCriteria = () => [bundle, nestedArtifact];

  assert.deepEqual(
    directCatalogItemInstances(
      root,
      ".artifact"
    ),
    []
  );
});

test("preflight validates hidden templates through an instance main component", async () => {
  const expected = { id: "tool-usage", name: ".tool artifact usage" };
  const mainComponent = {
    findAllWithCriteria: () => [expected],
  };
  const hiddenArtifactInstance = {
    id: "artifact-instance",
    type: "INSTANCE",
    findAllWithCriteria: () => [],
    getMainComponentAsync: async () => mainComponent,
  };

  assert.equal(
    await requireNestedTemplateInstance(
      hiddenArtifactInstance,
      ".tool artifact usage"
    ),
    expected
  );
});

test("bundle entries keep child artifact versions hidden and expose only the bundle version", () => {
  const entries = [
    {
      type: "bundle",
      alias: "composeBundle",
      artifacts: [
        "ui",
        "ui-graphics",
        "ui-tooling",
        "ui-tooling-preview",
      ],
      version: {
        value: "1.7.8",
        visible: true,
      },
      requiredByModules: [],
      providedByConventionPlugins: [
        {
          pluginId: "com.marmatsan.compose",
          pluginModule: ":gradle-plugins:compose",
          requiredByModules: [
            ":app",
            ":core:ui",
            ":onboarding:ui",
          ],
        },
      ],
    },
  ];

  assert.deepEqual(libraryArtifacts(entries), []);
  assert.deepEqual(libraryBundles(entries), [
    {
      alias: "composeBundle",
      version: {
        value: "1.7.8",
        visible: true,
      },
      artifacts: [
        {
          name: "ui",
          version: {
            value: null,
            visible: false,
          },
          requiredByModules: [],
          providedByConventionPlugins: [],
          configuredByConventionPlugins: [],
          isCatalogEntry: false,
        },
        {
          name: "ui-graphics",
          version: {
            value: null,
            visible: false,
          },
          requiredByModules: [],
          providedByConventionPlugins: [],
          configuredByConventionPlugins: [],
          isCatalogEntry: false,
        },
        {
          name: "ui-tooling",
          version: {
            value: null,
            visible: false,
          },
          requiredByModules: [],
          providedByConventionPlugins: [],
          configuredByConventionPlugins: [],
          isCatalogEntry: false,
        },
        {
          name: "ui-tooling-preview",
          version: {
            value: null,
            visible: false,
          },
          requiredByModules: [],
          providedByConventionPlugins: [],
          configuredByConventionPlugins: [],
          isCatalogEntry: false,
        },
      ],
      requiredByModules: [
        ":app",
        ":core:ui",
        ":onboarding:ui",
      ],
      providedByConventionPlugins: [
        {
          pluginId: "com.marmatsan.compose",
          pluginModule: ":gradle-plugins:compose",
          requiredByModules: [
            ":app",
            ":core:ui",
            ":onboarding:ui",
          ],
        },
      ],
      isCatalogEntry: true,
    },
  ]);
});

test("library tree node capacity rejects a legacy direct-artifact-only structure when the model requires a bundle", () => {
  const instance = libraryTreeNodeStructure(3, []);

  assert.equal(
    canRepresentLibraryCatalogEntries(instance, lifecycleCatalogNode()),
    false
  );
});

test("library tree node capacity accepts direct artifacts and sufficiently large bundle slots", () => {
  const instance = libraryTreeNodeStructure(1, [2]);

  assert.equal(
    canRepresentLibraryCatalogEntries(instance, lifecycleCatalogNode()),
    true
  );
});

test("library tree node capacity rejects a bundle slot with too few nested artifact rows", () => {
  const instance = libraryTreeNodeStructure(1, [1]);

  assert.equal(
    canRepresentLibraryCatalogEntries(instance, lifecycleCatalogNode()),
    false
  );
});

test("library template selection reuses a local node with enough interchangeable slots", () => {
  const incompatible = libraryTreeNodeStructure(3, []);
  incompatible.id = "incompatible";
  incompatible.name = ".tree node";
  incompatible.componentProperties = libraryTreeNodeProperties(true);
  const compatible = libraryTreeNodeStructure(1, [2]);
  compatible.id = "compatible";
  compatible.name = ".tree node";
  compatible.componentProperties = libraryTreeNodeProperties(true);
  const container = searchableContainer([incompatible]);
  const section = searchableContainer([incompatible, compatible]);

  assert.equal(
    findTreeNodeTemplate(section, container, {
      ...lifecycleCatalogNode(),
      artifactsVisible: true,
    }),
    incompatible
  );
});

test("library catalog-item slots can mix artifacts and bundles without a precomposed tree-node template", async () => {
  const artifactComponent = catalogItemComponent("artifact-component", ".artifact", 0);
  const bundleComponent = catalogItemComponent("bundle-component", ".artifacts bundle", 4);
  const instance = configurableLibraryTreeNode(3, artifactComponent);
  const bundleTemplate = configurableCatalogItemSlot("bundle-template", bundleComponent);
  const section = searchableContainer([
    ...instance.children[0].children,
    bundleTemplate,
  ]);
  const mutatedNodeIds = [];

  const configured = await configureLibraryCatalogItemSlots(
    section,
    instance,
    lifecycleCatalogNode(),
    mutatedNodeIds
  );

  assert.equal(configured, true);
  assert.equal(instance.children[0].children[0].name, ".artifact");
  assert.equal(instance.children[0].children[1].name, ".artifacts bundle");
  assert.equal(instance.children[0].children[1].children.length, 4);
  assert.deepEqual(
    instance.children[0].children[1].children.map((artifact) => artifact.visible),
    [true, true, false, false]
  );
  assert.equal(canRepresentLibraryCatalogEntries(instance, lifecycleCatalogNode()), true);
  assert.deepEqual(mutatedNodeIds, ["slot-1", "slot-1-artifact-1"]);
});

test("library slot configuration restores Figma invisible-child traversal after failure", async () => {
  const previousFigma = (globalThis as { figma?: unknown }).figma;
  const figmaApi = { skipInvisibleInstanceChildren: true };
  (globalThis as { figma?: unknown }).figma = figmaApi;
  const instance = configurableLibraryTreeNode(
    3,
    catalogItemComponent("artifact-component", ".artifact", 0)
  );

  try {
    const configured = await configureLibraryCatalogItemSlots(
      searchableContainer(instance.children[0].children),
      instance,
      lifecycleCatalogNode(),
      []
    );

    assert.equal(configured, false);
    assert.equal(figmaApi.skipInvisibleInstanceChildren, true);
  } finally {
    (globalThis as { figma?: unknown }).figma = previousFigma;
  }
});

function lifecycleCatalogNode() {
  return {
    type: "Library",
    label: "lifecycle",
    parentPath: ["androidx"],
    path: ["androidx", "lifecycle"],
    children: [],
    entries: [
      {
        type: "artifact",
        artifact: "lifecycle-runtime-ktx",
        version: { value: "1.0", visible: true },
        requiredByModules: [":app"],
      },
      {
        type: "bundle",
        alias: "lifecycleComposeBundle",
        artifacts: [
          "lifecycle-runtime-compose",
          "lifecycle-viewmodel-compose",
        ],
        version: { value: "1.0", visible: true },
        requiredByModules: [":app"],
      },
    ],
  };
}

function libraryTreeNodeStructure(directArtifactCount: number, bundleArtifactCounts: number[]) {
  const directArtifacts = Array.from({ length: directArtifactCount }, (_, index) => ({
    id: `artifact-${index}`,
    type: "INSTANCE",
    name: ".artifact",
  }));
  const bundles = bundleArtifactCounts.map((artifactCount, bundleIndex) => ({
    id: `bundle-${bundleIndex}`,
    type: "INSTANCE",
    name: ".artifacts bundle",
    children: Array.from({ length: artifactCount }, (_, artifactIndex) => ({
      id: `bundle-${bundleIndex}-artifact-${artifactIndex}`,
      type: "INSTANCE",
      name: ".artifact",
    })),
  }));
  const catalogItems = [...directArtifacts, ...bundles];

  return {
    id: "tree-node",
    children: [{ id: "artifacts", name: "artifacts", children: catalogItems }],
    findAllWithCriteria() {
      return catalogItems;
    },
  };
}

function libraryTreeNodeProperties(showArtifacts: boolean) {
  return {
    [TREE_NODE_PROPS.type]: { value: "Library" },
    [TREE_NODE_PROPS.showArtifacts]: { value: showArtifacts },
  };
}

function searchableContainer(instances) {
  return {
    findAllWithCriteria() {
      return instances;
    },
  };
}

function catalogItemComponent(id: string, name: string, nestedArtifactCount: number) {
  return {
    id,
    name,
    nestedArtifactCount,
  };
}

function configurableLibraryTreeNode(slotCount: number, component) {
  const slots = Array.from({ length: slotCount }, (_, index) =>
    configurableCatalogItemSlot(`slot-${index}`, component)
  );
  return {
    id: "configurable-tree-node",
    children: [{ id: "artifacts", name: "artifacts", children: slots }],
    findAllWithCriteria() {
      return this.children[0].children.flatMap((slot) => [slot, ...(slot.children || [])]);
    },
  };
}

function configurableCatalogItemSlot(id: string, initialComponent) {
  return {
    id,
    type: "INSTANCE",
    name: initialComponent.name,
    children: nestedArtifacts(id, initialComponent.nestedArtifactCount),
    async getMainComponentAsync() {
      return this.component;
    },
    component: initialComponent,
    swapComponent(component) {
      this.component = component;
      this.children = nestedArtifacts(id, component.nestedArtifactCount);
    },
    findAllWithCriteria() {
      return this.children;
    },
  };
}

function nestedArtifacts(parentId: string, count: number) {
  return Array.from({ length: count }, (_, index) => ({
    id: `${parentId}-artifact-${index}`,
    type: "INSTANCE",
    name: ".artifact",
    visible: index === 0,
  }));
}
