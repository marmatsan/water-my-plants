import assert from "node:assert/strict";
import test from "node:test";
import { createVisualSyncPlan } from "../scripts/write-visual-sync-plan";

const manifest = {
  modelHash: "sha256:model-new",
  writerHash: "sha256:writer",
  transportHash: "sha256:transport-new",
  manifestHash: "sha256:manifest",
  executionScopes: {
    "99-00-preflight.mcp.js": "preflight",
    "99-01-versions.mcp.js": "versions",
    "99-02-00-waterMyPlants-libraries-androidx.mcp.js": "waterMyPlants.libraries.androidx",
    "99-02-99-waterMyPlants-libraries-cleanup.mcp.js": "waterMyPlants.libraries.cleanup",
  },
  targetFingerprints: {
    preflight: "sha256:preflight-new",
    versions: "sha256:versions-same",
    "waterMyPlants.libraries.androidx": "sha256:androidx-new",
    "waterMyPlants.libraries.cleanup": "sha256:cleanup-same",
  },
};

test("missing Figma metadata fails closed to a complete visual sync", () => {
  const plan = createVisualSyncPlan(manifest, null);

  assert.equal(plan.decision, "full");
  assert.equal(plan.reason, "figma-metadata-unavailable");
  assert.deepEqual(plan.executionScopes, Object.values(manifest.executionScopes));
});

test("unchanged visual input skips Figma even when transport changes", () => {
  const plan = createVisualSyncPlan(
    { ...manifest, modelHash: "sha256:model-same" },
    {
      modelHash: "sha256:model-same",
      writerHash: manifest.writerHash,
      transportHash: "sha256:transport-old",
      targetFingerprints: manifest.targetFingerprints,
    }
  );

  assert.equal(plan.decision, "none");
  assert.equal(plan.requiresVisualWrite, false);
});

test("writer changes fail closed to a complete visual sync", () => {
  const plan = createVisualSyncPlan(manifest, {
    modelHash: "sha256:model-old",
    writerHash: "sha256:writer-old",
    targetFingerprints: {},
  });

  assert.equal(plan.decision, "full");
  assert.deepEqual(plan.executionScopes, Object.values(manifest.executionScopes));
});

test("model changes select only changed target fingerprints plus preflight", () => {
  const plan = createVisualSyncPlan(manifest, {
    modelHash: "sha256:model-old",
    writerHash: manifest.writerHash,
    targetFingerprints: {
      preflight: "sha256:preflight-old",
      versions: "sha256:versions-same",
      "waterMyPlants.libraries.androidx": "sha256:androidx-old",
      "waterMyPlants.libraries.cleanup": "sha256:cleanup-same",
    },
  });

  assert.equal(plan.decision, "partial");
  assert.deepEqual(plan.executionScopes, ["preflight", "waterMyPlants.libraries.androidx"]);
  assert.match(plan.planHash, /^sha256:[a-f0-9]{64}$/);
});

test("legacy metadata without writer fingerprints requires a full sync", () => {
  const plan = createVisualSyncPlan(manifest, {
    modelHash: "sha256:model-old",
  });

  assert.equal(plan.decision, "full");
  assert.equal(plan.reason, "legacy-metadata-without-execution-fingerprints");
});
