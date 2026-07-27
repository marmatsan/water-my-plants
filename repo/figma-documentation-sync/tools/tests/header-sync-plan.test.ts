import assert from "node:assert/strict";
import test from "node:test";
import { HEADER_SECTION_TARGETS } from "@figma-documentation-sync/project-config";
import {
  headerLinkNeedsLeftAlignment,
  headerLinkRanges,
  headerLinkText,
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

test("header source map covers every managed parent documentation section", () => {
  assert.deepEqual(
    HEADER_SECTION_TARGETS.map((target) => target.sectionNodeId),
    [
      "63685:108540",
      "62936:183",
      "63099:949",
    ]
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
});
