import assert from "node:assert/strict";
import { mkdir, mkdtemp, rm, writeFile } from "node:fs/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";
import test from "node:test";
import {
  CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY,
} from "@figma-design-sync/project-config";
import {
  createWriterScopeFingerprints,
  WRITER_SCOPE_FINGERPRINT_SCHEMA_VERSION,
} from "../scripts/writer-scope-fingerprints";

const REQUESTED_SCOPES = [
  "preflight",
  "versions",
  "waterMyPlants.libraries.androidx",
  "waterMyPlants.libraries.cleanup",
  "ci.overview",
];

test("target-specific writer changes invalidate only their execution family", async () => {
  const fixture = await createFixture();
  try {
    const before = await fingerprints(fixture);
    await writeFile(fixture.ciSource, "export const ci = 2;\n", "utf8");
    const after = await fingerprints(fixture);

    assert.notEqual(after["ci.overview"], before["ci.overview"]);
    assert.equal(after.preflight, before.preflight);
    assert.equal(after.versions, before.versions);
    assert.equal(
      after["waterMyPlants.libraries.androidx"],
      before["waterMyPlants.libraries.androidx"]
    );
    assert.equal(after.metadata, before.metadata);
  } finally {
    await rm(fixture.root, { recursive: true, force: true });
  }
});

test("catalog roots and cleanup share the catalog writer fingerprint", async () => {
  const fixture = await createFixture();
  try {
    const result = await fingerprints(fixture);

    assert.equal(
      result["waterMyPlants.libraries.androidx"],
      result["waterMyPlants.libraries.cleanup"]
    );
    assert.equal(WRITER_SCOPE_FINGERPRINT_SCHEMA_VERSION, 1);
  } finally {
    await rm(fixture.root, { recursive: true, force: true });
  }
});

test("unmapped shared writer changes invalidate every requested scope", async () => {
  const fixture = await createFixture();
  try {
    const before = await fingerprints(fixture);
    await writeFile(fixture.sharedSource, "export const shared = 2;\n", "utf8");
    const after = await fingerprints(fixture);

    for (const scope of [...REQUESTED_SCOPES, "metadata"]) {
      assert.notEqual(after[scope], before[scope], scope);
    }
  } finally {
    await rm(fixture.root, { recursive: true, force: true });
  }
});

test("official writer fingerprints ignore preview-only sources", async () => {
  const fixture = await createFixture();
  try {
    const before = await fingerprints(fixture);
    await writeFile(fixture.previewSource, "export const preview = 2;\n", "utf8");
    const after = await fingerprints(fixture);

    assert.deepEqual(after, before);
  } finally {
    await rm(fixture.root, { recursive: true, force: true });
  }
});

test("unknown policy targets fail closed during fingerprint generation", async () => {
  const fixture = await createFixture();
  try {
    await writeFile(
      fixture.policyPath,
      JSON.stringify({
        schemaVersion: 1,
        figmaVisualTargetRules: [{ paths: ["repo/tools/src/*"], targets: ["unknown"] }],
      }),
      "utf8"
    );

    await assert.rejects(() => fingerprints(fixture), /Unknown Figma writer target/);
  } finally {
    await rm(fixture.root, { recursive: true, force: true });
  }
});

