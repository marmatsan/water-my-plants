import {
  CI_CONNECTOR_NAME,
  CI_CONNECTOR_TEMPLATE_NAME,
  CI_CONNECTOR_TEMPLATE_NODE_ID,
  CI_DOCUMENTATION_PAGE_ID,
  CI_ICON_ENVIRONMENT_PROPERTY,
  CI_ICON_INSTANCE_NAME,
  CI_NODE_COMPONENT_ID,
  CI_NODE_INSTANCE_NAME,
  CI_NODE_PROPS,
  CI_OUTCOME_COMPONENT_SET_ID,
  CI_OUTCOME_PROPS,
  CI_OUTCOME_SLOT_COUNT,
  CI_PHASE_COMPONENT_ID,
  CI_PHASE_PROPS,
  CI_PHASE_SLOT_COUNT,
  CI_STEP_COMPONENT_SET_ID,
  CI_STEP_SLOT_COUNT,
  CI_STEP_PROPS,
  CI_VISUAL_TARGET_NAMES,
  CI_VARIABLE_COLLECTION_NAME,
  GITHUB_MAIN_BLOB_URL,
  HEADER_INSTANCE_NAME,
  HEADER_SECTION_TARGETS,
  METADATA_NAMESPACE,
} from "@figma-documentation-sync/project-config";
import {
  type CiVisualConnection,
  type CiVisualNode,
  type CiVisualOutcome,
  type CiVisualOrientation,
  type CiVisualPhase,
  type CiVisualPlan,
  type CiVisualSection,
  type CiVisualStep,
} from "../domain/ci/ci-visual-plan";
import type { CiDocumentationSyncGateway } from "../ports/sync-gateways";
import {
  applySectionStrokeContractTree,
  requireComponent,
  requireConnector,
  requireModeId,
  requireOutlineColorVariable,
  requirePage,
  requireSection,
  requireVariableCollection,
} from "./figma-node-gateway";
import { loadTextNodeFonts } from "./figma-text-gateway";
import {
  ciStepSlotNames,
  hasCiStepSlotNamePrefix,
} from "./ci-step-slot-contract";
import {
  ciPhaseSlotNames,
  hasCiPhaseSlotNamePrefix,
} from "./ci-phase-slot-contract";
import {
  ciOutcomeSlotNames,
  hasCiOutcomeSlotNamePrefix,
} from "./ci-outcome-slot-contract";

const SECTION_PADDING = 100;
const SECTION_GAP = 114;
const NODE_COLUMN_GAP = 160;
const NODE_ROW_GAP = 128;
const PARENT_CORNER_RADIUS = 28;
const CI_ROLE_KEY = "ciDocumentationRole";
const CI_TARGET_KEY = "ciDocumentationTarget";
const CI_MODEL_ID_KEY = "ciDocumentationModelId";
const ROLE_PARENT = "parent";
const ROLE_SECTION = "section";
const ROLE_NODE = "node";
const ROLE_CONNECTOR = "connector";
const LIGHT_MODE_NAME = "Light";
const CONNECTOR_LABEL_CLEARANCE = 24;
const CI_CONNECTOR_FALLBACK_FONT = { family: "Inter", style: "Medium" } as const;

export class FigmaCiDocumentationSyncGateway implements CiDocumentationSyncGateway {
  async syncCiDocumentation(
    targetNames: string[],
    plan: CiVisualPlan
  ) {
    const unknownTargets = targetNames.filter((target) => !CI_VISUAL_TARGET_NAMES.includes(target as any));
    if (unknownTargets.length > 0) {
      throw new Error(`Unknown CI documentation target(s): ${unknownTargets.join(", ")}.`);
    }

    const page = await requirePage(CI_DOCUMENTATION_PAGE_ID);
    const nodeComponent = await requireComponent(CI_NODE_COMPONENT_ID);
    const modeCollection = await requireVariableCollection(CI_VARIABLE_COLLECTION_NAME);
    const outlineVariable = await requireOutlineColorVariable();
    const surfaceVariable = await requireColorVariable("md/sys/color/surface");
    const surfaceCollection = await requireVariableCollectionById(surfaceVariable.variableCollectionId);
    const surfaceModeId = requireModeId(surfaceCollection, LIGHT_MODE_NAME);
    const mutatedNodeIds: string[] = [];
    const createdCiNodes: string[] = [];
    const updatedCiSteps: string[] = [];
    const createdCiConnectors: string[] = [];
    const updatedCiSections: string[] = [];
    const parent = await requireOrCreateParentSection(
      page,
      plan.parentName,
      surfaceVariable,
      surfaceCollection,
      surfaceModeId,
      mutatedNodeIds
    );
    unlockTree(parent, mutatedNodeIds);
    await syncParentHeader(parent, mutatedNodeIds);

    const requestedTargets = new Set(targetNames);
    for (const sectionPlan of plan.sections) {
      if (!requestedTargets.has(sectionPlan.target)) continue;

      const section = requireOrCreateChildSection(parent, sectionPlan, mutatedNodeIds);
      clearManagedSectionContent(section, mutatedNodeIds);
      await syncSectionContent(
        section,
        sectionPlan,
        nodeComponent,
        modeCollection,
        outlineVariable,
        createdCiNodes,
        updatedCiSteps,
        createdCiConnectors,
        mutatedNodeIds
      );
      updatedCiSections.push(section.id);
    }

    layoutChildSections(parent, mutatedNodeIds);
    resizeParent(parent, mutatedNodeIds);
    lockOnlyParent(parent, mutatedNodeIds);

    return {
      updatedCiSections,
      createdCiNodes,
      updatedCiSteps,
      createdCiConnectors,
      mutatedNodeIds: [...new Set(mutatedNodeIds)],
    };
  }
}

