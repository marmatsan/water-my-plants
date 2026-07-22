export type CiVisualTargetName = string;
export type CiVisualOrientation = "horizontal" | "grid";
export type CiVisualNodeType =
  | "actor"
  | "system"
  | "git reference"
  | "pipeline"
  | "job"
  | "artifact"
  | "check"
  | "gate";
export type CiVisualEnvironment =
  | "github"
  | "teamcity"
  | "cloudflare"
  | "figma"
  | "codex"
  | "browser"
  | "terminal"
  | "operator"
  | "json"
  | "gradle";

export type CiVisualRuntime = {
  platform: string;
  service: string;
  startup: string;
  identity: string;
};

export type CiVisualStepRole = "action" | "decision" | "group";

export type CiVisualStep = {
  order: string;
  role: CiVisualStepRole;
  title: string;
  technicalId?: string;
  description?: string;
  condition?: string;
};

export type CiVisualPhase = {
  order: string;
  title: string;
  technicalId?: string;
  description?: string;
  steps: CiVisualStep[];
};

export type CiVisualOutcomeKind =
  | "artifact"
  | "check"
  | "success"
  | "action";

export type CiVisualOutcome = {
  order: string;
  kind: CiVisualOutcomeKind;
  title: string;
  technicalId?: string;
  description?: string;
  condition?: string;
};

export type CiVisualNode = {
  id: string;
  type: CiVisualNodeType;
  environment: CiVisualEnvironment;
  name: string;
  description: string;
  phases: CiVisualPhase[];
  outcomes: CiVisualOutcome[];
  runtime?: CiVisualRuntime;
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
  orientation: CiVisualOrientation;
  headerSources: Array<{ label: string; url: string }>;
  nodes: CiVisualNode[];
  connections: CiVisualConnection[];
};

export type CiVisualPlan = {
  schemaVersion: 3;
  parentName: "Continuous Integration and Design Documentation";
  sections: CiVisualSection[];
};