test("repository policy maps catalog writer changes to catalog scopes and preflight", async () => {
  const root = await mkdtemp(join(tmpdir(), "figma-repository-writer-policy-"));
  const sourceRoot = join(root, "repo", "figma-design-sync", "tools", "src");
  const figmaRoot = join(sourceRoot, "figma");
  await mkdir(figmaRoot, { recursive: true });
  const catalogSource = join(figmaRoot, "figma-catalog-tree-sync-gateway.ts");
  await Promise.all([
    writeFile(catalogSource, "export const catalog = 1;\n", "utf8"),
    writeFile(join(figmaRoot, "figma-ci-documentation-sync-gateway.ts"), "export const ci = 1;\n", "utf8"),
    writeFile(join(figmaRoot, "figma-metadata-gateway.ts"), "export const metadata = 1;\n", "utf8"),
    writeFile(join(figmaRoot, "figma-node-gateway.ts"), "export const shared = 1;\n", "utf8"),
  ]);
  const options = {
    sourceRoot,
    repositoryRoot: root,
    policyPath: join(
      process.cwd(),
      "..",
      "..",
      "..",
      CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY
    ),
    scopes: REQUESTED_SCOPES,
  };

  try {
    const before = await createWriterScopeFingerprints(options);
    await writeFile(catalogSource, "export const catalog = 2;\n", "utf8");
    const after = await createWriterScopeFingerprints(options);

    assert.notEqual(after.preflight, before.preflight);
    assert.notEqual(
      after["waterMyPlants.libraries.androidx"],
      before["waterMyPlants.libraries.androidx"]
    );
    assert.notEqual(
      after["waterMyPlants.libraries.cleanup"],
      before["waterMyPlants.libraries.cleanup"]
    );
    assert.equal(after.versions, before.versions);
    assert.equal(after["ci.overview"], before["ci.overview"]);
    assert.equal(after.metadata, before.metadata);
  } finally {
    await rm(root, { recursive: true, force: true });
  }
});

async function createFixture() {
  const root = await mkdtemp(join(tmpdir(), "figma-writer-fingerprints-"));
  const sourceRoot = join(root, "repo", "tools", "src");
  const figmaRoot = join(sourceRoot, "figma");
  await mkdir(figmaRoot, { recursive: true });
  const ciSource = join(figmaRoot, "figma-ci-gateway.ts");
  const catalogSource = join(figmaRoot, "figma-catalog-gateway.ts");
  const metadataSource = join(figmaRoot, "figma-metadata-gateway.ts");
  const sharedSource = join(figmaRoot, "figma-node-gateway.ts");
  const appRoot = join(sourceRoot, "app");
  await mkdir(appRoot, { recursive: true });
  const previewSource = join(appRoot, "sync-catalog-tree-preview.mcp.ts");
  await Promise.all([
    writeFile(ciSource, "export const ci = 1;\n", "utf8"),
    writeFile(catalogSource, "export const catalog = 1;\n", "utf8"),
    writeFile(metadataSource, "export const metadata = 1;\n", "utf8"),
    writeFile(sharedSource, "export const shared = 1;\n", "utf8"),
    writeFile(previewSource, "export const preview = 1;\n", "utf8"),
  ]);
  const policyPath = join(root, "policy.json");
  await writeFile(
    policyPath,
    JSON.stringify({
      schemaVersion: 1,
      figmaTransportOnlyPaths: ["repo/tools/src/app/sync-catalog-tree-preview.mcp.ts"],
      figmaVisualTargetRules: [
        {
          paths: ["repo/tools/src/figma/figma-ci-*"],
          targets: [
            "ci.overview",
            "ci.pullRequestIntegration",
            "ci.postMergeDesignDocumentation",
            "ci.infrastructureAndAccess",
            "ci.windowsRuntime",
          ],
        },
        {
          paths: ["repo/tools/src/figma/figma-catalog-*"],
          targets: ["preflight", "waterMyPlants.libraries"],
        },
        {
          paths: ["repo/tools/src/figma/figma-metadata-*"],
          targets: ["metadata"],
        },
      ],
    }),
    "utf8"
  );
  return {
    root,
    sourceRoot,
    policyPath,
    ciSource,
    catalogSource,
    metadataSource,
    sharedSource,
    previewSource,
  };
}

function fingerprints(fixture) {
  return createWriterScopeFingerprints({
    sourceRoot: fixture.sourceRoot,
    repositoryRoot: fixture.root,
    policyPath: fixture.policyPath,
    scopes: REQUESTED_SCOPES,
  });
}
