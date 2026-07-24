import {
  ARTIFACTS_BUNDLE_INSTANCE_NAME,
  ARTIFACTS_BUNDLE_PROPS,
  ARTIFACT_INSTANCE_NAME,
  ARTIFACT_PROPS,
  CATALOG_TREE_TARGETS,
  CI_CONNECTOR_TEMPLATE_SECTION_ID,
  CI_DOCUMENTATION_PAGE_ID,
  CI_ICON_COMPONENT_SET_ID,
  CI_ICON_ENVIRONMENT_PROPERTY,
  CI_ICON_ENVIRONMENTS,
  CI_ICON_INSTANCE_NAME,
  CI_NODE_COMPONENT_ID,
  CI_NODE_OUTCOME_CONTAINER_NAME,
  CI_NODE_PHASE_CONTAINER_NAME,
  CI_NODE_PROPS,
  CI_OUTCOME_COMPONENT_SET_ID,
  CI_OUTCOME_KINDS,
  CI_OUTCOME_PROPS,
  CI_PHASE_COMPONENT_ID,
  CI_PHASE_PROPS,
  CI_PHASE_STEP_CONTAINER_NAME,
  CI_STEP_COMPONENT_SET_ID,
  CI_STEP_PROPS,
  CI_STEP_ROLES,
  CI_VISUAL_TARGET_NAMES,
  CI_VARIABLE_COLLECTION_NAME,
  CI_VARIABLE_MODE_NAMES,
  CONNECTOR_TEMPLATE_NAME,
  HEADER_INSTANCE_NAME,
  HEADER_LINK_PROPERTY_NAME,
  HEADER_SECTION_TARGETS,
  METADATA_PAGE_ID,
  PROJECT_VERSION_COMPONENT_ID,
  TOOL_ARTIFACT_USAGE_INSTANCE_NAME,
  TOOL_ARTIFACT_USAGE_PROPS,
  TREE_NODE_COMPONENT_IDS,
  TREE_NODE_PROPS,
  USAGE_CHIP_COMPONENT_SET_ID,
  USAGE_CHIP_INSTANCE_NAME,
  USAGE_CHIP_KINDS,
  USAGE_CHIP_PROPS,
  VERSION_ALIAS_MODE_NAME,
  VERSION_NUMBER_MODE_NAME,
  VERSION_SECTION_TARGETS,
  VERSIONS_COLLECTION_NAMES,
} from "@figma-documentation-sync/project-config";
import { flattenCatalogNodes, requireUniqueLabels } from "../domain/catalog/flatten-catalog-nodes";
import {
  libraryArtifacts,
  libraryBundles,
} from "../domain/catalog/library-catalog-entries";
import type { DesignModel } from "../domain/design-model";
import type {
  VisualContractCheckGateway,
  VisualContractCheckOptions,
} from "../ports/sync-gateways";
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
import { filterModelRoots } from "./figma-catalog-tree-sync-gateway";
import {
  requireComponent,
  requireComponentSet,
  requireFrameOrSection,
  requireModeId,
  requireOutlineColorVariable,
  requirePage,
  requireSection,
  requireVariableCollection,
} from "./figma-node-gateway";

export class FigmaVisualContractCheckGateway implements VisualContractCheckGateway {
  async checkVisualContract(
    designModel: DesignModel,
    options: VisualContractCheckOptions = {}
  ) {
    const checkedComponents: string[] = [];
    const checkedSections: string[] = [];
    const checkedVariables: string[] = [];
    const checkedTargets: string[] = [];

    const metadataPage = await requirePage(METADATA_PAGE_ID);
    checkedSections.push(`metadata:${metadataPage.id}`);

    await checkVersionContract(checkedComponents, checkedSections, checkedVariables);
    await checkHeaderContract(checkedComponents, checkedSections);
    await checkCatalogTreeContract(designModel, options, checkedComponents, checkedSections, checkedTargets);
    await checkCiDocumentationContract(options, checkedComponents, checkedSections, checkedVariables, checkedTargets);

    return {
      checkedComponents,
      checkedSections,
      checkedVariables,
      checkedTargets,
      mutatedNodeIds: [],
    };
  }
}

