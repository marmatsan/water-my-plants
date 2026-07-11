import assert from "node:assert/strict";
import test from "node:test";
import {
  dependencyVersionGridPosition,
  planDependencyVersionInstanceSync,
} from "../src/figma/figma-version-sync-gateway";

test("version sync removes stale renamed dependency versions and exact duplicates", () => {
  const instances = [
    dependencyVersion("old-library-name", "activityComposeVersion"),
    dependencyVersion("current-library-name", "activityComposeLibraryVersion"),
    dependencyVersion("duplicate-current-library-name", "activityComposeLibraryVersion"),
    dependencyVersion("unknown", ""),
  ];

  const plan = planDependencyVersionInstanceSync(
    instances,
    ["activityComposeLibraryVersion", "mockkLibraryVersion"],
    (instance) => instance.versionKey
  );

  assert.equal(
    plan.existingInstancesByVersionKey.get("activityComposeLibraryVersion")?.id,
    "current-library-name"
  );
  assert.deepEqual(
    plan.staleInstances.map((instance) => instance.id),
    ["old-library-name", "unknown"]
  );
  assert.deepEqual(
    plan.duplicateInstances.map((instance) => instance.id),
    ["duplicate-current-library-name"]
  );
});

test("dependency version grid positions two items per row with documented gaps", () => {
  const rowHeights = [40, 52, 36];

  assert.deepEqual(dependencyVersionGridPosition(0, 300, rowHeights), { x: 0, y: 0 });
  assert.deepEqual(dependencyVersionGridPosition(1, 300, rowHeights), { x: 364, y: 0 });
  assert.deepEqual(dependencyVersionGridPosition(2, 300, rowHeights), { x: 0, y: 72 });
  assert.deepEqual(dependencyVersionGridPosition(3, 300, rowHeights), { x: 364, y: 72 });
  assert.deepEqual(dependencyVersionGridPosition(4, 300, rowHeights), { x: 0, y: 156 });
});

function dependencyVersion(id: string, versionKey: string) {
  return {
    id,
    versionKey,
  };
}
