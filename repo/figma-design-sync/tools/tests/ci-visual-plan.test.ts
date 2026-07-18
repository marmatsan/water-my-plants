import assert from "node:assert/strict";
import test from "node:test";
import {
  artifactPathContains,
  createCiVisualPlan,
  externalEnvironment,
  windowsRuntimeEnvironment,
} from "../src/domain/ci/create-ci-visual-plan";
import {
  appendConnectorLabelLayers,
  centeredRowY,
  ciConnectorMagnets,
  ciNodePropertyValues,
  ciVisualGridPosition,
  connectorBoundsLabelPosition,
  connectorLabelLayers,
  connectorLabelPosition,
  horizontalFlowPositions,
  horizontalConnectorGap,
  managedCiRemovalPriority,
  waitForStableCiLayout,
} from "../src/figma/figma-ci-documentation-sync-gateway";

test("CI visual plan creates the five documented granular sections", () => {
  const plan = createCiVisualPlan(designModel());

  assert.equal(plan.parentName, "Continuous Integration and Design Documentation");
  assert.deepEqual(
    plan.sections.map((section) => section.target),
    [
      "ci.overview",
      "ci.pullRequestIntegration",
      "ci.postMergeDesignDocumentation",
      "ci.infrastructureAndAccess",
      "ci.windowsRuntime",
    ]
  );
  assert.deepEqual(
    plan.sections.map((section) => section.orientation),
    ["horizontal", "horizontal", "horizontal", "grid", "grid"]
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
  assert.ok(postMerge.connections.some((edge) => edge.label === "Rerun via HTTPS client"));
  assert.deepEqual(
    postMerge.connections
      .filter((edge) => ["pipeline-generate", "generate-artifact", "artifact-check"].includes(edge.id))
      .map(({ source, target }) => ({ source, target })),
    [
      { source: "pipeline-Root_FigmaSync", target: "job-generate" },
      { source: "job-generate", target: "design-model" },
      { source: "design-model", target: "job-check" },
    ]
  );

  const connectedNodeIds = new Set(
    postMerge.connections.flatMap(({ source, target }) => [source, target])
  );
  assert.deepEqual(
    postMerge.nodes.filter(({ id }) => !connectedNodeIds.has(id)).map(({ name }) => name),
    []
  );
});

test("CI visual plan resolves the design model inside TeamCity artifact directories", () => {
  assert.equal(
    artifactPathContains(
      "build/reports/figma-sync",
      "build/reports/figma-sync/design-model.json"
    ),
    true
  );
  assert.equal(
    artifactPathContains(
      "build\\reports\\figma-sync\\** => figma-sync",
      "build/reports/figma-sync/design-model.json"
    ),
    true
  );
  assert.equal(
    artifactPathContains(
      "build/reports/unrelated",
      "build/reports/figma-sync/design-model.json"
    ),
    false
  );
});

test("CI connector label text stays above its opaque background", () => {
  const background = { name: "Background" };
  const text = { name: "Label" };
  const appended: Array<{ name: string }> = [];

  assert.deepEqual(
    appendConnectorLabelLayers(
      { appendChild: (node) => appended.push(node) },
      background,
      text
    ).map((node) => node.name),
    ["Background", "Label"]
  );
  assert.deepEqual(appended.map((node) => node.name), ["Background", "Label"]);
  assert.deepEqual(connectorLabelLayers(background, text), appended);
});

test("CI visual plan maps node ownership to explicit icon environments", () => {
  const plan = createCiVisualPlan(designModel());
  const overview = plan.sections.find((section) => section.target === "ci.overview")!;

  assert.deepEqual(
    overview.nodes.map(({ name, environment }) => ({ name, environment })),
    [
      { name: "Pull Request", environment: "github" },
      { name: "CI", environment: "teamcity" },
      { name: "TeamCity CI", environment: "teamcity" },
      { name: "Pull request merge gate", environment: "github" },
      { name: "main", environment: "github" },
      { name: "Figma Sync", environment: "teamcity" },
      { name: "design-model.json", environment: "json" },
      { name: "TeamCity Figma Sync", environment: "teamcity" },
    ]
  );
});

test("CI node properties hide runtime when the visual node has no runtime data", () => {
  const node = createCiVisualPlan(designModel()).sections[0].nodes[0];

  assert.deepEqual(ciNodePropertyValues(node), {
    name: "Pull Request",
    description: "Proposes a reviewed change to the repository.",
    steps: "",
    source: "docs/ci/main-branch-protection.md",
    runtimePlatform: "",
    runtimeService: "",
    runtimeStartup: "",
    runtimeIdentity: "",
    showSteps: false,
    showSource: true,
    showRuntime: false,
  });
});

test("CI node properties expose complete runtime data", () => {
  const node = {
    ...createCiVisualPlan(designModel()).sections[0].nodes[0],
    runtime: {
      platform: "Windows",
      service: "TeamCity",
      startup: "Automatic",
      identity: "NT SERVICE\\TeamCity",
    },
  };

  assert.deepEqual(ciNodePropertyValues(node), {
    name: "Pull Request",
    description: "Proposes a reviewed change to the repository.",
    steps: "",
    source: "docs/ci/main-branch-protection.md",
    runtimePlatform: "Windows",
    runtimeService: "TeamCity",
    runtimeStartup: "Automatic",
    runtimeIdentity: "NT SERVICE\\TeamCity",
    showSteps: false,
    showSource: true,
    showRuntime: true,
  });
});

test("external CI nodes require an explicit icon environment mapping", () => {
  assert.equal(externalEnvironment("operator"), "operator");
  assert.equal(externalEnvironment("browser"), "browser");
  assert.equal(externalEnvironment("teamcity-cli"), "terminal");
  assert.equal(externalEnvironment("github-app"), "github");
  assert.equal(externalEnvironment("cloudflare-tunnel"), "cloudflare");
  assert.equal(externalEnvironment("build-agent"), "teamcity");
  assert.equal(externalEnvironment("codex-mcp-client"), "codex");
  assert.equal(externalEnvironment("figma-api"), "figma");
  assert.throws(() => externalEnvironment("unknown-system"), /no \.ci icon environment mapping/);
});

test("Windows runtime services require an explicit icon environment mapping", () => {
  assert.equal(windowsRuntimeEnvironment("teamcity-server"), "teamcity");
  assert.equal(windowsRuntimeEnvironment("build-agent"), "teamcity");
  assert.equal(windowsRuntimeEnvironment("cloudflare-tunnel"), "cloudflare");
  assert.throws(() => windowsRuntimeEnvironment("unknown-service"), /no \.ci icon environment mapping/);
});

test("CI visual plan maps Windows service data to runtime node properties", () => {
  const runtime = createCiVisualPlan(designModel()).sections
    .find((section) => section.target === "ci.windowsRuntime")!;

  assert.equal(runtime.name, "Windows Service Runtime");
  assert.deepEqual(runtime.connections, []);
  assert.deepEqual(
    runtime.nodes.map((node) => ({
      name: node.name,
      environment: node.environment,
      runtime: node.runtime,
    })),
    [
      {
        name: "TeamCity Server",
        environment: "teamcity",
        runtime: {
          platform: "Windows",
          service: "TeamCity",
          startup: "Automatic",
          identity: "NT SERVICE\\TeamCity",
        },
      },
      {
        name: "Build Agent",
        environment: "teamcity",
        runtime: {
          platform: "Windows",
          service: "TCBuildAgent",
          startup: "Automatic",
          identity: "NT SERVICE\\TCBuildAgent",
        },
      },
      {
        name: "Cloudflare Tunnel",
        environment: "cloudflare",
        runtime: {
          platform: "Windows",
          service: "Cloudflared",
          startup: "Automatic",
          identity: "LocalSystem",
        },
      },
    ]
  );
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

test("CI connector labels stay horizontal and centered in a vertical gap", () => {
  assert.deepEqual(
    connectorLabelPosition(
      { x: 100, y: 100, width: 300, height: 100 },
      { x: 200, y: 400, width: 300, height: 100 },
      120,
      40
    ),
    { x: 240, y: 280 }
  );
});

test("CI connector labels stay centered in a horizontal gap", () => {
  assert.deepEqual(
    connectorLabelPosition(
      { x: 100, y: 100, width: 200, height: 100 },
      { x: 500, y: 120, width: 200, height: 100 },
      100,
      40
    ),
    { x: 350, y: 140 }
  );
});

test("CI horizontal sections transpose logical rows into visual columns", () => {
  assert.deepEqual(ciVisualGridPosition({ row: 3, column: 1 }, "horizontal"), {
    row: 1,
    column: 3,
  });
  assert.deepEqual(ciVisualGridPosition({ row: 3, column: 1 }, "grid"), {
    row: 3,
    column: 1,
  });
});

test("CI connectors use horizontal anchors only in horizontal flows", () => {
  const left = { x: 100, y: 100, width: 200, height: 100 };
  const right = { x: 400, y: 100, width: 200, height: 100 };
  assert.deepEqual(ciConnectorMagnets(left, right, "horizontal"), {
    start: "RIGHT",
    end: "LEFT",
  });
  assert.deepEqual(ciConnectorMagnets(right, left, "horizontal"), {
    start: "BOTTOM",
    end: "BOTTOM",
  });
  assert.deepEqual(ciConnectorMagnets(left, right, "grid"), {
    start: "RIGHT",
    end: "LEFT",
  });
  assert.deepEqual(ciConnectorMagnets(left, right, "grid", { index: 0, count: 2 }), {
    start: "TOP",
    end: "TOP",
  });
  assert.deepEqual(ciConnectorMagnets(left, right, "grid", { index: 1, count: 2 }), {
    start: "BOTTOM",
    end: "BOTTOM",
  });
  assert.deepEqual(
    ciConnectorMagnets(
      { x: 100, y: 100, width: 200, height: 100 },
      { x: 100, y: 400, width: 200, height: 100 },
      "grid",
      { index: 1, count: 2 }
    ),
    { start: "LEFT", end: "LEFT" }
  );
});

test("CI horizontal layouts stack disconnected flows and left-align each row", () => {
  assert.deepEqual(
    [...horizontalFlowPositions(
      [
        { id: "first-a", row: 4 },
        { id: "first-b", row: 8 },
        { id: "second-a", row: 10 },
        { id: "second-b", row: 12 },
      ],
      [
        { source: "first-a", target: "first-b" },
        { source: "second-a", target: "second-b" },
      ]
    )],
    [
      ["first-a", { row: 0, column: 0 }],
      ["first-b", { row: 0, column: 1 }],
      ["second-a", { row: 1, column: 0 }],
      ["second-b", { row: 1, column: 1 }],
    ]
  );
});

test("CI horizontal rows align node centers and reserve label width", () => {
  assert.equal(centeredRowY(100, 200, 120), 140);
  assert.equal(horizontalConnectorGap(80), 160);
  assert.equal(horizontalConnectorGap(280), 328);
});

test("CI layout waits for hidden component blocks to collapse before positioning", async () => {
  const nodes = [
    { x: 0, y: 0, width: 577, height: 283 },
    { x: 0, y: 0, width: 763, height: 283 },
  ];
  let yields = 0;

  await waitForStableCiLayout(nodes, async () => {
    yields += 1;
    if (yields === 1) nodes[0].height = 225;
  });

  assert.equal(yields, 2);
  assert.equal(nodes[0].height, 225);
  assert.equal(centeredRowY(100, 283, nodes[0].height), 129);
  assert.equal(centeredRowY(100, 283, nodes[1].height), 100);
});

test("CI labels use stable connector bounds after endpoint geometry updates", async () => {
  const connectors = [{ x: 680.5, y: 241.5, width: 182, height: 0 }];
  let yields = 0;

  await waitForStableCiLayout(connectors, async () => {
    yields += 1;
    if (yields === 1) connectors[0].y = 256;
  });

  assert.equal(yields, 2);
  assert.deepEqual(
    connectorBoundsLabelPosition(connectors[0], { x: 0, y: 0 }, 141, 40),
    { x: 701, y: 236 }
  );
});

test("CI connector labels move outside nodes when the direct gap is too narrow", () => {
  assert.deepEqual(
    connectorLabelPosition(
      { x: 100, y: 100, width: 200, height: 100 },
      { x: 400, y: 100, width: 200, height: 100 },
      180,
      40,
      [
        { x: 100, y: 100, width: 200, height: 100 },
        { x: 400, y: 100, width: 200, height: 100 },
      ]
    ),
    { x: 260, y: 36 }
  );
});

test("CI parallel connector labels follow their distinct outside routes", () => {
  const source = { x: 100, y: 100, width: 200, height: 100 };
  const target = { x: 400, y: 100, width: 200, height: 100 };
  const obstacles = [source, target];
  assert.deepEqual(
    connectorLabelPosition(
      source,
      target,
      180,
      40,
      obstacles,
      [],
      { start: "TOP", end: "TOP" }
    ),
    { x: 260, y: 36 }
  );
  assert.deepEqual(
    connectorLabelPosition(
      source,
      target,
      180,
      40,
      obstacles,
      [],
      { start: "BOTTOM", end: "BOTTOM" }
    ),
    { x: 260, y: 224 }
  );
});

test("CI vertical return labels are centered on their connector bounds", () => {
  assert.deepEqual(
    connectorBoundsLabelPosition(
      { x: 1036.5, y: 3047.5, width: 55.5, height: 279 },
      { x: 100, y: 2035 },
      232,
      40
    ),
    { x: 848.25, y: 1132 }
  );
});

test("managed CI content removes connectors before their endpoint nodes", () => {
  const roles = ["node", "connector", "unmanaged", "connector", "node"];

  assert.deepEqual(
    roles.toSorted((first, second) =>
      managedCiRemovalPriority(first) - managedCiRemovalPriority(second)
    ),
    ["connector", "connector", "node", "node", "unmanaged"]
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
        windowsRuntime: {
          platform: "Windows",
          services: [
            {
              id: "teamcity-server",
              name: "TeamCity Server",
              description: "Hosts TeamCity.",
              service: "TeamCity",
              startup: "Automatic",
              identity: "NT SERVICE\\TeamCity",
            },
            {
              id: "build-agent",
              name: "Build Agent",
              description: "Runs builds.",
              service: "TCBuildAgent",
              startup: "Automatic",
              identity: "NT SERVICE\\TCBuildAgent",
            },
            {
              id: "cloudflare-tunnel",
              name: "Cloudflare Tunnel",
              description: "Publishes TeamCity through Cloudflare Tunnel.",
              service: "Cloudflared",
              startup: "Automatic",
              identity: "LocalSystem",
            },
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
                  id: "check",
                  name: "Check Figma trunk sync",
                  steps: [{ id: "RUNNER_1", name: "Check", command: ".\\gradlew.bat checkFigmaTrunkSync" }],
                  artifacts: [],
                  dependencies: [{ jobId: "generate", artifactPaths: ["build/reports/figma-sync"] }],
                  publishedChecks: [{ name: "TeamCity Figma Sync" }],
                },
                {
                  id: "generate",
                  name: "Generate main design model",
                  steps: [
                    { id: "RUNNER_1", name: "Generate config", command: ".\\mvnw.cmd teamcity-configs:generate" },
                    { id: "RUNNER_2", name: "Generate model", command: ".\\gradlew.bat generateFigmaDesignModel" },
                  ],
                  artifacts: [{ path: "build/reports/figma-sync" }],
                  dependencies: [],
                  publishedChecks: [],
                },
              ],
            },
          ],
        },
      },
    },
  };
}
