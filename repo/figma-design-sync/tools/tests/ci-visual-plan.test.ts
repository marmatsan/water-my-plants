import assert from "node:assert/strict";
import test from "node:test";
import { createCiVisualPlan } from "../src/domain/ci/create-ci-visual-plan";

test("CI visual plan creates the four documented granular sections", () => {
  const plan = createCiVisualPlan(designModel());

  assert.equal(plan.parentName, "Continuous Integration and Design Documentation");
  assert.deepEqual(
    plan.sections.map((section) => section.target),
    [
      "ci.overview",
      "ci.pullRequestIntegration",
      "ci.postMergeDesignDocumentation",
      "ci.infrastructureAndAccess",
    ]
  );
});

test("CI visual plan summarizes commands and keeps exact operational names", () => {
  const plan = createCiVisualPlan(designModel());
  const postMerge = plan.sections.find((section) => section.target === "ci.postMergeDesignDocumentation")!;
  const generateJob = postMerge.nodes.find((node) => node.name === "Generate main design model")!;

  assert.equal(
    generateJob.steps,
    "Generate effective TeamCity configuration\nGenerate design model"
  );
  assert.ok(postMerge.nodes.some((node) => node.name === "design-model.json" && node.type === "artifact"));
  assert.ok(postMerge.connections.some((edge) => edge.label === "Manual rerun"));
});

test("CI overview labels trigger, check, gate, merge, and model verification connections", () => {
  const overview = createCiVisualPlan(designModel()).sections
    .find((section) => section.target === "ci.overview")!;

  assert.deepEqual(
    overview.connections.map((connection) => connection.label),
    [
      "VCS trigger +:*",
      "Publish check",
      "Required check",
      "Merge",
      "After Root_Ci succeeds",
      "Generate and publish",
      "Verify model hash",
    ]
  );
});

test("CI visual plan renders external topology as infrastructure nodes and directed edges", () => {
  const infrastructure = createCiVisualPlan(designModel()).sections
    .find((section) => section.target === "ci.infrastructureAndAccess")!;

  assert.deepEqual(
    infrastructure.nodes.map((node) => node.name),
    ["Operator", "Cloudflare Access", "TeamCity Server", "Codex/MCP Client", "Figma Design Document"]
  );
  assert.deepEqual(infrastructure.connections[0], {
    id: "external-operator-access",
    source: "external-operator",
    target: "external-cloudflare-access",
    label: "Open TeamCity",
  });
});

test("CI visual plan rejects models without CI content", () => {
  assert.throws(
    () => createCiVisualPlan({ branch: "main", content: {} }),
    /designModel\.content\.ci is required/
  );
});

function designModel() {
  return {
    branch: "main",
    content: {
      ci: {
        externalTopology: {
          nodes: [
            { id: "operator", type: "actor", name: "Operator", description: "Starts manual actions." },
            { id: "cloudflare-access", type: "system", name: "Cloudflare Access", description: "Applies access policy." },
            { id: "teamcity-server", type: "system", name: "TeamCity Server", description: "Orchestrates pipelines." },
            { id: "codex-mcp-client", type: "system", name: "Codex/MCP Client", description: "Applies visual changes." },
            { id: "figma-design-document", type: "system", name: "Figma Design Document", description: "Stores visual documentation." },
          ],
          connections: [
            { id: "operator-access", source: "operator", target: "cloudflare-access", label: "Open TeamCity" },
          ],
        },
        teamCity: {
          vcsRoots: [],
          pipelines: [
            {
              id: "Root_Ci",
              name: "CI",
              triggers: [{ type: "vcs", branchFilter: "+:*" }],
              jobs: [
                {
                  id: "verify",
                  name: "Verify",
                  steps: [{ id: "RUNNER_1", name: "Run Gradle check", command: ".\\gradlew.bat check" }],
                  artifacts: [],
                  publishedChecks: [{ name: "TeamCity CI" }],
                },
              ],
            },
            {
              id: "Root_FigmaSync",
              name: "Figma Sync",
              triggers: [{ type: "pipeline finish", dependencyPipelineId: "Root_Ci" }],
              jobs: [
                {
                  id: "generate",
                  name: "Generate main design model",
                  steps: [
                    { id: "RUNNER_1", name: "Generate config", command: ".\\mvnw.cmd teamcity-configs:generate" },
                    { id: "RUNNER_2", name: "Generate model", command: ".\\gradlew.bat generateFigmaDesignModel" },
                  ],
                  artifacts: [{ path: "build/reports/figma-sync/design-model.json" }],
                  publishedChecks: [],
                },
                {
                  id: "check",
                  name: "Check Figma trunk sync",
                  steps: [{ id: "RUNNER_1", name: "Check", command: ".\\gradlew.bat checkFigmaTrunkSync" }],
                  artifacts: [],
                  publishedChecks: [{ name: "TeamCity Figma Sync" }],
                },
              ],
            },
          ],
        },
      },
    },
  };
}
