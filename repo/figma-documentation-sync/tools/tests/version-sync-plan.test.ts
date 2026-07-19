import assert from "node:assert/strict";
import test from "node:test";
import {
  dependencyVersionGridPosition,
  planDependencyVersionInstanceSync,
} from "../src/figma/figma-version-sync-gateway";
import { sectionStrokeContractSatisfied } from "../src/figma/figma-node-gateway";

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

test("dependency version grid keeps equal section padding and documented gaps", () => {
  const rowHeights = [40, 52, 36];

  assert.deepEqual(dependencyVersionGridPosition(0, 300, rowHeights), { x: 100, y: 100 });
  assert.deepEqual(dependencyVersionGridPosition(1, 300, rowHeights), { x: 464, y: 100 });
  assert.deepEqual(dependencyVersionGridPosition(2, 300, rowHeights), { x: 100, y: 172 });
  assert.deepEqual(dependencyVersionGridPosition(3, 300, rowHeights), { x: 464, y: 172 });
  assert.deepEqual(dependencyVersionGridPosition(4, 300, rowHeights), { x: 100, y: 256 });
});

test("section stroke contract requires the bound outline variable, inside alignment, and weight two", () => {
  const compliantSection = {
    children: [],
    strokeAlign: "INSIDE",
    strokeWeight: 2,
    strokes: [{
      type: "SOLID",
      boundVariables: { color: { id: "outline-variable" } },
    }],
  };

  assert.equal(sectionStrokeContractSatisfied(compliantSection, "outline-variable"), true);
  assert.equal(sectionStrokeContractSatisfied({ ...compliantSection, strokeWeight: 1 }, "outline-variable"), false);
  assert.equal(sectionStrokeContractSatisfied({ ...compliantSection, strokeAlign: "CENTER" }, "outline-variable"), false);
  assert.equal(sectionStrokeContractSatisfied(compliantSection, "another-variable"), false);
});

test("parent sections with a direct Header satisfy the contract only without strokes", () => {
  const parentSection = {
    children: [{ type: "INSTANCE", name: ".Header" }],
    strokeAlign: "INSIDE",
    strokeWeight: 2,
    strokes: [],
  };

  assert.equal(sectionStrokeContractSatisfied(parentSection, "outline-variable"), true);
  assert.equal(
    sectionStrokeContractSatisfied({
      ...parentSection,
      strokes: [{
        type: "SOLID",
        boundVariables: { color: { id: "outline-variable" } },
      }],
    }, "outline-variable"),
    false
  );
});

function dependencyVersion(id: string, versionKey: string) {
  return {
    id,
    versionKey,
  };
}
