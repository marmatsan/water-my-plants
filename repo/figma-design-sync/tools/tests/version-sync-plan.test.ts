import assert from "node:assert/strict";
import test from "node:test";
import { planDependencyVersionInstanceSync } from "../src/figma/figma-version-sync-gateway";

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

function dependencyVersion(id: string, versionKey: string) {
  return {
    id,
    versionKey,
  };
}
