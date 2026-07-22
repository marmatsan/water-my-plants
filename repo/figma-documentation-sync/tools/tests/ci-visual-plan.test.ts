import assert from "node:assert/strict";
import test from "node:test";
import type { CiVisualNode } from "../src/domain/ci/ci-visual-plan";
import {
  appendConnectorLabelLayers,
  centeredRowY,
  ciConnectorMagnets,
  ciNodePropertyValues,
  ciOutcomePropertyValues,
  ciParentResizeDimensions,
  ciPhasePropertyValues,
  ciStepPropertyValues,
  ciVisualGridPosition,
  connectorBoundsLabelPosition,
  connectorLabelLayers,
  connectorLabelPosition,
  horizontalFlowPositions,
  horizontalConnectorGap,
  managedCiRemovalPriority,
  waitForStableCiLayout,
} from "../src/figma/figma-ci-documentation-sync-gateway";
import {
  ciStepSlotName,
  ciStepSlotNames,
} from "../src/figma/ci-step-slot-contract";
import {
  ciPhaseSlotName,
  ciPhaseSlotNames,
} from "../src/figma/ci-phase-slot-contract";
import {
  ciOutcomeSlotName,
  ciOutcomeSlotNames,
} from "../src/figma/ci-outcome-slot-contract";

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

test("CI node properties hide runtime when the visual node has no runtime data", () => {
  const node = visualNode();

  assert.deepEqual(ciNodePropertyValues(node), {
    name: "Pull Request",
    description: "Proposes a reviewed change to the repository.",
    executionPlanHeading: "Execution plan",
    source: "docs/ci/main-branch-protection.md",
    runtimePlatform: "",
    runtimeService: "",
    runtimeStartup: "",
    runtimeIdentity: "",
    showExecutionPlan: false,
    showSource: true,
    showRuntime: false,
    showOptionalDetails: true,
  });
});

test("CI node properties expose complete runtime data", () => {
  const node = {
    ...visualNode(),
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
    executionPlanHeading: "Execution plan",
    source: "docs/ci/main-branch-protection.md",
    runtimePlatform: "Windows",
    runtimeService: "TeamCity",
    runtimeStartup: "Automatic",
    runtimeIdentity: "NT SERVICE\\TeamCity",
    showExecutionPlan: false,
    showSource: true,
    showRuntime: true,
    showOptionalDetails: true,
  });
});

test("CI hierarchy reserves stable phase, step, and outcome slot names", () => {
  assert.equal(ciPhaseSlotNames().length, 8);
  assert.equal(ciPhaseSlotName(1), "phase 01");
  assert.equal(ciPhaseSlotName(8), "phase 08");
  assert.throws(() => ciPhaseSlotName(9), /between 1 and 8/);
  assert.equal(ciStepSlotNames().length, 8);
  assert.equal(ciStepSlotName(1), "step 01");
  assert.equal(ciStepSlotName(8), "step 08");
  assert.throws(() => ciStepSlotName(9), /between 1 and 8/);
  assert.equal(ciOutcomeSlotNames().length, 4);
  assert.equal(ciOutcomeSlotName(1), "outcome 01");
  assert.equal(ciOutcomeSlotName(4), "outcome 04");
  assert.throws(() => ciOutcomeSlotName(5), /between 1 and 4/);
});

test("CI node properties reveal the execution plan when phases exist", () => {
  const node = {
    ...visualNode(),
    phases: [{
      order: "01",
      title: "Run TeamCity phase",
      steps: [],
    }],
  };

  assert.equal(ciNodePropertyValues(node).showExecutionPlan, true);
});

test("CI phase properties expose its executable identity", () => {
  assert.deepEqual(
    ciPhasePropertyValues({
      order: "02",
      title: "Run planned Gradle checks",
      technicalId: "RUNNER_3",
      description: "Runs the Gradle entry points owned by this TeamCity phase.",
      steps: [],
    }),
    {
      order: "02",
      title: "Run planned Gradle checks",
      technicalId: "RUNNER_3",
      description: "Runs the Gradle entry points owned by this TeamCity phase.",
      showTechnicalId: true,
      showDescription: true,
      showSteps: false,
    }
  );
});

test("CI phase properties reveal its steps when executable detail exists", () => {
  const properties = ciPhasePropertyValues({
    order: "02",
    title: "Run planned Gradle checks",
    steps: [{
      order: "02.1",
      role: "action",
      title: "Run repository task",
      technicalId: "check",
    }],
  });

  assert.equal(properties.showSteps, true);
});

test("CI step properties expose a decision with its executable condition", () => {
  assert.deepEqual(
    ciStepPropertyValues({
      order: "02.1",
      role: "decision",
      title: "Select affected verification tasks",
      technicalId: "ci.plan.gradleTasks",
      description: "Limits verification to the affected Gradle scopes.",
      condition: "Affected scopes are derived from the comparison base.",
    }),
    {
      order: "02.1",
      role: "decision",
      title: "Select affected verification tasks",
      technicalId: "ci.plan.gradleTasks",
      tasks: "",
      description: "Limits verification to the affected Gradle scopes.",
      condition: "Affected scopes are derived from the comparison base.",
      showTechnicalId: true,
      showDescription: true,
      showCondition: true,
    }
  );
});

test("CI group properties render exact task identifiers as a multiline bullet list", () => {
  assert.deepEqual(
    ciStepPropertyValues({
      order: "02.2",
      role: "group",
      title: "Always",
      technicalId: "checkGitWorkflow · checkDocumentation",
      description: "Runs the checks required for every change.",
      condition: "Always",
    }),
    {
      order: "02.2",
      role: "group",
      title: "Always",
      technicalId: "checkGitWorkflow · checkDocumentation",
      tasks: "• checkGitWorkflow\n• checkDocumentation",
      description: "Runs the checks required for every change.",
      condition: "Always",
      showTechnicalId: true,
      showDescription: true,
      showCondition: true,
    }
  );
});

test("CI outcome properties expose a published check", () => {
  assert.deepEqual(
    ciOutcomePropertyValues({
      order: "04",
      kind: "check",
      title: "Publish GitHub check",
      technicalId: "TeamCity CI",
      description: "Reports the verified job result to the pull request.",
      condition: "After successful job execution",
    }),
    {
      order: "04",
      kind: "check",
      title: "Publish GitHub check",
      technicalId: "TeamCity CI",
      description: "Reports the verified job result to the pull request.",
      condition: "After successful job execution",
      showTechnicalId: true,
      showDescription: true,
      showCondition: true,
    }
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

test("CI parent resize follows child sections instead of a stale header width", () => {
  assert.deepEqual(
    ciParentResizeDimensions([
      { type: "INSTANCE", name: ".Header", x: 0, y: 0, width: 18518, height: 407 },
      { type: "SECTION", x: 100, y: 521, width: 3747, height: 412 },
      { type: "SECTION", x: 100, y: 4737, width: 4375, height: 2418 },
      { type: "SECTION", x: 100, y: 9835, width: 1480, height: 617 },
    ]),
    { width: 4575, height: 10552 }
  );
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

function visualNode(): CiVisualNode {
  return {
    id: "overview-pr",
    type: "git reference",
    environment: "github",
    name: "Pull Request",
    description: "Proposes a reviewed change to the repository.",
    source: "docs/ci/main-branch-protection.md",
    sourceUrl: "https://example.test/docs/ci/main-branch-protection.md",
    phases: [],
    outcomes: [],
    row: 0,
    column: 0,
  };
}
