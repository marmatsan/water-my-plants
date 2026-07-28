import assert from "node:assert/strict";
import test from "node:test";
import { CATALOG_TREE_TARGETS } from "@figma-documentation-sync/project-config";

test("Water My Plants catalogs and Gradle plugin inventories are visual targets", () => {
  const targets = CATALOG_TREE_TARGETS;

  assert.deepEqual(
    targets.map((target) => [target.name, target.lifecycle]),
    [
      ["waterMyPlants.libraries", "stableDocumentationTarget"],
      ["waterMyPlants.plugins", "stableDocumentationTarget"],
      ["waterMyPlants.customGradleConventionPlugins", "stableDocumentationTarget"],
      ["waterMyPlants.customGradlePlugins", "stableDocumentationTarget"],
    ]
  );
});