async function checkCiDocumentationContract(
  options,
  checkedComponents,
  checkedSections,
  checkedVariables,
  checkedTargets
) {
  const requestedTargets = options.targetNames == null
    ? [...CI_VISUAL_TARGET_NAMES]
    : options.targetNames.filter((target) => CI_VISUAL_TARGET_NAMES.includes(target));
  if (requestedTargets.length === 0) return;

  const page = await requirePage(CI_DOCUMENTATION_PAGE_ID);
  checkedSections.push(`ciDocumentation:${page.id}`);

  const component = await requireComponent(CI_NODE_COMPONENT_ID);
  requireComponentProperty(component, CI_NODE_PROPS.name, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.description, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.executionPlanHeading, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.source, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.runtimePlatform, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.runtimeService, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.runtimeStartup, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.runtimeIdentity, "TEXT");
  requireComponentProperty(component, CI_NODE_PROPS.showExecutionPlan, "BOOLEAN");
  requireComponentProperty(component, CI_NODE_PROPS.showOutcome, "BOOLEAN");
  requireComponentProperty(component, CI_NODE_PROPS.showSource, "BOOLEAN");
  requireComponentProperty(component, CI_NODE_PROPS.showRuntime, "BOOLEAN");
  requireComponentProperty(component, CI_NODE_PROPS.showOptionalDetails, "BOOLEAN");
  requireMissingComponentProperty(component, "steps");
  requireMissingComponentProperty(component, "show steps");
  checkedComponents.push(`${component.name}:${component.id}`);

  const executionPlan = requireDirectFrame(
    component,
    CI_NODE_PHASE_CONTAINER_NAME,
    ".ci node"
  );
  requireComponentPropertyReference(
    executionPlan,
    "visible",
    CI_NODE_PROPS.showExecutionPlan,
    ".ci node execution plan"
  );
  const outcome = requireDirectFrame(
    component,
    CI_NODE_OUTCOME_CONTAINER_NAME,
    ".ci node"
  );
  requireComponentPropertyReference(
    outcome,
    "visible",
    CI_NODE_PROPS.showOutcome,
    ".ci node outcome"
  );
  const executionPlanHeading = requireDirectFrame(
    executionPlan,
    CI_NODE_PROPS.executionPlanHeading,
    ".ci node execution plan"
  );
  const executionPlanHeadingTexts = executionPlanHeading.findAllWithCriteria({ types: ["TEXT"] })
    .filter((text) => text.name === CI_NODE_PROPS.executionPlanHeading);
  if (executionPlanHeadingTexts.length !== 1) {
    throw new Error(
      `Execution plan heading '${executionPlanHeading.id}' must contain exactly one ` +
        `'${CI_NODE_PROPS.executionPlanHeading}' text layer; found ${executionPlanHeadingTexts.length}.`
    );
  }
  requireComponentPropertyReference(
    executionPlanHeadingTexts[0],
    "characters",
    CI_NODE_PROPS.executionPlanHeading,
    ".ci node execution plan heading"
  );

  const phaseComponent = await requireComponent(CI_PHASE_COMPONENT_ID);
  requireComponentProperty(phaseComponent, CI_PHASE_PROPS.order, "TEXT");
  requireComponentProperty(phaseComponent, CI_PHASE_PROPS.title, "TEXT");
  requireComponentProperty(phaseComponent, CI_PHASE_PROPS.technicalId, "TEXT");
  requireComponentProperty(phaseComponent, CI_PHASE_PROPS.description, "TEXT");
  requireComponentProperty(phaseComponent, CI_PHASE_PROPS.showTechnicalId, "BOOLEAN");
  requireComponentProperty(phaseComponent, CI_PHASE_PROPS.showDescription, "BOOLEAN");
  requireComponentProperty(phaseComponent, CI_PHASE_PROPS.showSteps, "BOOLEAN");
  const phaseSteps = requireDirectFrame(
    phaseComponent,
    CI_PHASE_STEP_CONTAINER_NAME,
    ".ci phase"
  );
  requireComponentPropertyReference(
    phaseSteps,
    "visible",
    CI_PHASE_PROPS.showSteps,
    ".ci phase steps"
  );
  checkedComponents.push(`${phaseComponent.name}:${phaseComponent.id}`);

  const stepSet = await requireComponentSet(CI_STEP_COMPONENT_SET_ID);
  requireComponentProperty(stepSet, CI_STEP_PROPS.order, "TEXT");
  requireComponentProperty(stepSet, CI_STEP_PROPS.title, "TEXT");
  requireComponentProperty(stepSet, CI_STEP_PROPS.technicalId, "TEXT");
  const tasksDefinition = requireComponentProperty(stepSet, CI_STEP_PROPS.tasks, "TEXT");
  requireComponentProperty(stepSet, CI_STEP_PROPS.description, "TEXT");
  requireComponentProperty(stepSet, CI_STEP_PROPS.condition, "TEXT");
  requireComponentProperty(stepSet, CI_STEP_PROPS.showTechnicalId, "BOOLEAN");
  requireComponentProperty(stepSet, CI_STEP_PROPS.showDescription, "BOOLEAN");
  requireComponentProperty(stepSet, CI_STEP_PROPS.showCondition, "BOOLEAN");
  requireExactVariantOptions({
    node: stepSet,
    propertyName: CI_STEP_PROPS.role,
    expectedOptions: CI_STEP_ROLES,
    label: "CI step roles",
  });
  requireCiGroupTasksContract({
    stepSet,
    tasksDefinition,
  });
  requireMissingComponentProperty(stepSet, "level");
  checkedComponents.push(`${stepSet.name}:${stepSet.id}`);

  const outcomeSet = await requireComponentSet(CI_OUTCOME_COMPONENT_SET_ID);
  requireComponentProperty(outcomeSet, CI_OUTCOME_PROPS.order, "TEXT");
  requireComponentProperty(outcomeSet, CI_OUTCOME_PROPS.title, "TEXT");
  requireComponentProperty(outcomeSet, CI_OUTCOME_PROPS.technicalId, "TEXT");
  requireComponentProperty(outcomeSet, CI_OUTCOME_PROPS.description, "TEXT");
  requireComponentProperty(outcomeSet, CI_OUTCOME_PROPS.condition, "TEXT");
  requireComponentProperty(outcomeSet, CI_OUTCOME_PROPS.showTechnicalId, "BOOLEAN");
  requireComponentProperty(outcomeSet, CI_OUTCOME_PROPS.showDescription, "BOOLEAN");
  requireComponentProperty(outcomeSet, CI_OUTCOME_PROPS.showCondition, "BOOLEAN");
  requireExactVariantOptions({
    node: outcomeSet,
    propertyName: CI_OUTCOME_PROPS.kind,
    expectedOptions: CI_OUTCOME_KINDS,
    label: "CI outcome kinds",
  });
  checkedComponents.push(`${outcomeSet.name}:${outcomeSet.id}`);

  await requireExactReservedSlots({
    owner: executionPlan,
    expectedNames: ciPhaseSlotNames(),
    hasNamePrefix: hasCiPhaseSlotNamePrefix,
    label: "CI phase",
    matchesComponent: async (slot) => (await slot.getMainComponentAsync())?.id === phaseComponent.id,
    expectedComponentLabel: `component '${phaseComponent.id}'`,
  });
  await requireExactReservedSlots({
    owner: outcome,
    expectedNames: ciOutcomeSlotNames(),
    hasNamePrefix: hasCiOutcomeSlotNamePrefix,
    label: "CI outcome",
    matchesComponent: async (slot) => (await slot.getMainComponentAsync())?.parent?.id === outcomeSet.id,
    expectedComponentLabel: `component set '${outcomeSet.id}'`,
  });
  await requireExactReservedSlots({
    owner: phaseSteps,
    expectedNames: ciStepSlotNames(),
    hasNamePrefix: hasCiStepSlotNamePrefix,
    label: "CI step",
    matchesComponent: async (slot) => (await slot.getMainComponentAsync())?.parent?.id === stepSet.id,
    expectedComponentLabel: `component set '${stepSet.id}'`,
  });

  const iconSet = await requireComponentSet(CI_ICON_COMPONENT_SET_ID);
  const nestedIcons = component.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((candidate) => candidate.name === CI_ICON_INSTANCE_NAME);
  if (nestedIcons.length !== 1) {
    throw new Error(
      `CI node component '${component.id}' must contain exactly one '${CI_ICON_INSTANCE_NAME}' ` +
        `nested instance; found ${nestedIcons.length}.`
    );
  }
  const nestedIcon = nestedIcons[0];
  const mainIconComponent = await nestedIcon.getMainComponentAsync();
  if (!mainIconComponent || mainIconComponent.parent?.id !== iconSet.id) {
    throw new Error(
      `Nested CI icon '${nestedIcon.id}' must belong to component set '${iconSet.id}'.`
    );
  }
  requireComponentProperty(nestedIcon, CI_ICON_ENVIRONMENT_PROPERTY, "VARIANT");
  const environmentDefinition = requireComponentProperty(
    iconSet,
    CI_ICON_ENVIRONMENT_PROPERTY,
    "VARIANT"
  );
  const actualEnvironments = environmentDefinition.variantOptions || [];
  const missingEnvironments = CI_ICON_ENVIRONMENTS.filter((value) => !actualEnvironments.includes(value));
  const unexpectedEnvironments = actualEnvironments.filter((value) => !CI_ICON_ENVIRONMENTS.includes(value));
  if (missingEnvironments.length > 0 || unexpectedEnvironments.length > 0) {
    throw new Error(
      `CI icon environments differ from the supported contract. ` +
        `Missing: ${missingEnvironments.join(", ") || "none"}. ` +
        `Unexpected: ${unexpectedEnvironments.join(", ") || "none"}.`
    );
  }
  checkedComponents.push(`${iconSet.name}:${iconSet.id}`);

  const connectorSection = await requireSection(CI_CONNECTOR_TEMPLATE_SECTION_ID);
  const connectorTemplate = connectorSection.findAllWithCriteria({ types: ["CONNECTOR"] })
    .find((candidate) => candidate.name === CONNECTOR_TEMPLATE_NAME);
  if (!connectorTemplate) {
    throw new Error(
      `No '${CONNECTOR_TEMPLATE_NAME}' connector template was found in section '${connectorSection.id}'.`
    );
  }
  checkedComponents.push(`${connectorTemplate.name}:${connectorTemplate.id}`);
  checkedSections.push(`ciConnectorTemplate:${connectorSection.id}`);

  const collection = await requireVariableCollection(CI_VARIABLE_COLLECTION_NAME);
  for (const modeName of CI_VARIABLE_MODE_NAMES) {
    requireModeId(collection, modeName);
  }
  checkedVariables.push(collection.name);
  checkedTargets.push(...requestedTargets);
}

