import type { DesignModel } from "../design-model";

export const CI_VISUAL_TARGET_NAMES = [
  "ci.overview",
  "ci.pullRequestIntegration",
  "ci.postMergeDesignDocumentation",
  "ci.infrastructureAndAccess",
] as const;

export type CiVisualTargetName = typeof CI_VISUAL_TARGET_NAMES[number];
export type CiVisualNodeType =
  | "actor"
  | "system"
  | "git reference"
  | "pipeline"
  | "job"
  | "artifact"
  | "check"
  | "gate";

export type CiVisualNode = {
  id: string;
  type: CiVisualNodeType;
  name: string;
  description: string;
  steps?: string;
  source: string;
  sourceUrl: string;
  row: number;
  column: number;
};

export type CiVisualConnection = {
  id: string;
  source: string;
  target: string;
  label: string;
};

export type CiVisualSection = {
  target: CiVisualTargetName;
  name: string;
  description: string;
  headerSources: Array<{ label: string; url: string }>;
  nodes: CiVisualNode[];
  connections: CiVisualConnection[];
};

export type CiVisualPlan = {
  parentName: "Continuous Integration and Design Documentation";
  sections: CiVisualSection[];
};

const GITHUB_MAIN_BLOB_URL = "https://github.com/marmatsan/water-my-plants/blob/main";
const TEAMCITY_SOURCE = ".teamcity/settings.kts";
const TOPOLOGY_SOURCE = "docs/ci/external-topology.yaml";
const VISUAL_CONTRACT_SOURCE = "docs/ci/visual-model-contract.md";
const BRANCH_PROTECTION_SOURCE = "docs/ci/main-branch-protection.md";
const OFFICIAL_SYNC_SOURCE = "repo/figma-design-sync/docs/runbooks/official-artifact-visual-sync.md";

export function createCiVisualPlan(designModel: DesignModel): CiVisualPlan {
  const ci = requireCiContent(designModel);
  const ciPipeline = requirePipeline(ci.teamCity, "CI");
  const figmaPipeline = requirePipeline(ci.teamCity, "Figma Sync");

  return {
    parentName: "Continuous Integration and Design Documentation",
    sections: [
      createOverviewSection(ciPipeline, figmaPipeline),
      createPullRequestSection(ciPipeline),
      createPostMergeSection(ci, figmaPipeline),
      createInfrastructureSection(ci),
    ],
  };
}

export function requireCiContent(designModel: DesignModel) {
  const ci = designModel.content?.ci;
  if (!ci || typeof ci !== "object") {
    throw new Error("designModel.content.ci is required for CI visual sync.");
  }
  if (!ci.externalTopology || !Array.isArray(ci.externalTopology.nodes) || !Array.isArray(ci.externalTopology.connections)) {
    throw new Error("designModel.content.ci.externalTopology must contain nodes and connections.");
  }
  if (!ci.teamCity || !Array.isArray(ci.teamCity.pipelines) || !Array.isArray(ci.teamCity.vcsRoots)) {
    throw new Error("designModel.content.ci.teamCity must contain pipelines and vcsRoots.");
  }
  return ci;
}