async function requireOrCreateParentSection(
  page,
  name,
  surfaceVariable,
  surfaceCollection,
  surfaceModeId,
  mutatedNodeIds
) {
  const existing = page.children.find((child) =>
    child.type === "SECTION" &&
      (
        child.getSharedPluginData(METADATA_NAMESPACE, CI_ROLE_KEY) === ROLE_PARENT ||
        child.name === name
      )
  );
  const parent = existing?.type === "SECTION" ? existing : figma.createSection();

  if (!existing) {
    page.appendChild(parent);
    parent.x = 0;
    parent.y = 0;
  }
  parent.name = name;
  parent.cornerRadius = PARENT_CORNER_RADIUS;
  parent.strokes = [];
  parent.setExplicitVariableModeForCollection(surfaceCollection, surfaceModeId);
  parent.fills = [boundColorPaint(surfaceVariable, resolveColorForConsumer(surfaceVariable, parent))];
  parent.setSharedPluginData(METADATA_NAMESPACE, CI_ROLE_KEY, ROLE_PARENT);
  mutatedNodeIds.push(parent.id);
  return parent;
}

async function syncParentHeader(parent, mutatedNodeIds) {
  let header = parent.children.find((child) => child.type === "INSTANCE" && child.name === HEADER_INSTANCE_NAME);
  if (!header || header.type !== "INSTANCE") {
    const templateSection = await requireSection(HEADER_SECTION_TARGETS[0].sectionNodeId);
    const templateInstance = templateSection.children.find(
      (child) => child.type === "INSTANCE" && child.name === HEADER_INSTANCE_NAME
    );
    if (!templateInstance || templateInstance.type !== "INSTANCE") {
      throw new Error(`Could not resolve '${HEADER_INSTANCE_NAME}' from section '${templateSection.id}'.`);
    }
    const mainComponent = await templateInstance.getMainComponentAsync();
    if (!mainComponent) throw new Error(`Header instance '${templateInstance.id}' has no main component.`);
    header = mainComponent.createInstance();
    header.name = HEADER_INSTANCE_NAME;
    parent.appendChild(header);
  }

  const links = [
    sourceLink("docs/ci/visual-model-contract.md"),
    sourceLink(".teamcity/settings.kts"),
    sourceLink("docs/ci/external-topology.yaml"),
    sourceLink("docs/ci/windows-runtime.yaml"),
  ];
  setComponentTextProperty(header, "Header", "Continuous Integration and Design Documentation");
  setComponentTextProperty(
    header,
    "Definition",
    "Current pull request integration, post-merge design documentation, CI infrastructure, and Windows service runtime."
  );
  const linkText = links.map((link) => link.label).join("\n");
  setComponentTextProperty(header, "Link", linkText);
  await applyTextLinks(header, "Link", links, linkText);
  header.x = 0;
  header.y = 0;
  mutatedNodeIds.push(header.id);
}

function requireOrCreateChildSection(parent, plan: CiVisualSection, mutatedNodeIds) {
  const existing = parent.children.find((child) =>
    child.type === "SECTION" &&
      child.getSharedPluginData(METADATA_NAMESPACE, CI_TARGET_KEY) === plan.target
  );
  const section = existing?.type === "SECTION" ? existing : figma.createSection();

  if (!existing) parent.appendChild(section);
  section.name = plan.name;
  section.cornerRadius = 0;
  section.fills = [];
  section.setSharedPluginData(METADATA_NAMESPACE, CI_ROLE_KEY, ROLE_SECTION);
  section.setSharedPluginData(METADATA_NAMESPACE, CI_TARGET_KEY, plan.target);
  mutatedNodeIds.push(section.id);
  return section;
}

function clearManagedSectionContent(section, mutatedNodeIds) {
  const managedChildren = [...section.children]
    .filter((child) => !child.removed)
    .map((child) => ({
      child,
      role: child.getSharedPluginData?.(METADATA_NAMESPACE, CI_ROLE_KEY),
    }))
    .filter(({ role }) => role === ROLE_NODE || role === ROLE_CONNECTOR)
    .sort((first, second) => managedCiRemovalPriority(first.role) - managedCiRemovalPriority(second.role));

  for (const { child } of managedChildren) {
    // Removing an endpoint may remove its native connector as a side effect.
    if (child.removed) continue;
    mutatedNodeIds.push(child.id);
    child.remove();
  }
}

export function managedCiRemovalPriority(role: string) {
  if (role === ROLE_CONNECTOR) return 0;
  if (role === ROLE_NODE) return 1;
  return 2;
}