function requireCiGroupTasksContract({
  stepSet,
  tasksDefinition,
}: {
  stepSet: ComponentSetNode;
  tasksDefinition: any;
}) {
  const defaultLines = String(tasksDefinition.defaultValue || "")
    .split(/\r?\n/)
    .filter(Boolean);
  if (defaultLines.length < 2 || defaultLines.some((line) => !line.startsWith("• "))) {
    throw new Error(
      `CI step component set '${stepSet.id}' must expose a multiline bullet-list default ` +
        `for '${CI_STEP_PROPS.tasks}'.`
    );
  }

  const groupVariant = stepSet.children.find((child) =>
    child.type === "COMPONENT" && child.variantProperties?.[CI_STEP_PROPS.role] === "group"
  );
  if (!groupVariant || groupVariant.type !== "COMPONENT") {
    throw new Error(`CI step component set '${stepSet.id}' is missing role=group.`);
  }
  const taskTexts = groupVariant.findAllWithCriteria({ types: ["TEXT"] })
    .filter((text) => text.name === CI_STEP_PROPS.tasks);
  if (taskTexts.length !== 1) {
    throw new Error(
      `CI step role=group '${groupVariant.id}' must contain exactly one ` +
        `'${CI_STEP_PROPS.tasks}' text layer; found ${taskTexts.length}.`
    );
  }
  const tasksText = taskTexts[0];
  requireComponentPropertyReference(
    tasksText,
    "characters",
    CI_STEP_PROPS.tasks,
    "CI step role=group tasks"
  );
  if (tasksText.textAlignHorizontal !== "LEFT" || tasksText.textAutoResize !== "HEIGHT") {
    throw new Error(
      `CI step role=group tasks '${tasksText.id}' must be left-aligned with HEIGHT text resize.`
    );
  }
}

