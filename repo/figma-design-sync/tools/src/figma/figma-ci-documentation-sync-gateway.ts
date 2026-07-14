import {
  CI_CONNECTOR_NAME,
  CI_CONNECTOR_TEMPLATE_SECTION_ID,
  CI_DOCUMENTATION_PAGE_ID,
  CI_NODE_COMPONENT_ID,
  CI_NODE_INSTANCE_NAME,
  CI_NODE_PROPS,
  CI_VARIABLE_COLLECTION_NAME,
  CONNECTOR_TEMPLATE_NAME,
  HEADER_INSTANCE_NAME,
  HEADER_SECTION_TARGETS,
  METADATA_NAMESPACE,
} from "../config/figma-config";
import {
  CI_VISUAL_TARGET_NAMES,
  createCiVisualPlan,
  type CiVisualNode,
  type CiVisualSection,
} from "../domain/ci/create-ci-visual-plan";
import type { DesignModel } from "../domain/design-model";
import type { CiDocumentationSyncGateway } from "../ports/sync-gateways";
import {
  applySectionStrokeContractTree,
  requireComponent,
  requireModeId,
  requireOutlineColorVariable,
  requirePage,
  requireSection,
  requireVariableCollection,
} from "./figma-node-gateway";
import { loadTextNodeFonts } from "./figma-text-gateway";

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

export class FigmaCiDocumentationSyncGateway implements CiDocumentationSyncGateway {
  async syncCiDocumentation(designModel: DesignModel, targetNames: string[]) {
    const unknownTargets = targetNames.filter((target) => !CI_VISUAL_TARGET_NAMES.includes(target as any));
    if (unknownTargets.length > 0) {
      throw new Error(`Unknown CI documentation target(s): ${unknownTargets.join(", ")}.`);
    }

    const plan = createCiVisualPlan(designModel);
    const page = await requirePage(CI_DOCUMENTATION_PAGE_ID);
    const nodeComponent = await requireComponent(CI_NODE_COMPONENT_ID);
    const modeCollection = await requireVariableCollection(CI_VARIABLE_COLLECTION_NAME);
    const outlineVariable = await requireOutlineColorVariable();
    const surfaceVariable = await requireColorVariable("md/sys/color/surface");
    const mutatedNodeIds: string[] = [];
    const createdCiNodes: string[] = [];
    const createdCiConnectors: string[] = [];
    const updatedCiSections: string[] = [];
    const parent = await requireOrCreateParentSection(page, plan.parentName, surfaceVariable, mutatedNodeIds);
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
      createdCiConnectors,
      mutatedNodeIds: [...new Set(mutatedNodeIds)],
    };
  }
}

async function requireOrCreateParentSection(page, name, surfaceVariable, mutatedNodeIds) {
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
  parent.fills = [boundColorPaint(surfaceVariable)];
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
  ];
  setComponentTextProperty(header, "Header", "Continuous Integration and Design Documentation");
  setComponentTextProperty(
    header,
    "Definition",
    "Current pull request integration, post-merge design documentation, and CI infrastructure."
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
  for (const child of [...section.children]) {
    const role = child.getSharedPluginData?.(METADATA_NAMESPACE, CI_ROLE_KEY);
    if (role === ROLE_NODE || role === ROLE_CONNECTOR) {
      mutatedNodeIds.push(child.id);
      child.remove();
    }
  }
}

async function syncSectionContent(
  section,
  plan: CiVisualSection,
  nodeComponent,
  modeCollection,
  outlineVariable,
  createdCiNodes,
  createdCiConnectors,
  mutatedNodeIds
) {
  const groupsByModelId = new Map<string, GroupNode>();
  const groups: Array<{ plan: CiVisualNode; group: GroupNode }> = [];
  const connectorTemplate = plan.connections.length > 0
    ? await requireCiConnectorTemplate()
    : null;

  for (const nodePlan of plan.nodes) {
    const instance = nodeComponent.createInstance();
    instance.name = CI_NODE_INSTANCE_NAME;
    section.appendChild(instance);
    await syncCiNode(instance, nodePlan, modeCollection);
    const group = figma.group([instance], section);
    group.name = ".ci node group";
    group.setSharedPluginData(METADATA_NAMESPACE, CI_ROLE_KEY, ROLE_NODE);
    group.setSharedPluginData(METADATA_NAMESPACE, CI_MODEL_ID_KEY, nodePlan.id);
    groupsByModelId.set(nodePlan.id, group);
    groups.push({ plan: nodePlan, group });
    createdCiNodes.push(instance.id);
    mutatedNodeIds.push(group.id, instance.id);
  }

  layoutNodeGroups(groups);

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
    connector.connectorStart = { endpointNodeId: source.id, magnet: "BOTTOM" };
    connector.connectorEnd = { endpointNodeId: target.id, magnet: "TOP" };
    connector.strokes = [boundColorPaint(outlineVariable)];
    connector.strokeWeight = 2;
    connector.setSharedPluginData(METADATA_NAMESPACE, CI_ROLE_KEY, ROLE_CONNECTOR);
    connector.setSharedPluginData(METADATA_NAMESPACE, CI_MODEL_ID_KEY, edge.id);
    section.insertChild(0, connector);
    await setConnectorLabel(connector, edge.label);
    createdCiConnectors.push(connector.id);
    mutatedNodeIds.push(connector.id);
  }

  resizeChildSection(section, groups.map((item) => item.group), mutatedNodeIds);
  applySectionStrokeContractTree(section, outlineVariable, mutatedNodeIds);
}