async function syncSectionContent(
  section,
  plan: CiVisualSection,
  nodeComponent,
  modeCollection,
  outlineVariable,
  createdCiNodes,
  updatedCiSteps,
  createdCiConnectors,
  mutatedNodeIds
) {
  const groupsByModelId = new Map<string, GroupNode>();
  const groups: Array<{ plan: CiVisualNode; group: GroupNode }> = [];
  const connectorLabelMetricsByModelId = new Map<string, { width: number }>();
  const connectorTemplate = plan.connections.length > 0
    ? await requireCiConnectorTemplate()
    : null;

  for (const nodePlan of plan.nodes) {
    const instance = nodeComponent.createInstance();
    instance.name = CI_NODE_INSTANCE_NAME;
    section.appendChild(instance);
    await syncCiNode(instance, nodePlan, modeCollection);
    const execution = await syncCiExecution({
      node: instance,
      phases: nodePlan.phases,
      outcomes: nodePlan.outcomes,
    });
    const group = figma.group([instance], section);
    group.name = ".ci node group";
    group.setSharedPluginData(METADATA_NAMESPACE, CI_ROLE_KEY, ROLE_NODE);
    group.setSharedPluginData(METADATA_NAMESPACE, CI_MODEL_ID_KEY, nodePlan.id);
    groupsByModelId.set(nodePlan.id, group);
    groups.push({ plan: nodePlan, group });
    createdCiNodes.push(instance.id);
    updatedCiSteps.push(...execution.steps.map((stepInstance) => stepInstance.id));
    mutatedNodeIds.push(
      group.id,
      instance.id,
      ...execution.phases.map((phaseInstance) => phaseInstance.id),
      ...execution.steps.map((stepInstance) => stepInstance.id),
      ...execution.outcomes.map((outcomeInstance) => outcomeInstance.id)
    );
  }

  const connectorRecords: Array<{
    edge: CiVisualConnection;
    source: GroupNode;
    target: GroupNode;
    connector: ConnectorNode;
  }> = [];

  for (const edge of plan.connections) {
    const source = groupsByModelId.get(edge.source);
    const target = groupsByModelId.get(edge.target);
    if (!source || !target) {
      throw new Error(`CI section '${plan.target}' connection '${edge.id}' references an unknown node.`);
    }
    if (!connectorTemplate) {
      throw new Error(`CI section '${plan.target}' requires a connector template.`);
    }
    const connector = connectorTemplate.clone();
    connector.name = CI_CONNECTOR_NAME;
    connector.connectorLineType = "ELBOWED";
    connector.connectorStartStrokeCap = "NONE";
    connector.connectorEndStrokeCap = "ARROW_LINES";
    connector.strokes = [boundColorPaint(outlineVariable)];
    connector.strokeWeight = 2;
    connector.setSharedPluginData(METADATA_NAMESPACE, CI_ROLE_KEY, ROLE_CONNECTOR);
    connector.setSharedPluginData(METADATA_NAMESPACE, CI_MODEL_ID_KEY, edge.id);
    section.insertChild(0, connector);
    await setNativeConnectorLabel(
      connector,
      edge.label
    );
    connectorLabelMetricsByModelId.set(edge.id, { width: connector.text.width });
    connectorRecords.push({ edge, source, target, connector });
    createdCiConnectors.push(connector.id);
    mutatedNodeIds.push(connector.id);
  }

  await waitForStableCiLayout(groups.map(({ group }) => group));
  layoutNodeGroups(
    groups,
    plan.orientation,
    plan.connections,
    connectorLabelMetricsByModelId
  );
  await waitForStableCiLayout(groups.map(({ group }) => group));
  const parallelConnections = parallelConnectionInfo(plan.connections);
  for (const { edge, source, target, connector } of connectorRecords) {
    const magnets = ciConnectorMagnets(
      source,
      target,
      plan.orientation,
      parallelConnections.get(edge.id)
    );
    connector.connectorStart = { endpointNodeId: source.id, magnet: magnets.start };
    connector.connectorEnd = { endpointNodeId: target.id, magnet: magnets.end };
  }
  await waitForStableCiLayout(connectorRecords.map(({ connector }) => connector));

  resizeChildSection(
    section,
    [...groups.map((item) => item.group), ...connectorRecords.map(({ connector }) => connector)],
    mutatedNodeIds
  );
  applySectionStrokeContractTree(section, outlineVariable, mutatedNodeIds);
}

async function requireCiConnectorTemplate(): Promise<ConnectorNode> {
  const connector = await requireConnector(CI_CONNECTOR_TEMPLATE_NODE_ID);
  if (connector.name !== CI_CONNECTOR_TEMPLATE_NAME) {
    throw new Error(
      `CI connector template '${connector.id}' must be named '${CI_CONNECTOR_TEMPLATE_NAME}'; ` +
        `found '${connector.name}'.`
    );
  }
  return connector;
}

async function syncCiNode(instance, nodePlan: CiVisualNode, modeCollection) {
  const modeId = requireModeId(modeCollection, modeName(nodePlan.type));
  instance.setExplicitVariableModeForCollection(modeCollection, modeId);
  const icon = requireSingleNestedInstance(instance, CI_ICON_INSTANCE_NAME);
  setComponentVariantProperty(icon, CI_ICON_ENVIRONMENT_PROPERTY, nodePlan.environment);
  const properties = ciNodePropertyValues(nodePlan);
  setComponentBooleanProperty(
    instance,
    CI_NODE_PROPS.showExecutionPlan,
    properties.showExecutionPlan
  );
  setComponentBooleanProperty(instance, CI_NODE_PROPS.showOutcome, properties.showOutcome);
  setComponentBooleanProperty(instance, CI_NODE_PROPS.showSource, properties.showSource);
  setComponentBooleanProperty(instance, CI_NODE_PROPS.showRuntime, properties.showRuntime);
  setComponentBooleanProperty(
    instance,
    CI_NODE_PROPS.showOptionalDetails,
    properties.showOptionalDetails
  );
  setComponentTextProperty(instance, CI_NODE_PROPS.name, properties.name);
  setComponentTextProperty(instance, CI_NODE_PROPS.description, properties.description);
  setComponentTextProperty(
    instance,
    CI_NODE_PROPS.executionPlanHeading,
    properties.executionPlanHeading
  );
  setComponentTextProperty(instance, CI_NODE_PROPS.source, properties.source);
  setComponentTextProperty(instance, CI_NODE_PROPS.runtimePlatform, properties.runtimePlatform);
  setComponentTextProperty(instance, CI_NODE_PROPS.runtimeService, properties.runtimeService);
  setComponentTextProperty(instance, CI_NODE_PROPS.runtimeStartup, properties.runtimeStartup);
  setComponentTextProperty(instance, CI_NODE_PROPS.runtimeIdentity, properties.runtimeIdentity);
  await applyTextLinks(
    instance,
    "File",
    [{ label: nodePlan.source, url: nodePlan.sourceUrl }],
    nodePlan.source
  );
}