function requireDirectFrame(
  owner: ComponentNode | FrameNode,
  frameName: string,
  ownerLabel: string
): FrameNode {
  const frames = owner.children.filter(
    (candidate) => candidate.type === "FRAME" && candidate.name === frameName
  ) as FrameNode[];
  if (frames.length !== 1) {
    throw new Error(
      `${ownerLabel} '${owner.id}' must contain exactly one direct '${frameName}' frame; ` +
        `found ${frames.length}.`
    );
  }
  return frames[0];
}

async function requireExactReservedSlots({
  owner,
  expectedNames,
  hasNamePrefix,
  label,
  matchesComponent,
  expectedComponentLabel,
}: {
  owner: ComponentNode | FrameNode;
  expectedNames: string[];
  hasNamePrefix: (name: string) => boolean;
  label: string;
  matchesComponent: (slot: InstanceNode) => Promise<boolean>;
  expectedComponentLabel: string;
}) {
  const slots = owner.children.filter(
    (candidate) => candidate.type === "INSTANCE" && hasNamePrefix(candidate.name)
  ) as InstanceNode[];
  const actualNames = slots.map((slot) => slot.name);
  const missingNames = expectedNames.filter((name) => !actualNames.includes(name));
  const unexpectedNames = actualNames.filter((name) => !expectedNames.includes(name));
  const duplicateNames = expectedNames.filter(
    (name) => actualNames.filter((actualName) => actualName === name).length > 1
  );
  if (
    slots.length !== expectedNames.length ||
    missingNames.length > 0 ||
    unexpectedNames.length > 0 ||
    duplicateNames.length > 0
  ) {
    throw new Error(
      `${label} owner '${owner.id}' must contain the exact reserved slots. ` +
        `Missing: ${missingNames.join(", ") || "none"}. ` +
        `Unexpected: ${unexpectedNames.join(", ") || "none"}. ` +
        `Duplicated: ${duplicateNames.join(", ") || "none"}.`
    );
  }
  for (const slot of slots) {
    if (!slot.isExposedInstance) {
      throw new Error(`${label} slot '${slot.name}' in '${owner.id}' must be exposed.`);
    }
    if (!await matchesComponent(slot)) {
      throw new Error(
        `${label} slot '${slot.name}' in '${owner.id}' must belong to ${expectedComponentLabel}.`
      );
    }
  }
}

