import assert from "node:assert/strict";
import test from "node:test";
import { presentPluginVersions, presentedVersion } from "../src/domain/catalog/plugin-version-presentation";

test("presentedVersion distinguishes a shared reference from its resolved value", () => {
  assert.deepEqual(
    presentedVersion(
      { value: "gradlePluginsVersion", visible: true },
      { gradlePluginsVersion: "0.1.0-SNAPSHOT" },
      new Set(["gradlePluginsVersion"])
    ),
    {
      value: "gradlePluginsVersion",
      visible: true,
      displayValue: "ref gradlePluginsVersion\nresolved 0.1.0-SNAPSHOT\npolicy shared",
    }
  );
});

test("presentPluginVersions preserves unresolved references and source values", () => {
  const sourceNode = {
    type: "Plugin" as const,
    label: "android",
    parentPath: ["com", "marmatsan"],
    path: ["com", "marmatsan", "android"],
    children: [],
    version: { value: "unknownPluginVersion", visible: true },
  };

  assert.deepEqual(
    presentPluginVersions(
      [sourceNode],
      {
        name: "waterMyPlants.plugins",
        sectionNodeId: "section",
        type: "Plugin",
        lifecycle: "stableDocumentationTarget",
        nodes: () => [],
        versionValuesPath: ["content", "versions"],
      },
      { content: { versions: {} } }
    ),
    [sourceNode]
  );
});