export function ciNodePropertyValues(nodePlan: CiVisualNode) {
  const showSource = Boolean(nodePlan.source);
  const showRuntime = Boolean(nodePlan.runtime);
  return {
    name: nodePlan.name,
    description: nodePlan.description,
    executionPlanHeading: "Execution plan",
    source: nodePlan.source,
    runtimePlatform: nodePlan.runtime?.platform || "",
    runtimeService: nodePlan.runtime?.service || "",
    runtimeStartup: nodePlan.runtime?.startup || "",
    runtimeIdentity: nodePlan.runtime?.identity || "",
    showExecutionPlan: nodePlan.phases.length > 0,
    showOutcome: nodePlan.outcomes.length > 0,
    showSource,
    showRuntime,
    showOptionalDetails: showSource || showRuntime,
  };
}

async function syncCiExecution({
  node,
  phases,
  outcomes,
}: {
  node: InstanceNode;
  phases: CiVisualPhase[];
  outcomes: CiVisualOutcome[];
}) {
  if (phases.length > CI_PHASE_SLOT_COUNT) {
    throw new Error(
      `CI node '${node.name}' requires ${phases.length} phases but .ci node reserves ` +
        `only ${CI_PHASE_SLOT_COUNT} slots.`
    );
  }
  if (outcomes.length > CI_OUTCOME_SLOT_COUNT) {
    throw new Error(
      `CI node '${node.name}' requires ${outcomes.length} outcomes but .ci node reserves ` +
        `only ${CI_OUTCOME_SLOT_COUNT} slots.`
    );
  }

  const phaseSlots = await requireCiPhaseSlots(node);
  const outcomeSlots = await requireCiOutcomeSlots(node);
  const updatedPhases: InstanceNode[] = [];
  const updatedSteps: InstanceNode[] = [];
  const updatedOutcomes: InstanceNode[] = [];

  for (const [index, phaseInstance] of phaseSlots.entries()) {
    const phase = phases[index];
    if (!phase) {
      phaseInstance.visible = false;
      continue;
    }
    if (phase.steps.length > CI_STEP_SLOT_COUNT) {
      throw new Error(
        `CI phase '${phase.title}' requires ${phase.steps.length} steps but .ci phase reserves ` +
          `only ${CI_STEP_SLOT_COUNT} slots.`
      );
    }
    syncCiPhase({
      instance: phaseInstance,
      phase,
    });
    phaseInstance.visible = true;
    updatedPhases.push(phaseInstance);
    updatedSteps.push(...await syncCiStepSlots({
      phase: phaseInstance,
      steps: phase.steps,
    }));
  }

  for (const [index, outcomeInstance] of outcomeSlots.entries()) {
    const outcome = outcomes[index];
    if (!outcome) {
      outcomeInstance.visible = false;
      continue;
    }
    syncCiOutcome({
      instance: outcomeInstance,
      outcome,
    });
    outcomeInstance.visible = true;
    updatedOutcomes.push(outcomeInstance);
  }

  return {
    phases: updatedPhases,
    steps: updatedSteps,
    outcomes: updatedOutcomes,
  };
}

async function requireCiPhaseSlots(node: InstanceNode): Promise<InstanceNode[]> {
  const slots = requireNamedExposedSlots({
    owner: node,
    expectedNames: ciPhaseSlotNames(),
    expectedCount: CI_PHASE_SLOT_COUNT,
    hasNamePrefix: hasCiPhaseSlotNamePrefix,
    slotLabel: "CI phase",
  });
  for (const slot of slots) {
    const mainComponent = await slot.getMainComponentAsync();
    if (!mainComponent || mainComponent.id !== CI_PHASE_COMPONENT_ID) {
      throw new Error(
        `CI phase slot '${slot.name}' in .ci node '${node.id}' must use component ` +
          `'${CI_PHASE_COMPONENT_ID}'.`
      );
    }
  }
  return slots;
}

async function requireCiOutcomeSlots(node: InstanceNode): Promise<InstanceNode[]> {
  const slots = requireNamedExposedSlots({
    owner: node,
    expectedNames: ciOutcomeSlotNames(),
    expectedCount: CI_OUTCOME_SLOT_COUNT,
    hasNamePrefix: hasCiOutcomeSlotNamePrefix,
    slotLabel: "CI outcome",
  });
  for (const slot of slots) {
    const mainComponent = await slot.getMainComponentAsync();
    const componentSet = mainComponent?.parent;
    if (componentSet?.type !== "COMPONENT_SET" || componentSet.id !== CI_OUTCOME_COMPONENT_SET_ID) {
      throw new Error(
        `CI outcome slot '${slot.name}' in .ci node '${node.id}' must use component set ` +
          `'${CI_OUTCOME_COMPONENT_SET_ID}'.`
      );
    }
  }
  return slots;
}

async function syncCiStepSlots({
  phase,
  steps,
}: {
  phase: InstanceNode;
  steps: CiVisualStep[];
}): Promise<InstanceNode[]> {
  const slots = await requireCiStepSlots(phase);
  const updated: InstanceNode[] = [];
  for (const [index, instance] of slots.entries()) {
    const step = steps[index];
    if (!step) {
      instance.visible = false;
      continue;
    }
    syncCiStep({
      instance,
      step,
    });
    instance.visible = true;
    updated.push(instance);
  }
  return updated;
}