async function checkHeaderContract(checkedComponents, checkedSections) {
  for (const target of HEADER_SECTION_TARGETS) {
    const section = await requireSection(target.sectionNodeId);
    const header = section.children.find(
      (child) => child.type === "INSTANCE" && child.name === HEADER_INSTANCE_NAME
    );
    if (!header || header.type !== "INSTANCE") {
      throw new Error(`Section '${section.id}' is missing a direct '${HEADER_INSTANCE_NAME}' instance.`);
    }
    requireComponentProperty(header, HEADER_LINK_PROPERTY_NAME, "TEXT");
    const linkTexts = header.findAllWithCriteria({ types: ["TEXT"] })
      .filter((text) => text.name === HEADER_LINK_PROPERTY_NAME);
    if (linkTexts.length !== 1) {
      throw new Error(`Header '${header.id}' expected one '${HEADER_LINK_PROPERTY_NAME}' text node, found ${linkTexts.length}.`);
    }
    checkedComponents.push(`${HEADER_INSTANCE_NAME}:${header.id}`);
    checkedSections.push(`header:${section.id}`);
  }
}

async function checkVersionContract(checkedComponents, checkedSections, checkedVariables) {
  const collection = await requireVariableCollection(VERSIONS_COLLECTION_NAMES);
  requireModeId(collection, VERSION_ALIAS_MODE_NAME);
  requireModeId(collection, VERSION_NUMBER_MODE_NAME);
  checkedVariables.push(collection.name);

  const projectVersionComponent = await requireComponent(PROJECT_VERSION_COMPONENT_ID);
  checkedComponents.push(`${projectVersionComponent.name}:${projectVersionComponent.id}`);
  const outlineVariable = await requireOutlineColorVariable();
  checkedVariables.push(outlineVariable.name);

  for (const [sectionName, sectionTarget] of Object.entries(VERSION_SECTION_TARGETS)) {
    const sectionFrame = await requireFrameOrSection(sectionTarget.parentNodeId);
    checkedSections.push(`${sectionName}:${sectionFrame.id}`);
  }
}

