import assert from "node:assert/strict";
import test from "node:test";
import {
  libraryArtifacts,
  libraryBundles,
} from "../src/domain/catalog/library-catalog-entries";
import { requireNestedTemplateInstance } from "../src/figma/figma-visual-contract-check-gateway";
import { requireFreshInstance } from "../src/figma/figma-node-gateway";

test("tree node writers reacquire instances after component property updates", async () => {
  const staleInstance = {
    id: "tree-node-instance",
    type: "INSTANCE",
    findAllWithCriteria: () => [],
  };
  const refreshedInstance = {
    id: "tree-node-instance",
    type: "INSTANCE",
    findAllWithCriteria: () => [
      { id: "bundle-artifact", name: ".artifact" },
    ],
  };

  assert.equal(
    await requireFreshInstance(
      staleInstance,
      async () => refreshedInstance
    ),
    refreshedInstance
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

test("bundle entries keep artifacts inside the bundle instead of exposing direct artifacts", () => {
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
        value: null,
        visible: false,
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
        value: null,
        visible: false,
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