async function requireCiStepSlots(phase: InstanceNode): Promise<InstanceNode[]> {
  const slots = requireNamedExposedSlots({
    owner: phase,
    expectedNames: ciStepSlotNames(),
    expectedCount: CI_STEP_SLOT_COUNT,
    hasNamePrefix: hasCiStepSlotNamePrefix,
    slotLabel: "CI step",
  });
  for (const slot of slots) {
    const mainComponent = await slot.getMainComponentAsync();
    const componentSet = mainComponent?.parent;
    if (componentSet?.type !== "COMPONENT_SET" || componentSet.id !== CI_STEP_COMPONENT_SET_ID) {
      throw new Error(
        `CI step slot '${slot.name}' in .ci phase '${phase.id}' must use component set ` +
          `'${CI_STEP_COMPONENT_SET_ID}'.`
      );
    }
  }
  return slots;
}

function requireNamedExposedSlots({
  owner,
  expectedNames,
  expectedCount,
  hasNamePrefix,
  slotLabel,
}: {
  owner: InstanceNode;
  expectedNames: string[];
  expectedCount: number;
  hasNamePrefix: (name: string) => boolean;
  slotLabel: string;
}): InstanceNode[] {
  const candidates = owner.exposedInstances.filter((instance) => hasNamePrefix(instance.name));
  const duplicateNames = expectedNames.filter(
    (name) => candidates.filter((candidate) => candidate.name === name).length > 1
  );
  const missingNames = expectedNames.filter(
    (name) => !candidates.some((candidate) => candidate.name === name)
  );
  if (candidates.length !== expectedCount || duplicateNames.length > 0 || missingNames.length > 0) {
    throw new Error(
      `${slotLabel} owner '${owner.id}' must expose exactly ${expectedCount} slots. ` +
        `Missing: ${missingNames.join(", ") || "none"}. ` +
        `Duplicated: ${duplicateNames.join(", ") || "none"}.`
    );
  }
  return expectedNames.map(
    (name) => candidates.find((candidate) => candidate.name === name)!
  );
}

function syncCiPhase({
  instance,
  phase,
}: {
  instance: InstanceNode;
  phase: CiVisualPhase;
}) {
  const properties = ciPhasePropertyValues(phase);
  setComponentBooleanProperty(instance, CI_PHASE_PROPS.showTechnicalId, properties.showTechnicalId);
  setComponentBooleanProperty(instance, CI_PHASE_PROPS.showDescription, properties.showDescription);
  setComponentBooleanProperty(instance, CI_PHASE_PROPS.showSteps, properties.showSteps);
  setComponentTextProperty(instance, CI_PHASE_PROPS.order, properties.order);
  setComponentTextProperty(instance, CI_PHASE_PROPS.title, properties.title);
  setComponentTextProperty(instance, CI_PHASE_PROPS.technicalId, properties.technicalId);
  setComponentTextProperty(instance, CI_PHASE_PROPS.description, properties.description);
}

export function ciPhasePropertyValues(phase: CiVisualPhase) {
  return {
    order: phase.order,
    title: phase.title,
    technicalId: phase.technicalId || "",
    description: phase.description || "",
    showTechnicalId: Boolean(phase.technicalId),
    showDescription: Boolean(phase.description),
    showSteps: phase.steps.length > 0,
  };
}

function syncCiStep({
  instance,
  step,
}: {
  instance: InstanceNode;
  step: CiVisualStep;
}) {
  const properties = ciStepPropertyValues(step);
  setComponentVariantProperty(instance, CI_STEP_PROPS.role, properties.role);
  setComponentBooleanProperty(instance, CI_STEP_PROPS.showTechnicalId, properties.showTechnicalId);
  setComponentBooleanProperty(instance, CI_STEP_PROPS.showDescription, properties.showDescription);
  setComponentBooleanProperty(instance, CI_STEP_PROPS.showCondition, properties.showCondition);
  setComponentTextProperty(instance, CI_STEP_PROPS.order, properties.order);
  setComponentTextProperty(instance, CI_STEP_PROPS.title, properties.title);
  setComponentTextProperty(instance, CI_STEP_PROPS.technicalId, properties.technicalId);
  setComponentTextProperty(instance, CI_STEP_PROPS.tasks, properties.tasks);
  setComponentTextProperty(instance, CI_STEP_PROPS.description, properties.description);
  setComponentTextProperty(instance, CI_STEP_PROPS.condition, properties.condition);
}

export function ciStepPropertyValues(step: CiVisualStep) {
  return {
    order: step.order,
    role: step.role,
    title: step.title,
    technicalId: step.technicalId || "",
    tasks: ciGroupTasksValue(step),
    description: step.description || "",
    condition: step.condition || "",
    showTechnicalId: Boolean(step.technicalId),
    showDescription: Boolean(step.description) && step.role !== "action",
    showCondition: Boolean(step.condition),
  };
}

function ciGroupTasksValue(step: CiVisualStep): string {
  if (step.role !== "group" || !step.technicalId) return "";
  return step.technicalId
    .split(/\s*·\s*|\r?\n/)
    .map((task) => task.replace(/^•\s*/, "").trim())
    .filter(Boolean)
    .map((task) => `• ${task}`)
    .join("\n");
}

function syncCiOutcome({
  instance,
  outcome,
}: {
  instance: InstanceNode;
  outcome: CiVisualOutcome;
}) {
  const properties = ciOutcomePropertyValues(outcome);
  setComponentVariantProperty(instance, CI_OUTCOME_PROPS.kind, properties.kind);
  setComponentBooleanProperty(instance, CI_OUTCOME_PROPS.showTechnicalId, properties.showTechnicalId);
  setComponentBooleanProperty(instance, CI_OUTCOME_PROPS.showDescription, properties.showDescription);
  setComponentBooleanProperty(instance, CI_OUTCOME_PROPS.showCondition, properties.showCondition);
  setComponentTextProperty(instance, CI_OUTCOME_PROPS.order, properties.order);
  setComponentTextProperty(instance, CI_OUTCOME_PROPS.title, properties.title);
  setComponentTextProperty(instance, CI_OUTCOME_PROPS.technicalId, properties.technicalId);
  setComponentTextProperty(instance, CI_OUTCOME_PROPS.description, properties.description);
  setComponentTextProperty(instance, CI_OUTCOME_PROPS.condition, properties.condition);
}

