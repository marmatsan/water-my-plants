import assert from "node:assert/strict";
import test from "node:test";
import { CATALOG_TREE_TARGETS } from "@figma-documentation-sync/project-config";

test("only Water My Plants production catalogs are visual targets", () => {
  const targets = CATALOG_TREE_TARGETS;

  assert.deepEqual(
    targets.map((target) => [target.name, target.lifecycle]),
    [
      ["waterMyPlants.libraries", "stableDocumentationTarget"],
      ["waterMyPlants.plugins", "stableDocumentationTarget"],
    ]
  );
});
