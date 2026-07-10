import assert from "node:assert/strict";
import test from "node:test";
import { CATALOG_TREE_TARGETS } from "../src/config/figma-config";

test("custom Gradle convention plugin target warns when leaf plugins are not applied by any module", () => {
  const target = CATALOG_TREE_TARGETS.find((candidate) =>
    candidate.name === "waterMyPlants.customGradleConventionPlugins"
  );

  assert.equal(target?.type, "Plugin");
  assert.equal(target?.gradlePluginNodes, true);
  assert.equal(target?.warnWhenUnused, true);
});

test("custom regular Gradle plugin target keeps the same unused warning contract", () => {
  const target = CATALOG_TREE_TARGETS.find((candidate) =>
    candidate.name === "waterMyPlants.customGradlePlugins"
  );

  assert.equal(target?.type, "Plugin");
  assert.equal(target?.gradlePluginNodes, true);
  assert.equal(target?.warnWhenUnused, true);
});

test("main project targets are stable documentation targets", () => {
  const targets = CATALOG_TREE_TARGETS.filter((candidate) =>
    candidate.name.startsWith("waterMyPlants.")
  );

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

test("included build settings catalog targets are declared catalog targets", () => {
  const targets = CATALOG_TREE_TARGETS.filter((candidate) =>
    candidate.name.startsWith("gradlePlugins.") ||
      candidate.name.startsWith("figmaDesignSync.")
  );

  assert.deepEqual(
    targets.map((target) => [target.name, target.lifecycle]),
    [
      ["gradlePlugins.libraries", "declaredCatalogTarget"],
      ["gradlePlugins.plugins", "declaredCatalogTarget"],
      ["figmaDesignSync.libraries", "declaredCatalogTarget"],
      ["figmaDesignSync.plugins", "declaredCatalogTarget"],
    ]
  );
});