export function ciOutcomePropertyValues(outcome: CiVisualOutcome) {
  return {
    order: outcome.order,
    kind: outcome.kind,
    title: outcome.title,
    technicalId: outcome.technicalId || "",
    description: outcome.description || "",
    condition: outcome.condition || "",
    showTechnicalId: Boolean(outcome.technicalId),
    showDescription: Boolean(outcome.description),
    showCondition: Boolean(outcome.condition),
  };
}

function layoutNodeGroups(
  groups: Array<{ plan: CiVisualNode; group: GroupNode }>,
  orientation: CiVisualOrientation,
  connections: CiVisualConnection[],
  labelsByModelId: Map<string, { width: number }>
) {
  const items = groups.map(({ plan, group }) => ({
    plan,
    group,
    position: ciVisualGridPosition(plan, orientation),
  }));
  if (orientation === "horizontal") {
    const flowPositions = horizontalFlowPositions(groups.map(({ plan }) => plan), connections);
    for (const item of items) {
      item.position = flowPositions.get(item.plan.id) ?? item.position;
    }
  }
  const rows = [...new Set(items.map(({ position }) => position.row))].sort((a, b) => a - b);
  const columns = [...new Set(items.map(({ position }) => position.column))].sort((a, b) => a - b);
  const rowHeights = new Map(rows.map((row) => [
    row,
    Math.max(...items.filter(({ position }) => position.row === row).map(({ group }) => group.height)),
  ]));
  const columnWidths = new Map(columns.map((column) => [
    column,
    Math.max(...items.filter(({ position }) => position.column === column).map(({ group }) => group.width)),
  ]));
  const rowY = cumulativePositions(rows, rowHeights, NODE_ROW_GAP, SECTION_PADDING);
  const columnGaps = requiredHorizontalColumnGaps(
    items,
    columns,
    connections,
    labelsByModelId,
    orientation
  );
  const columnX = cumulativePositionsWithVariableGaps(
    columns,
    columnWidths,
    columnGaps,
    NODE_COLUMN_GAP,
    SECTION_PADDING
  );

  for (const { group, position } of items) {
    const columnWidth = columnWidths.get(position.column)!;
    const rowHeight = rowHeights.get(position.row)!;
    group.x = columnX.get(position.column)! + (columnWidth - group.width) / 2;
    group.y = centeredRowY(rowY.get(position.row)!, rowHeight, group.height);
  }
}

function requireSingleNestedInstance(root, instanceName) {
  const matches = root.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((candidate) => candidate.name === instanceName);
  if (matches.length !== 1) {
    throw new Error(
      `CI node '${root.id}' must contain exactly one '${instanceName}' nested instance; found ${matches.length}.`
    );
  }
  return matches[0];
}

function requiredHorizontalColumnGaps(
  items: Array<{
    plan: CiVisualNode;
    group: GroupNode;
    position: { row: number; column: number };
  }>,
  columns: number[],
  connections: CiVisualConnection[],
  labelsByModelId: Map<string, { width: number }>,
  orientation: CiVisualOrientation
): Map<number, number> {
  const gaps = new Map<number, number>();
  if (orientation !== "horizontal") return gaps;
  const itemByModelId = new Map(items.map((item) => [item.plan.id, item]));
  const columnIndex = new Map(columns.map((column, index) => [column, index]));

  for (const edge of connections) {
    const source = itemByModelId.get(edge.source);
    const target = itemByModelId.get(edge.target);
    const label = labelsByModelId.get(edge.id);
    if (!source || !target || !label || source.position.row !== target.position.row) continue;
    const sourceIndex = columnIndex.get(source.position.column);
    const targetIndex = columnIndex.get(target.position.column);
    if (sourceIndex === undefined || targetIndex === undefined || Math.abs(sourceIndex - targetIndex) !== 1) continue;
    const boundaryColumn = columns[Math.min(sourceIndex, targetIndex)];
    const requiredGap = horizontalConnectorGap(label.width);
    gaps.set(boundaryColumn, Math.max(gaps.get(boundaryColumn) || 0, requiredGap));
  }
  return gaps;
}

export function centeredRowY(rowTop: number, rowHeight: number, itemHeight: number) {
  return rowTop + (rowHeight - itemHeight) / 2;
}

export async function waitForStableCiLayout(
  nodes: Array<Pick<SceneNode, "x" | "y" | "width" | "height">>,
  yieldLayout: () => Promise<void> = yieldToFigmaLayout,
  maxAttempts = 5
) {
  let previous = ciLayoutSignature(nodes);
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    await yieldLayout();
    const current = ciLayoutSignature(nodes);
    if (current === previous) return;
    previous = current;
  }
  throw new Error(`CI layout did not stabilize after ${maxAttempts} attempts.`);
}

function ciLayoutSignature(nodes: Array<Pick<SceneNode, "x" | "y" | "width" | "height">>) {
  return nodes.map(({ x, y, width, height }) => `${x}:${y}:${width}:${height}`).join("|");
}

async function yieldToFigmaLayout() {
  await new Promise<void>((resolve) => setTimeout(resolve, 0));
}

export function horizontalConnectorGap(labelWidth: number) {
  return Math.max(NODE_COLUMN_GAP, labelWidth + CONNECTOR_LABEL_CLEARANCE * 2);
}

export function ciVisualGridPosition(
  plan: Pick<CiVisualNode, "row" | "column">,
  orientation: CiVisualOrientation
) {
  return orientation === "horizontal"
    ? { row: plan.column, column: plan.row }
    : { row: plan.row, column: plan.column };
}