function createOverviewSection(ciPipeline, figmaPipeline): CiVisualSection {
  const ciCheck = publishedChecks(ciPipeline)[0] || "TeamCity CI";
  const figmaCheck = publishedChecks(figmaPipeline)[0] || "TeamCity Figma Sync";
  const nodes: CiVisualNode[] = [
    visualNode("overview-pr", "git reference", "Pull Request", "Proposes a reviewed change to the repository.", BRANCH_PROTECTION_SOURCE, 0, 0),
    pipelineNode("overview-ci", ciPipeline, 1, 0),
    visualNode("overview-ci-check", "check", ciCheck, "Reports CI verification to GitHub.", TEAMCITY_SOURCE, 2, 0),
    visualNode("overview-gate", "gate", "Pull request merge gate", "Requires the CI check before merge.", BRANCH_PROTECTION_SOURCE, 3, 0),
    visualNode("overview-main", "git reference", "main", "Stable trunk after the reviewed merge.", BRANCH_PROTECTION_SOURCE, 4, 0),
    pipelineNode("overview-figma", figmaPipeline, 5, 0),
    visualNode("overview-model", "artifact", "design-model.json", "Carries the repository documentation snapshot.", TEAMCITY_SOURCE, 6, 0),
    visualNode("overview-figma-check", "check", figmaCheck, "Reports whether Figma metadata matches main.", TEAMCITY_SOURCE, 7, 0),
  ];

  return section(
    "ci.overview",
    "Overview",
    "Simplified pull request and post-merge design documentation journeys.",
    [VISUAL_CONTRACT_SOURCE],
    nodes,
    [
      connection("overview-pr-trigger", "overview-pr", "overview-ci", triggerLabel(ciPipeline)),
      connection("overview-ci-check", "overview-ci", "overview-ci-check", "Publish check"),
      connection("overview-check-gate", "overview-ci-check", "overview-gate", "Required check"),
      connection("overview-gate-main", "overview-gate", "overview-main", "Merge"),
      connection("overview-main-figma", "overview-main", "overview-figma", triggerLabel(figmaPipeline)),
      connection("overview-figma-model", "overview-figma", "overview-model", "Generate and publish"),
      connection("overview-model-check", "overview-model", "overview-figma-check", "Verify model hash"),
    ]
  );
}

function createPullRequestSection(ciPipeline): CiVisualSection {
  const nodes: CiVisualNode[] = [
    visualNode("pr", "git reference", "Pull Request", "Contains the branch revision proposed for main.", BRANCH_PROTECTION_SOURCE, 0, 0),
    pipelineNode(`pipeline-${ciPipeline.id}`, ciPipeline, 1, 0),
    ...ciPipeline.jobs.map((job, index) => jobNode(`job-${job.id}`, job, 2 + index, 0)),
  ];

  const lastJob = ciPipeline.jobs.at(-1);
  for (const [index, checkName] of publishedChecks(ciPipeline).entries()) {
    nodes.push(visualNode(`check-${index}`, "check", checkName, "Publishes the CI result to GitHub.", TEAMCITY_SOURCE, 2 + ciPipeline.jobs.length, index));
  }
  nodes.push(visualNode("merge-gate", "gate", "Pull request merge gate", "Requires TeamCity CI before merging.", BRANCH_PROTECTION_SOURCE, 3 + ciPipeline.jobs.length, 0));
  nodes.push(visualNode("main", "git reference", "main", "Receives the reviewed change after the gate passes.", BRANCH_PROTECTION_SOURCE, 4 + ciPipeline.jobs.length, 0));

  const connections: CiVisualConnection[] = [
    connection("pr-trigger", "pr", `pipeline-${ciPipeline.id}`, triggerLabel(ciPipeline)),
    ...ciPipeline.jobs.map((job, index) => connection(
      `pipeline-job-${job.id}`,
      index === 0 ? `pipeline-${ciPipeline.id}` : `job-${ciPipeline.jobs[index - 1].id}`,
      `job-${job.id}`,
      index === 0 ? "Run pipeline" : "Continue"
    )),
    ...publishedChecks(ciPipeline).map((_, index) => connection(
      `job-check-${index}`,
      lastJob ? `job-${lastJob.id}` : `pipeline-${ciPipeline.id}`,
      `check-${index}`,
      "Publish check"
    )),
    ...publishedChecks(ciPipeline).map((_, index) => connection(`check-gate-${index}`, `check-${index}`, "merge-gate", "Required check")),
    connection("gate-main", "merge-gate", "main", "Merge"),
  ];

  return section(
    "ci.pullRequestIntegration",
    "Pull Request Integration",
    "Detailed merge-gate flow derived from the effective CI pipeline.",
    [TEAMCITY_SOURCE, BRANCH_PROTECTION_SOURCE],
    nodes,
    connections
  );
}