async function requireCiConnectorTemplate(): Promise<ConnectorNode> {
  const section = await requireSection(CI_CONNECTOR_TEMPLATE_SECTION_ID);
  const connector = section.findAllWithCriteria({ types: ["CONNECTOR"] })
    .find((candidate) => candidate.name === CONNECTOR_TEMPLATE_NAME);
  if (!connector) {
    throw new Error(
      `No '${CONNECTOR_TEMPLATE_NAME}' connector template was found in section '${section.id}'.`
    );
  }
  return connector;
}

async function syncCiNode(instance, nodePlan: CiVisualNode, modeCollection) {
  const modeId = requireModeId(modeCollection, modeName(nodePlan.type));
  instance.setExplicitVariableModeForCollection(modeCollection, modeId);
  setComponentTextProperty(instance, CI_NODE_PROPS.name, nodePlan.name);
  setComponentTextProperty(instance, CI_NODE_PROPS.description, nodePlan.description);
  setComponentTextProperty(instance, CI_NODE_PROPS.steps, nodePlan.steps || "");
  setComponentTextProperty(instance, CI_NODE_PROPS.source, nodePlan.source);
  setComponentBooleanProperty(instance, CI_NODE_PROPS.showSteps, Boolean(nodePlan.steps));
  setComponentBooleanProperty(instance, CI_NODE_PROPS.showSource, true);
  await applyTextLinks(
    instance,
    "File",
    [{ label: nodePlan.source, url: nodePlan.sourceUrl }],
    nodePlan.source
  );
}

function layoutNodeGroups(groups: Array<{ plan: CiVisualNode; group: GroupNode }>) {
  const rows = [...new Set(groups.map(({ plan }) => plan.row))].sort((a, b) => a - b);
  const columns = [...new Set(groups.map(({ plan }) => plan.column))].sort((a, b) => a - b);
  const rowHeights = new Map(rows.map((row) => [
    row,
    Math.max(...groups.filter(({ plan }) => plan.row === row).map(({ group }) => group.height)),
  ]));
  const columnWidths = new Map(columns.map((column) => [
    column,
    Math.max(...groups.filter(({ plan }) => plan.column === column).map(({ group }) => group.width)),
  ]));
  const rowY = cumulativePositions(rows, rowHeights, NODE_ROW_GAP, SECTION_PADDING);
  const columnX = cumulativePositions(columns, columnWidths, NODE_COLUMN_GAP, SECTION_PADDING);

  for (const { plan, group } of groups) {
    const columnWidth = columnWidths.get(plan.column)!;
    group.x = columnX.get(plan.column)! + (columnWidth - group.width) / 2;
    group.y = rowY.get(plan.row)!;
  }
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
  const width = Math.max(...children.map((child) => child.x + child.width), 1) + SECTION_PADDING;
  const height = Math.max(...children.map((child) => child.y + child.height), 1) + SECTION_PADDING;
  parent.resizeWithoutConstraints(width, height);
  const header = children.find((child) => child.type === "INSTANCE" && child.name === HEADER_INSTANCE_NAME);
  if (header?.type === "INSTANCE") {
    header.resizeWithoutConstraints(width, header.height);
  }
  mutatedNodeIds.push(parent.id);
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

async function setConnectorLabel(connector, label) {
  const currentFontName = connector.text.fontName;
  const fontName = currentFontName === figma.mixed ||
      !currentFontName.family?.trim() ||
      !currentFontName.style?.trim()
    ? { family: "Poppins", style: "Regular" }
    : currentFontName;
  await figma.loadFontAsync(fontName);
  connector.text.fontName = fontName;
  connector.text.characters = label;
}

async function requireColorVariable(name) {
  const variables = await figma.variables.getLocalVariablesAsync("COLOR");
  const variable = variables.find((candidate) => candidate.name === name);
  if (!variable) throw new Error(`Color variable '${name}' was not found.`);
  return variable;
}

function boundColorPaint(variable) {
  return figma.variables.setBoundVariableForPaint(
    { type: "SOLID", color: { r: 0, g: 0, b: 0 }, opacity: 1 },
    "color",
    variable
  );
}

function sourceLink(path) {
  return {
    label: path,
    url: `https://github.com/marmatsan/water-my-plants/blob/main/${path}`,
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