export function horizontalFlowPositions(
  nodes: Array<Pick<CiVisualNode, "id" | "row">>,
  connections: Array<Pick<CiVisualConnection, "source" | "target">>
) {
  const byId = new Map(nodes.map((node) => [node.id, node]));
  const adjacent = new Map(nodes.map((node) => [node.id, new Set<string>()]));
  for (const edge of connections) {
    if (!byId.has(edge.source) || !byId.has(edge.target)) continue;
    adjacent.get(edge.source)!.add(edge.target);
    adjacent.get(edge.target)!.add(edge.source);
  }

  const components: Array<Array<Pick<CiVisualNode, "id" | "row">>> = [];
  const visited = new Set<string>();
  for (const node of [...nodes].sort((first, second) => first.row - second.row)) {
    if (visited.has(node.id)) continue;
    const component: Array<Pick<CiVisualNode, "id" | "row">> = [];
    const pending = [node.id];
    visited.add(node.id);
    while (pending.length > 0) {
      const id = pending.shift()!;
      component.push(byId.get(id)!);
      for (const neighbour of adjacent.get(id) || []) {
        if (visited.has(neighbour)) continue;
        visited.add(neighbour);
        pending.push(neighbour);
      }
    }
    components.push(component.sort((first, second) => first.row - second.row));
  }

  return new Map(components.flatMap((component, visualRow) =>
    component.map((node, visualColumn) => [node.id, { row: visualRow, column: visualColumn }] as const)
  ));
}

export function ciConnectorMagnets(
  source: Pick<SceneNode, "x" | "y" | "width" | "height">,
  target: Pick<SceneNode, "x" | "y" | "width" | "height">,
  orientation: CiVisualOrientation,
  parallel: { index: number; count: number } = { index: 0, count: 1 }
) {
  if (orientation === "horizontal") {
    const sourceBottom = source.y + source.height;
    const targetBottom = target.y + target.height;
    if (sourceBottom <= target.y) {
      return { start: "BOTTOM", end: "TOP" } as const;
    }
    if (targetBottom <= source.y) {
      return { start: "TOP", end: "BOTTOM" } as const;
    }
    return source.x <= target.x
      ? { start: "RIGHT", end: "LEFT" } as const
      : { start: "BOTTOM", end: "BOTTOM" } as const;
  }

  const sourceCenterX = source.x + source.width / 2;
  const sourceCenterY = source.y + source.height / 2;
  const targetCenterX = target.x + target.width / 2;
  const targetCenterY = target.y + target.height / 2;
  const isHorizontal = Math.abs(targetCenterX - sourceCenterX) >=
    Math.abs(targetCenterY - sourceCenterY);

  if (parallel.count > 1) {
    if (isHorizontal) {
      return parallel.index % 2 === 0
        ? { start: "TOP", end: "TOP" } as const
        : { start: "BOTTOM", end: "BOTTOM" } as const;
    }
    if (parallel.index > 0) return { start: "LEFT", end: "LEFT" } as const;
  }

  if (isHorizontal) {
    return sourceCenterX <= targetCenterX
      ? { start: "RIGHT", end: "LEFT" } as const
      : { start: "LEFT", end: "RIGHT" } as const;
  }
  return sourceCenterY <= targetCenterY
    ? { start: "BOTTOM", end: "TOP" } as const
    : { start: "TOP", end: "BOTTOM" } as const;
}

function parallelConnectionInfo(connections: CiVisualConnection[]) {
  const byEndpoints = new Map<string, CiVisualConnection[]>();
  for (const edge of connections) {
    const key = [edge.source, edge.target].sort().join("::");
    const edges = byEndpoints.get(key) || [];
    edges.push(edge);
    byEndpoints.set(key, edges);
  }
  const result = new Map<string, { index: number; count: number }>();
  for (const edges of byEndpoints.values()) {
    edges.forEach((edge, index) => result.set(edge.id, { index, count: edges.length }));
  }
  return result;
}

function cumulativePositions(
  keys: number[],
  sizes: Map<number, number>,
  gap: number,
  start: number
): Map<number, number> {
  const positions = new Map<number, number>();
  let next = start;
  for (const key of keys) {
    positions.set(key, next);
    next += (sizes.get(key) ?? 0) + gap;
  }
  return positions;
}

function modeName(type: CiVisualNode["type"]): string {
  const names: Record<CiVisualNode["type"], string> = {
    actor: "Actor",
    system: "System",
    "git reference": "Git reference",
    pipeline: "Pipeline",
    job: "Job",
    artifact: "Artifact",
    check: "Check",
    gate: "Gate",
  };
  return names[type];
}

function resizeChildSection(section, groups, mutatedNodeIds) {
  const maxRight = Math.max(...groups.map((group) => group.x + group.width));
  const maxBottom = Math.max(...groups.map((group) => group.y + group.height));
  section.resizeWithoutConstraints(maxRight + SECTION_PADDING, maxBottom + SECTION_PADDING);
  mutatedNodeIds.push(section.id);
}

function layoutChildSections(parent, mutatedNodeIds) {
  const header = parent.children.find((child) => child.type === "INSTANCE" && child.name === HEADER_INSTANCE_NAME);
  const sections = parent.children
    .filter((child) => child.type === "SECTION")
    .sort((first, second) => targetOrder(first) - targetOrder(second));
  let nextY = (header?.height || 0) + SECTION_GAP;

  for (const section of sections) {
    section.x = SECTION_PADDING;
    section.y = nextY;
    nextY += section.height + SECTION_GAP;
    mutatedNodeIds.push(section.id);
  }
}

