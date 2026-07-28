import assert from "node:assert/strict";
import test from "node:test";
import {
  HEADER_DEFINITION_PROPERTY_NAME,
  HEADER_SECTION_TARGETS,
  PARENT_SECTION_NODE_IDS,
} from "@figma-documentation-sync/project-config";
import {
  headerLinkNeedsLeftAlignment,
  headerLinkRanges,
  headerLinkText,
  headerTextPropertyUpdates,
} from "../src/figma/figma-header-sync-gateway";

test("header link text gives every source its own hyperlink range", () => {
  const links = [
    { label: "first.kt", url: "https://example.test/first.kt" },
    { label: "second.kt", url: "https://example.test/second.kt" },
  ];

  assert.equal(headerLinkText(links), "first.kt\nsecond.kt");
  assert.deepEqual(headerLinkRanges(links), [
    { start: 0, end: 8, url: "https://example.test/first.kt" },
    { start: 9, end: 18, url: "https://example.test/second.kt" },
  ]);
});

test("header links require left horizontal alignment", () => {
  assert.equal(headerLinkNeedsLeftAlignment({ textAlignHorizontal: "CENTER" }), true);
  assert.equal(headerLinkNeedsLeftAlignment({ textAlignHorizontal: "LEFT" }), false);
});

test("header text updates include an explicitly managed definition", () => {
  assert.deepEqual(
    headerTextPropertyUpdates(
      {
        "Link#1": { type: "TEXT", value: "old.kt" },
        "Definition#2": { type: "TEXT", value: "Old definition" },
      },
      {
        links: [{ label: "current.kt", url: "https://example.test/current.kt" }],
        definition: "Current definition",
      },
      "current.kt",
      "header-id"
    ),
    {
      "Link#1": "current.kt",
      "Definition#2": "Current definition",
    }
  );
});

test("header source map covers every managed parent documentation section", () => {
  assert.equal(HEADER_DEFINITION_PROPERTY_NAME, "Definition");
  assert.deepEqual(PARENT_SECTION_NODE_IDS, [
    "63685:108540",
    "62936:183",
    "63099:949",
    "64886:247",
    "64886:248",
  ]);
  assert.deepEqual(
    HEADER_SECTION_TARGETS.map((target) => target.sectionNodeId),
    PARENT_SECTION_NODE_IDS
  );
  assert.equal(
    HEADER_SECTION_TARGETS
      .flatMap((target) => target.links)
      .some((link) => link.url.includes("build-logic")),
    false
  );
  assert.deepEqual(
    HEADER_SECTION_TARGETS[0].links.map((link) => link.label),
    [
      "repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/library/LibraryTreeDsl.kt",
      "repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/library/LibraryScope.kt",
      "repo/dependency-catalog/catalog-core/src/main/kotlin/com/marmatsan/dependencies/tree/dsl/plugin/PluginTreeDsl.kt",
    ]
  );
  assert.deepEqual(
    HEADER_SECTION_TARGETS[2].links.map((link) => link.label),
    [
      "repo/water-my-plants-project-config/catalog/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/catalog/LibraryTrees.kt",
      "repo/water-my-plants-project-config/catalog/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/catalog/PluginTrees.kt",
    ]
  );
  assert.equal(
    HEADER_SECTION_TARGETS[1].definition,
    "Represents repo/water-my-plants-project-config/versions.properties, " +
      "the repository-owned source for dependency and plugin versions consumed by the Gradle builds."
  );
  assert.deepEqual(
    HEADER_SECTION_TARGETS[3].links.map((link) => link.label),
    ["repo/gradle-plugins"]
  );
  assert.deepEqual(
    HEADER_SECTION_TARGETS[4].links.map((link) => link.label),
    [
      "repo/dependency-catalog/catalog-gradle-plugin/src/main/kotlin/com/marmatsan/dependencies/gradle/DependencyCatalogSettingsPlugin.kt",
      "repo/figma-documentation-sync/plugin/src/main/kotlin/com/marmatsan/figmaDocumentationSync/plugin/gradle/FigmaDocumentationSyncGradlePlugin.kt",
      "repo/verification-platform/plugin/src/main/kotlin/com/marmatsan/verificationPlatform/plugin/VerificationPlatformPlugin.kt",
      "repo/water-my-plants-project-config/plugin/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/gradle/WaterMyPlantsProjectConfigPlugin.kt",
      "repo/water-my-plants-project-config/plugin/src/main/kotlin/com/marmatsan/waterMyPlants/projectConfig/gradle/WaterMyPlantsSettingsPlugin.kt",
    ]
  );
});