function createPostMergeSection(ci, figmaPipeline): CiVisualSection {
  const externalById = new Map(ci.externalTopology.nodes.map((node) => [node.id, node]));
  const operator = externalById.get("operator");
  const codex = externalById.get("codex-mcp-client");
  const figmaDocument = externalById.get("figma-design-document");
  const nodes: CiVisualNode[] = [
    visualNode("main", "git reference", "main", "Starts documentation verification after successful CI.", TEAMCITY_SOURCE, 0, 0),
    pipelineNode(`pipeline-${figmaPipeline.id}`, figmaPipeline, 1, 0),
    ...figmaPipeline.jobs.map((job, index) => jobNode(`job-${job.id}`, job, 2 + index * 2, 0)),
  ];
  const artifactJob = figmaPipeline.jobs.find((job) => job.artifacts?.some((artifact) => artifact.path.endsWith("design-model.json")));
  const artifactRow = artifactJob ? 3 : 2;
  nodes.push(visualNode("design-model", "artifact", "design-model.json", "Official repository snapshot consumed by visual synchronization.", TEAMCITY_SOURCE, artifactRow, 0));
  const checkRow = 2 + figmaPipeline.jobs.length * 2;
  for (const [index, checkName] of publishedChecks(figmaPipeline).entries()) {
    nodes.push(visualNode(`figma-check-${index}`, "check", checkName, "Publishes the post-merge documentation result.", TEAMCITY_SOURCE, checkRow, index));
  }
  if (operator) nodes.push(externalNode("operator", operator, checkRow + 1, 0));
  if (codex) nodes.push(externalNode("codex", codex, checkRow + 2, 0));
  if (figmaDocument) nodes.push(externalNode("figma-document", figmaDocument, checkRow + 3, 0));

  const generateJob = figmaPipeline.jobs[0];
  const checkJob = figmaPipeline.jobs[1];
  const connections: CiVisualConnection[] = [
    connection("main-trigger", "main", `pipeline-${figmaPipeline.id}`, triggerLabel(figmaPipeline)),
    ...(generateJob ? [connection("pipeline-generate", `pipeline-${figmaPipeline.id}`, `job-${generateJob.id}`, "Run pipeline")] : []),
    ...(artifactJob ? [connection("generate-artifact", `job-${artifactJob.id}`, "design-model", "Publish artifact")] : []),
    ...(checkJob ? [connection("artifact-check", "design-model", `job-${checkJob.id}`, "Consume artifact")] : []),
    ...publishedChecks(figmaPipeline).map((_, index) => connection(
      `check-status-${index}`,
      checkJob ? `job-${checkJob.id}` : `pipeline-${figmaPipeline.id}`,
      `figma-check-${index}`,
      "Publish status"
    )),
    ...(operator && publishedChecks(figmaPipeline).length > 0 ? [connection("mismatch-operator", "figma-check-0", "operator", "Mismatch requires action")] : []),
    ...(operator && codex ? [connection("operator-codex", "operator", "codex", "Request visual synchronization")] : []),
    ...(codex && figmaDocument ? [connection("codex-figma", "codex", "figma-document", "Apply visual changes")] : []),
    ...(figmaDocument && checkJob ? [connection("rerun", "figma-document", `job-${checkJob.id}`, "Manual rerun")] : []),
  ];

  return section(
    "ci.postMergeDesignDocumentation",
    "Post-merge Design Documentation",
    "Official model generation, visual synchronization, and verification loop.",
    [TEAMCITY_SOURCE, OFFICIAL_SYNC_SOURCE],
    nodes,
    connections
  );
}

function createInfrastructureSection(ci): CiVisualSection {
  const placement = infrastructurePlacement();
  const nodes = ci.externalTopology.nodes.map((node, index) => {
    const position = placement[node.id] || { row: index, column: 0 };
    return externalNode(`external-${node.id}`, node, position.row, position.column);
  });
  const nodeIds = new Map(ci.externalTopology.nodes.map((node) => [node.id, `external-${node.id}`]));
  const connections = ci.externalTopology.connections.map((edge) => connection(
    `external-${edge.id}`,
    nodeIds.get(edge.source),
    nodeIds.get(edge.target),
    edge.label
  ));

  return section(
    "ci.infrastructureAndAccess",
    "Infrastructure and Access",
    "External systems, trust boundaries, authentication paths, and automation modes.",
    [TOPOLOGY_SOURCE, "docs/ci/external-topology-validation.md"],
    nodes,
    connections
  );
}