function resizeParent(parent, mutatedNodeIds) {
  const children = parent.children.filter((child) => child.visible !== false);
  const { width, height } = ciParentResizeDimensions(children);
  parent.resizeWithoutConstraints(width, height);
  const header = children.find((child) => child.type === "INSTANCE" && child.name === HEADER_INSTANCE_NAME);
  if (header?.type === "INSTANCE") {
    header.resizeWithoutConstraints(width, header.height);
  }
  mutatedNodeIds.push(parent.id);
}

export function ciParentResizeDimensions(
  children: Array<{
    type: string;
    name?: string;
    visible?: boolean;
    x: number;
    y: number;
    width: number;
    height: number;
  }>
) {
  const visibleChildren = children.filter((child) => child.visible !== false);
  const sections = visibleChildren.filter((child) => child.type === "SECTION");
  const header = visibleChildren.find(
    (child) => child.type === "INSTANCE" && child.name === HEADER_INSTANCE_NAME
  );
  const width = Math.max(...sections.map((section) => section.x + section.width), 1) + SECTION_PADDING;
  const height = Math.max(
    ...sections.map((section) => section.y + section.height),
    header ? header.y + header.height : 1
  ) + SECTION_PADDING;
  return { width, height };
}

function targetOrder(section) {
  const target = section.getSharedPluginData(METADATA_NAMESPACE, CI_TARGET_KEY);
  const index = CI_VISUAL_TARGET_NAMES.indexOf(target as any);
  return index < 0 ? Number.MAX_SAFE_INTEGER : index;
}

function setComponentTextProperty(instance, propertyName, value) {
  const key = requireComponentPropertyKey(instance, propertyName, "TEXT");
  instance.setProperties({ [key]: value });
}

function setComponentBooleanProperty(instance, propertyName, value) {
  const key = requireComponentPropertyKey(instance, propertyName, "BOOLEAN");
  instance.setProperties({ [key]: value });
}

function requireComponentPropertyKey(instance, propertyName, propertyType) {
  const expected = normalizePropertyName(propertyName);
  const match = Object.entries(instance.componentProperties || {}).find(([key, property]: [string, any]) =>
    normalizePropertyName(key) === expected && property.type === propertyType
  );
  if (!match) {
    throw new Error(`CI node '${instance.id}' is missing ${propertyType} property '${propertyName}'.`);
  }
  return match[0];
}

function normalizePropertyName(value) {
  return value.split("#")[0].trim().toLowerCase();
}

async function applyTextLinks(root, textNodeName, links, expectedText) {
  const text = root.findAllWithCriteria({ types: ["TEXT"] })
    .find((candidate) => candidate.name.toLowerCase() === textNodeName.toLowerCase());
  if (!text || text.characters !== expectedText) return;

  await loadTextNodeFonts(text);

  let start = 0;
  for (const link of links) {
    const end = start + link.label.length;
    text.setRangeHyperlink(start, end, { type: "URL", value: link.url });
    start = end + 1;
  }
}

async function setNativeConnectorLabel(
  connector,
  label
) {
  const currentFontName = connector.text.fontName;
  const fontName = currentFontName === figma.mixed ||
      !currentFontName.family?.trim() ||
      !currentFontName.style?.trim()
    ? CI_CONNECTOR_FALLBACK_FONT
    : currentFontName;
  await figma.loadFontAsync(fontName);
  connector.text.fontName = fontName;
  connector.text.characters = label;
}

function setComponentVariantProperty(instance, propertyName, value) {
  const key = requireComponentPropertyKey(instance, propertyName, "VARIANT");
  instance.setProperties({ [key]: value });
}

function cumulativePositionsWithVariableGaps(
  keys: number[],
  sizes: Map<number, number>,
  gaps: Map<number, number>,
  defaultGap: number,
  start: number
) {
  const positions = new Map<number, number>();
  let next = start;
  for (const key of keys) {
    positions.set(key, next);
    next += (sizes.get(key) ?? 0) + Math.max(defaultGap, gaps.get(key) || 0);
  }
  return positions;
}

async function requireColorVariable(name) {
  const variables = await figma.variables.getLocalVariablesAsync("COLOR");
  const variable = variables.find((candidate) => candidate.name === name);
  if (!variable) throw new Error(`Color variable '${name}' was not found.`);
  return variable;
}

async function requireVariableCollectionById(collectionId) {
  const collection = await figma.variables.getVariableCollectionByIdAsync(collectionId);
  if (!collection) throw new Error(`Variable collection '${collectionId}' was not found.`);
  return collection;
}

function resolveColorForConsumer(variable, consumer) {
  const resolved = variable.resolveForConsumer(consumer);
  if (resolved.resolvedType !== "COLOR" || !resolved.value) {
    throw new Error(`Color variable '${variable.name}' did not resolve for '${consumer.id}'.`);
  }
  const { r, g, b } = resolved.value;
  return { r, g, b };
}

function boundColorPaint(variable, fallbackColor = { r: 0, g: 0, b: 0 }) {
  return figma.variables.setBoundVariableForPaint(
    { type: "SOLID", color: fallbackColor, opacity: 1 },
    "color",
    variable
  );
}

function sourceLink(path) {
  return {
    label: path,
    url: `${GITHUB_MAIN_BLOB_URL}/${path}`,
  };
}

function unlockTree(parent, mutatedNodeIds) {
  if (parent.locked) {
    parent.locked = false;
    mutatedNodeIds.push(parent.id);
  }
  for (const node of parent.findAll(() => true)) {
    if ("locked" in node && node.locked) {
      node.locked = false;
      mutatedNodeIds.push(node.id);
    }
  }
}

function lockOnlyParent(parent, mutatedNodeIds) {
  for (const node of parent.findAll(() => true)) {
    if ("locked" in node && node.locked) {
      node.locked = false;
      mutatedNodeIds.push(node.id);
    }
  }
  parent.locked = true;
  mutatedNodeIds.push(parent.id);
}