async function checkCatalogTreeContract(
  designModel,
  options,
  checkedComponents,
  checkedSections,
  checkedTargets
) {
  const targets = resolveCatalogTargets(options.targetNames);
  const requiredTreeNodeTypes = new Set(targets.map((target) => target.type));

  if (requiredTreeNodeTypes.has("Library")) {
    await checkLibraryTreeNodeComponent(checkedComponents);
  }
  if (requiredTreeNodeTypes.has("Plugin")) {
    await checkPluginTreeNodeComponent(checkedComponents);
  }
  if (requiredTreeNodeTypes.size > 0) {
    await checkUsageChipComponentSet(checkedComponents);
  }

  for (const target of targets) {
    const modelNodes = target.nodes(designModel);
    const sectionNodeId = options.sectionNodeOverrides?.[target.name] || target.sectionNodeId;

    if (modelNodes == null) {
      if (target.lifecycle === "stableDocumentationTarget") {
        const section = await requireSection(sectionNodeId);
        checkedSections.push(`${target.name}:${section.id}`);
        checkedTargets.push(target.name);
      }
      continue;
    }

    if (!Array.isArray(modelNodes)) {
      throw new Error(`designModel.content.catalogs.${target.name} is required for visual contract preflight.`);
    }

    const rootFilter = options.rootFilters?.[target.name];
    const scopedModelNodes = filterModelRoots(target, modelNodes, rootFilter);
    const expectedNodes = flattenCatalogNodes(scopedModelNodes, target.type);
    requireUniqueLabels(target, expectedNodes);

    if (target.lifecycle === "declaredCatalogTarget" && expectedNodes.length === 0) {
      checkedTargets.push(target.name);
      continue;
    }

    const section = await requireSection(sectionNodeId);
    checkedSections.push(`${target.name}:${section.id}`);
    if (target.type === "Library") {
      await checkLibraryTemplatesForTarget(section, expectedNodes, checkedComponents);
    }
    checkedTargets.push(target.name);
  }
}

async function checkLibraryTreeNodeComponent(checkedComponents) {
  const component = await requireComponent(TREE_NODE_COMPONENT_IDS.Library);
  checkedComponents.push(`${component.name}:${component.id}`);
  requireComponentProperty(component, TREE_NODE_PROPS.libraryGroup, "TEXT");
  requireComponentProperty(component, TREE_NODE_PROPS.showArtifacts, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.type, "VARIANT");
}

