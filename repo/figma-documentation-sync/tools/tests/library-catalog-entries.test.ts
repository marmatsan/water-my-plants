import assert from "node:assert/strict";
import test from "node:test";
import {
  libraryArtifacts,
  libraryBundles,
} from "../src/domain/catalog/library-catalog-entries";

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