function section(target, name, description, sources, nodes, connections): CiVisualSection {
  return {
    target,
    name,
    description,
    headerSources: sources.map((source) => ({ label: source, url: sourceUrl(source) })),
    nodes,
    connections,
  };
}

function pipelineNode(id, pipeline, row, column): CiVisualNode {
  return visualNode(id, "pipeline", pipeline.name, pipelineDescription(pipeline.name), TEAMCITY_SOURCE, row, column);
}

function jobNode(id, job, row, column): CiVisualNode {
  return {
    ...visualNode(id, "job", job.name, jobDescription(job.name), TEAMCITY_SOURCE, row, column),
    steps: job.steps?.map((step) => summarizeCommand(step.command, step.name)).join("\n") || undefined,
  };
}

function externalNode(id, node, row, column): CiVisualNode {
  return visualNode(id, node.type, node.name, node.description, TOPOLOGY_SOURCE, row, column);
}

function visualNode(id, type, name, description, source, row, column): CiVisualNode {
  return { id, type, name, description, source, sourceUrl: sourceUrl(source), row, column };
}

function connection(id, source, target, label): CiVisualConnection {
  if (!source || !target) throw new Error(`CI visual connection '${id}' has an unknown endpoint.`);
  return { id, source, target, label };
}

function publishedChecks(pipeline): string[] {
  return pipeline.jobs.flatMap((job) => job.publishedChecks || []).map((check) => check.name);
}

function requirePipeline(teamCity, name) {
  const pipeline = teamCity.pipelines.find((candidate) => candidate.name === name);
  if (!pipeline) throw new Error(`Effective TeamCity configuration is missing pipeline '${name}'.`);
  if (!Array.isArray(pipeline.jobs) || !Array.isArray(pipeline.triggers)) {
    throw new Error(`Effective TeamCity pipeline '${name}' has an invalid shape.`);
  }
  return pipeline;
}

function triggerLabel(pipeline): string {
  const trigger = pipeline.triggers[0];
  if (!trigger) return "Run manually";
  if (trigger.type === "vcs") return `VCS trigger ${trigger.branchFilter || ""}`.trim();
  if (trigger.type === "pipeline finish") return `After ${trigger.dependencyPipelineId || "pipeline"} succeeds`;
  return trigger.type;
}

function summarizeCommand(command: string, fallback: string): string {
  if (command.includes("teamcity-configs:generate")) return "Generate effective TeamCity configuration";
  if (command.includes("generateFigmaDesignModel")) return "Generate design model";
  if (command.includes("checkFigmaTrunkSync")) return "Check Figma trunk sync";
  if (/gradlew(?:\.bat)?\s+check(?:\s|$)/i.test(command)) return "Gradle check";
  return fallback;
}

function pipelineDescription(name: string): string {
  return name === "CI"
    ? "Validates pull requests and branch revisions before merge."
    : "Verifies post-merge Figma documentation against main.";
}

function jobDescription(name: string): string {
  if (name === "Verify") return "Runs repository verification and publishes the required CI check.";
  if (name === "Generate main design model") return "Builds and publishes the official design-model.json artifact.";
  if (name === "Check Figma trunk sync") return "Compares current Figma metadata with the official model hash.";
  return "Executes an effective TeamCity pipeline job.";
}

function sourceUrl(source: string): string {
  return `${GITHUB_MAIN_BLOB_URL}/${source}`;
}

function infrastructurePlacement() {
  return {
    browser: { row: 0, column: 0 },
    "teamcity-cli": { row: 0, column: 1 },
    "github-app": { row: 0, column: 2 },
    operator: { row: 0, column: 3 },
    "cloudflare-access": { row: 1, column: 1 },
    "cloudflare-tunnel": { row: 2, column: 1 },
    "teamcity-server": { row: 3, column: 1 },
    "github-repository": { row: 3, column: 2 },
    "build-agent": { row: 4, column: 1 },
    "codex-mcp-client": { row: 4, column: 3 },
    "figma-api": { row: 5, column: 2 },
    "figma-design-document": { row: 6, column: 2 },
  };
}
