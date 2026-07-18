import assert from "node:assert/strict";
import test from "node:test";
import { createVisualSyncPlan } from "../scripts/write-visual-sync-plan";

const manifest = {
  modelHash: "sha256:model-new",
  writerHash: "sha256:writer-new",
  transportHash: "sha256:transport-new",
  manifestHash: "sha256:manifest",
  writerScopeFingerprintSchemaVersion: 1,
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
  writerScopeFingerprints: {
    preflight: "sha256:preflight-writer-same",
    versions: "sha256:versions-writer-same",
    "waterMyPlants.libraries.androidx": "sha256:catalog-writer-same",
    "waterMyPlants.libraries.cleanup": "sha256:catalog-writer-same",
    metadata: "sha256:metadata-writer-same",
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
    previousMetadata({ modelHash: "sha256:model-same" })
  );

  assert.equal(plan.decision, "none");
  assert.equal(plan.requiresVisualWrite, false);
});

test("model changes select only changed target fingerprints plus preflight", () => {
  const plan = createVisualSyncPlan(manifest, previousMetadata({
    modelHash: "sha256:model-old",
    targetFingerprints: {
      ...manifest.targetFingerprints,
      preflight: "sha256:preflight-old",
      "waterMyPlants.libraries.androidx": "sha256:androidx-old",
    },
  }));

  assert.equal(plan.decision, "partial");
  assert.equal(plan.reason, "target-model-fingerprints-changed");
  assert.deepEqual(plan.executionScopes, ["preflight", "waterMyPlants.libraries.androidx"]);
  assert.match(plan.planHash, /^sha256:[a-f0-9]{64}$/);
});

test("target-specific writer changes select only that writer scope plus preflight", () => {
  const plan = createVisualSyncPlan(
    { ...manifest, modelHash: "sha256:model-same" },
    previousMetadata({
      modelHash: "sha256:model-same",
      writerHash: "sha256:writer-old",
      writerScopeFingerprints: {
        ...manifest.writerScopeFingerprints,
        versions: "sha256:versions-writer-old",
      },
    })
  );

  assert.equal(plan.decision, "partial");
  assert.equal(plan.reason, "writer-scope-fingerprints-changed");
  assert.deepEqual(plan.executionScopes, ["preflight", "versions"]);
});

test("metadata-only writer changes run preflight before refreshing metadata", () => {
  const plan = createVisualSyncPlan(
    { ...manifest, modelHash: "sha256:model-same" },
    previousMetadata({
      modelHash: "sha256:model-same",
      writerHash: "sha256:writer-old",
      writerScopeFingerprints: {
        ...manifest.writerScopeFingerprints,
        metadata: "sha256:metadata-writer-old",
      },
    })
  );

  assert.equal(plan.decision, "partial");
  assert.equal(plan.reason, "metadata-writer-fingerprint-changed");
  assert.deepEqual(plan.executionScopes, ["preflight"]);
});

test("model and writer changes use the union of affected scopes", () => {
  const plan = createVisualSyncPlan(manifest, previousMetadata({
    modelHash: "sha256:model-old",
    writerHash: "sha256:writer-old",
    targetFingerprints: {
      ...manifest.targetFingerprints,
      "waterMyPlants.libraries.androidx": "sha256:androidx-old",
    },
    writerScopeFingerprints: {
      ...manifest.writerScopeFingerprints,
      versions: "sha256:versions-writer-old",
    },
  }));

  assert.equal(plan.decision, "partial");
  assert.equal(plan.reason, "target-model-and-writer-fingerprints-changed");
  assert.deepEqual(
    plan.executionScopes,
    ["preflight", "waterMyPlants.libraries.androidx", "versions"]
  );
});

test("shared writer changes fail closed to a complete visual sync", () => {
  const oldWriterScopes = Object.fromEntries(
    Object.keys(manifest.writerScopeFingerprints).map((scope) => [scope, `sha256:${scope}-old`])
  );
  const plan = createVisualSyncPlan(
    { ...manifest, modelHash: "sha256:model-same" },
    previousMetadata({
      modelHash: "sha256:model-same",
      writerHash: "sha256:writer-old",
      writerScopeFingerprints: oldWriterScopes,
    })
  );

  assert.equal(plan.decision, "full");
  assert.equal(plan.reason, "shared-visual-writer-changed");
  assert.deepEqual(plan.executionScopes, Object.values(manifest.executionScopes));
});

test("unexplained writer changes fail closed to a complete visual sync", () => {
  const plan = createVisualSyncPlan(
    { ...manifest, modelHash: "sha256:model-same" },
    previousMetadata({ modelHash: "sha256:model-same", writerHash: "sha256:writer-old" })
  );

  assert.equal(plan.decision, "full");
  assert.equal(plan.reason, "writer-changed-outside-known-scope-fingerprints");
});

test("writer scope schema changes require a complete migration sync", () => {
  const plan = createVisualSyncPlan(manifest, previousMetadata({
    writerScopeFingerprintSchemaVersion: 2,
  }));

  assert.equal(plan.decision, "full");
  assert.equal(plan.reason, "writer-scope-fingerprint-schema-changed");
});

test("model changes outside known target fingerprints fail closed", () => {
  const plan = createVisualSyncPlan(manifest, previousMetadata({
    modelHash: "sha256:model-old",
  }));

  assert.equal(plan.decision, "full");
  assert.equal(plan.reason, "model-changed-outside-known-target-fingerprints");
});

test("legacy metadata without scoped writer fingerprints requires a full sync", () => {
  const plan = createVisualSyncPlan(manifest, {
    modelHash: "sha256:model-old",
    writerHash: "sha256:writer-old",
    targetFingerprints: {},
  });

  assert.equal(plan.decision, "full");
  assert.equal(plan.reason, "legacy-metadata-without-execution-fingerprints");
});

test("incomplete current scoped writer identity is rejected", () => {
  assert.throws(
    () => createVisualSyncPlan(
      {
        ...manifest,
        writerScopeFingerprints: {
          ...manifest.writerScopeFingerprints,
          versions: undefined,
        },
      },
      previousMetadata()
    ),
    /missing complete model-target or writer-scope fingerprints/
  );
});

test("incomplete previous scoped writer identity requires a full migration sync", () => {
  const plan = createVisualSyncPlan(manifest, previousMetadata({
    writerScopeFingerprints: {
      ...manifest.writerScopeFingerprints,
      versions: undefined,
    },
  }));

  assert.equal(plan.decision, "full");
  assert.equal(plan.reason, "legacy-metadata-without-execution-fingerprints");
});

function previousMetadata(overrides = {}) {
  return {
    modelHash: manifest.modelHash,
    writerHash: manifest.writerHash,
    targetFingerprints: manifest.targetFingerprints,
    writerScopeFingerprints: manifest.writerScopeFingerprints,
    writerScopeFingerprintSchemaVersion: manifest.writerScopeFingerprintSchemaVersion,
    ...overrides,
  };
}