async function checkLibraryTemplatesForTarget(section, expectedNodes, checkedComponents) {
  const artifacts = expectedNodes.flatMap((node) => libraryArtifacts(node.entries));
  const bundles = expectedNodes.flatMap((node) => libraryBundles(node.entries));
  const requiresArtifactTemplate = artifacts.length > 0 ||
    bundles.some((bundle) => (bundle.artifacts || []).length > 0);
  const requiresBundleTemplate = bundles.length > 0;
  const requiresToolArtifactUsage = artifacts.some((artifact) =>
    (artifact.configuredByConventionPlugins || []).length > 0
  );

  if (requiresArtifactTemplate) {
    const artifact = await findLibraryTemplateInstance(section, ARTIFACT_INSTANCE_NAME);
    checkArtifactTemplate(artifact, checkedComponents);
    if (requiresToolArtifactUsage) {
      const toolArtifactUsage = requireNestedInstance(artifact, TOOL_ARTIFACT_USAGE_INSTANCE_NAME);
      requireComponentProperty(toolArtifactUsage, TOOL_ARTIFACT_USAGE_PROPS.target, "TEXT");
      checkedComponents.push(`${TOOL_ARTIFACT_USAGE_INSTANCE_NAME}:${toolArtifactUsage.id}`);
    }
  }

  if (requiresBundleTemplate) {
    const bundle = await findLibraryTemplateInstance(section, ARTIFACTS_BUNDLE_INSTANCE_NAME);
    checkBundleTemplate(bundle, checkedComponents);
    checkArtifactTemplate(requireNestedInstance(bundle, ARTIFACT_INSTANCE_NAME), checkedComponents);
  }
}

async function findLibraryTemplateInstance(section, instanceName) {
  const sectionTemplate = section.findAllWithCriteria({ types: ["INSTANCE"] })
    .find((candidate) => candidate.name === instanceName);
  if (sectionTemplate) return sectionTemplate;

  const component = await requireComponent(TREE_NODE_COMPONENT_IDS.Library);
  return requireNestedInstance(component, instanceName);
}

function checkArtifactTemplate(artifact, checkedComponents) {
  requireComponentProperty(artifact, ARTIFACT_PROPS.name, "TEXT");
  requireComponentProperty(artifact, ARTIFACT_PROPS.version, "TEXT");
  requireComponentProperty(artifact, ARTIFACT_PROPS.showVersion, "BOOLEAN");
  requireComponentProperty(artifact, ARTIFACT_PROPS.showAppliedByPlugin, "BOOLEAN");
  requireComponentProperty(artifact, ARTIFACT_PROPS.showUsedByModule, "BOOLEAN");
  requireComponentProperty(artifact, ARTIFACT_PROPS.showConfiguredAsTool, "BOOLEAN");
  checkedComponents.push(`${ARTIFACT_INSTANCE_NAME}:${artifact.id}`);
}

function checkBundleTemplate(bundle, checkedComponents) {
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.alias, "TEXT");
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.version, "TEXT");
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.showVersion, "BOOLEAN");
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.showAppliedByPlugin, "BOOLEAN");
  requireComponentProperty(bundle, ARTIFACTS_BUNDLE_PROPS.showUsedByModule, "BOOLEAN");
  checkedComponents.push(`${ARTIFACTS_BUNDLE_INSTANCE_NAME}:${bundle.id}`);
}

async function checkPluginTreeNodeComponent(checkedComponents) {
  const component = await requireComponent(TREE_NODE_COMPONENT_IDS.Plugin);
  checkedComponents.push(`${component.name}:${component.id}`);
  requireComponentProperty(component, TREE_NODE_PROPS.pluginId, "TEXT");
  requireComponentProperty(component, TREE_NODE_PROPS.pluginVersion, "TEXT");
  requireComponentProperty(component, TREE_NODE_PROPS.showPluginVersion, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.showAppliedByModule, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.showUsedByConventionPlugin, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.showUnused, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.showIsGradlePlugin, "BOOLEAN");
  requireComponentProperty(component, TREE_NODE_PROPS.type, "VARIANT");
}

async function checkUsageChipComponentSet(checkedComponents) {
  const componentSet = await figma.getNodeByIdAsync(USAGE_CHIP_COMPONENT_SET_ID);
  if (!componentSet || componentSet.type !== "COMPONENT_SET") {
    throw new Error(`Expected '${USAGE_CHIP_COMPONENT_SET_ID}' to be the '${USAGE_CHIP_INSTANCE_NAME}' COMPONENT_SET.`);
  }

  for (const kind of Object.values(USAGE_CHIP_KINDS)) {
    const variant = componentSet.children
      .filter((candidate) => candidate.type === "COMPONENT")
      .find((candidate) => candidate.variantProperties?.[USAGE_CHIP_PROPS.kind] === kind);
    if (!variant) {
      throw new Error(`Expected '${USAGE_CHIP_INSTANCE_NAME}' component set to contain kind='${kind}'.`);
    }
  }

  checkedComponents.push(`${componentSet.name}:${componentSet.id}`);
}

function resolveCatalogTargets(targetNames?: string[]) {
  const knownTargetNames = new Set(
    CATALOG_TREE_TARGETS.flatMap((target) => [target.name, ...(target.aliases || [])])
  );
  const scopedTargetNames = targetNames?.filter((targetName) => knownTargetNames.has(targetName));
  const targetNameSet = new Set<string>(scopedTargetNames || CATALOG_TREE_TARGETS.map((target) => target.name));

  return CATALOG_TREE_TARGETS.filter((target) =>
    targetNameSet.has(target.name) || (target.aliases || []).some((alias) => targetNameSet.has(alias))
  );
}

function requireNestedInstance(root: any, instanceName: string) {
  const instance = root.findAllWithCriteria({ types: ["INSTANCE"] })
    .find((candidate) => candidate.name === instanceName);
  if (!instance) {
    throw new Error(`Expected '${root.id}' to contain a '${instanceName}' template instance.`);
  }
  return instance;
}

function requireComponentProperty(node: any, propertyName: string, propertyType: string) {
  const properties = componentPropertiesForPreflight(node);
  const propertyEntry = Object.entries(properties)
    .find(([key, property]) =>
      (key === propertyName || key.startsWith(`${componentPropertyName(propertyName)}#`)) &&
        (!propertyType || property.type === propertyType)
    );

  if (!propertyEntry) {
    throw new Error(
      `Node '${node.id}' is missing ${propertyType} component property '${componentPropertyName(propertyName)}'.`
    );
  }
  return propertyEntry[1] as any;
}

function requireMissingComponentProperty(node: any, propertyName: string) {
  const expectedName = componentPropertyName(propertyName);
  const actualName = Object.keys(componentPropertiesForPreflight(node))
    .map(componentPropertyName)
    .find((name) => name === expectedName);
  if (actualName) {
    throw new Error(
      `Node '${node.id}' must not expose obsolete component property '${expectedName}'.`
    );
  }
}

function requireComponentPropertyReference(
  node: any,
  field: "characters" | "visible",
  propertyName: string,
  label: string
) {
  const reference = node.componentPropertyReferences?.[field];
  if (!reference || componentPropertyName(reference) !== componentPropertyName(propertyName)) {
    throw new Error(
      `${label} '${node.id}' must bind '${field}' to component property ` +
        `'${componentPropertyName(propertyName)}'.`
    );
  }
}

function requireExactVariantOptions({
  node,
  propertyName,
  expectedOptions,
  label,
}: {
  node: any;
  propertyName: string;
  expectedOptions: string[];
  label: string;
}) {
  const definition = requireComponentProperty(node, propertyName, "VARIANT");
  const actualOptions = definition.variantOptions || [];
  const missing = expectedOptions.filter((value) => !actualOptions.includes(value));
  const unexpected = actualOptions.filter((value) => !expectedOptions.includes(value));
  if (missing.length > 0 || unexpected.length > 0) {
    throw new Error(
      `${label} differ from the supported contract. ` +
        `Missing: ${missing.join(", ") || "none"}. ` +
        `Unexpected: ${unexpected.join(", ") || "none"}.`
    );
  }
  return definition;
}

function componentPropertiesForPreflight(node: any): Record<string, any> {
  if (node.type === "INSTANCE") {
    return node.componentProperties || {};
  }

  if (node.type === "COMPONENT" && node.parent?.type === "COMPONENT_SET") {
    return node.parent.componentPropertyDefinitions || {};
  }

  if (node.type === "COMPONENT" || node.type === "COMPONENT_SET") {
    return node.componentPropertyDefinitions || {};
  }

  return {};
}

function componentPropertyName(propertyName) {
  return propertyName.split("#")[0];
}
