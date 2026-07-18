import {
  CI_CONNECTOR_NAME,
  CI_CONNECTOR_LABEL_NAME,
  CI_CONNECTOR_TEMPLATE_SECTION_ID,
  CI_DOCUMENTATION_PAGE_ID,
  CI_ICON_ENVIRONMENT_PROPERTY,
  CI_ICON_INSTANCE_NAME,
  CI_NODE_COMPONENT_ID,
  CI_NODE_INSTANCE_NAME,
  CI_NODE_PROPS,
  CI_VISUAL_TARGET_NAMES,
  CI_VARIABLE_COLLECTION_NAME,
  CONNECTOR_TEMPLATE_NAME,
  GITHUB_MAIN_BLOB_URL,
  HEADER_INSTANCE_NAME,
  HEADER_SECTION_TARGETS,
  METADATA_NAMESPACE,
} from "@figma-design-sync/project-config";
import {
  createCiVisualPlan,
  type CiVisualConnection,
  type CiVisualNode,
  type CiVisualOrientation,
  type CiVisualPlan,
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
const LIGHT_MODE_NAME = "Light";
const CONNECTOR_LABEL_FONT = { family: "Inter", style: "Medium" } as const;
const CONNECTOR_LABEL_CLEARANCE = 24;
const CONNECTOR_LABEL_COLLISION_GAP = 16;
const CONNECTOR_LABEL_MAX_TEXT_WIDTH = 280;

export class FigmaCiDocumentationSyncGateway implements CiDocumentationSyncGateway {
  async syncCiDocumentation(
    designModel: DesignModel,
    targetNames: string[],
    visualPlan?: CiVisualPlan
  ) {
    const unknownTargets = targetNames.filter((target) => !CI_VISUAL_TARGET_NAMES.includes(target as any));
    if (unknownTargets.length > 0) {
      throw new Error(`Unknown CI documentation target(s): ${unknownTargets.join(", ")}.`);
    }

    const plan = visualPlan ?? createCiVisualPlan(designModel);
    const page = await requirePage(CI_DOCUMENTATION_PAGE_ID);
    const nodeComponent = await requireComponent(CI_NODE_COMPONENT_ID);
    const modeCollection = await requireVariableCollection(CI_VARIABLE_COLLECTION_NAME);
    const outlineVariable = await requireOutlineColorVariable();
    const surfaceVariable = await requireColorVariable("md/sys/color/surface");
    const onSurfaceVariable = await requireColorVariable("md/sys/color/on-surface");
    const surfaceCollection = await requireVariableCollectionById(surfaceVariable.variableCollectionId);
    const surfaceModeId = requireModeId(surfaceCollection, LIGHT_MODE_NAME);
    const mutatedNodeIds: string[] = [];
    const createdCiNodes: string[] = [];
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
        surfaceVariable,
        onSurfaceVariable,
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
  surfaceVariable,
  onSurfaceVariable,
  createdCiNodes,
  createdCiConnectors,
  mutatedNodeIds
) {
  const groupsByModelId = new Map<string, GroupNode>();
  const groups: Array<{ plan: CiVisualNode; group: GroupNode }> = [];
  const connectorLabels: GroupNode[] = [];
  const connectorLabelsByModelId = new Map<string, GroupNode>();
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

  for (const edge of plan.connections) {
    const label = await createConnectorLabel(
      section,
      edge.label,
      edge.id,
      surfaceVariable,
      onSurfaceVariable
    );
    connectorLabels.push(label);
    connectorLabelsByModelId.set(edge.id, label);
    mutatedNodeIds.push(label.id);
  }

  await waitForStableCiLayout(groups.map(({ group }) => group));
  layoutNodeGroups(groups, plan.orientation, plan.connections, connectorLabelsByModelId);
  await waitForStableCiLayout(groups.map(({ group }) => group));
  const positionedLabels: GroupNode[] = [];
  const parallelConnections = parallelConnectionInfo(plan.connections);
  const connectorRecords: Array<{
    source: GroupNode;
    target: GroupNode;
    label: GroupNode;
    connector: ConnectorNode;
    magnets: { start: string; end: string };
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
    const label = connectorLabelsByModelId.get(edge.id);
    if (!label) throw new Error(`CI section '${plan.target}' has no label for connection '${edge.id}'.`);
    const connector = connectorTemplate.clone();
    connector.name = CI_CONNECTOR_NAME;
    connector.connectorLineType = "ELBOWED";
    connector.connectorStartStrokeCap = "NONE";
    connector.connectorEndStrokeCap = "ARROW_LINES";
    const magnets = ciConnectorMagnets(
      source,
      target,
      plan.orientation,
      parallelConnections.get(edge.id)
    );
    connector.connectorStart = { endpointNodeId: source.id, magnet: magnets.start };
    connector.connectorEnd = { endpointNodeId: target.id, magnet: magnets.end };
    connector.strokes = [boundColorPaint(outlineVariable)];
    connector.strokeWeight = 2;
    connector.setSharedPluginData(METADATA_NAMESPACE, CI_ROLE_KEY, ROLE_CONNECTOR);
    connector.setSharedPluginData(METADATA_NAMESPACE, CI_MODEL_ID_KEY, edge.id);
    section.insertChild(0, connector);
    await clearNativeConnectorLabel(connector);
    connectorRecords.push({ source, target, label, connector, magnets });
    createdCiConnectors.push(connector.id);
    mutatedNodeIds.push(connector.id);
  }

  await waitForStableCiLayout(connectorRecords.map(({ connector }) => connector));
  for (const { source, target, label, connector, magnets } of connectorRecords) {
    const connectorBounds = connector.absoluteBoundingBox;
    const sectionBounds = section.absoluteBoundingBox;
    const position = centersLabelOnConnector(magnets) && connectorBounds && sectionBounds
      ? connectorBoundsLabelPosition(
          connectorBounds,
          sectionBounds,
          label.width,
          label.height
        )
      : connectorLabelPosition(
          source,
          target,
          label.width,
          label.height,
          groups.map((item) => item.group),
          positionedLabels,
          magnets
        );
    label.x = position.x;
    label.y = position.y;
    positionedLabels.push(label);
  }

  resizeChildSection(
    section,
    [...groups.map((item) => item.group), ...connectorLabels],
    mutatedNodeIds
  );
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
  const icon = requireSingleNestedInstance(instance, CI_ICON_INSTANCE_NAME);
  setComponentVariantProperty(icon, CI_ICON_ENVIRONMENT_PROPERTY, nodePlan.environment);
  const properties = ciNodePropertyValues(nodePlan);
  setComponentBooleanProperty(instance, CI_NODE_PROPS.showSteps, properties.showSteps);
  setComponentBooleanProperty(instance, CI_NODE_PROPS.showSource, properties.showSource);
  setComponentBooleanProperty(instance, CI_NODE_PROPS.showRuntime, properties.showRuntime);
  setComponentTextProperty(instance, CI_NODE_PROPS.name, properties.name);
  setComponentTextProperty(instance, CI_NODE_PROPS.description, properties.description);
  setComponentTextProperty(instance, CI_NODE_PROPS.steps, properties.steps);
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
  return {
    name: nodePlan.name,
    description: nodePlan.description,
    steps: nodePlan.steps || "",
    source: nodePlan.source,
    runtimePlatform: nodePlan.runtime?.platform || "",
    runtimeService: nodePlan.runtime?.service || "",
    runtimeStartup: nodePlan.runtime?.startup || "",
    runtimeIdentity: nodePlan.runtime?.identity || "",
    showSteps: Boolean(nodePlan.steps),
    showSource: true,
    showRuntime: Boolean(nodePlan.runtime),
  };
}

function layoutNodeGroups(
  groups: Array<{ plan: CiVisualNode; group: GroupNode }>,
  orientation: CiVisualOrientation,
  connections: CiVisualConnection[],
  labelsByModelId: Map<string, GroupNode>
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
  labelsByModelId: Map<string, GroupNode>,
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

async function clearNativeConnectorLabel(connector) {
  const currentFontName = connector.text.fontName;
  const fontName = currentFontName === figma.mixed ||
      !currentFontName.family?.trim() ||
      !currentFontName.style?.trim()
    ? { family: "Poppins", style: "Regular" }
    : currentFontName;
  await figma.loadFontAsync(fontName);
  connector.text.fontName = fontName;
  connector.text.characters = "";
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

async function createConnectorLabel(
  section,
  label,
  modelId,
  surfaceVariable,
  onSurfaceVariable
) {
  await figma.loadFontAsync(CONNECTOR_LABEL_FONT);
  const text = figma.createText();
  text.name = "Label";
  text.fontName = CONNECTOR_LABEL_FONT;
  text.fontSize = 16;
  text.lineHeight = { unit: "PERCENT", value: 150 };
  text.textAlignHorizontal = "CENTER";
  text.textAutoResize = "WIDTH_AND_HEIGHT";
  text.characters = label;
  if (text.width > CONNECTOR_LABEL_MAX_TEXT_WIDTH) {
    text.textAutoResize = "HEIGHT";
    text.resize(CONNECTOR_LABEL_MAX_TEXT_WIDTH, text.height);
  }

  const background = figma.createRectangle();
  background.name = "Background";
  background.resize(text.width + 24, text.height + 16);
  background.cornerRadius = 4;
  background.strokes = [];

  const layers = appendConnectorLabelLayers(section, background, text);
  background.fills = [
    boundColorPaint(surfaceVariable, resolveColorForConsumer(surfaceVariable, background)),
  ];
  text.fills = [boundColorPaint(onSurfaceVariable, resolveColorForConsumer(onSurfaceVariable, text))];
  background.x = 0;
  background.y = 0;
  text.x = 12;
  text.y = 8;

  const group = figma.group(layers, section);
  group.name = CI_CONNECTOR_LABEL_NAME;
  group.setSharedPluginData(METADATA_NAMESPACE, CI_ROLE_KEY, ROLE_CONNECTOR);
  group.setSharedPluginData(METADATA_NAMESPACE, CI_MODEL_ID_KEY, modelId);
  return group;
}

export function connectorLabelLayers<B, T>(background: B, text: T): Array<B | T> {
  return [background, text];
}

export function appendConnectorLabelLayers<B, T>(
  section: { appendChild(node: B | T): void },
  background: B,
  text: T
): Array<B | T> {
  const layers = connectorLabelLayers(background, text);
  for (const layer of layers) section.appendChild(layer);
  return layers;
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

export function connectorLabelPosition(
  source,
  target,
  labelWidth,
  labelHeight,
  obstacles: Array<{ x: number; y: number; width: number; height: number }> = [],
  occupied: Array<{ x: number; y: number; width: number; height: number }> = [],
  route?: { start: string; end: string }
) {
  const sourceRight = source.x + source.width;
  const sourceBottom = source.y + source.height;
  const targetRight = target.x + target.width;
  const targetBottom = target.y + target.height;
  let centerX = (source.x + source.width / 2 + target.x + target.width / 2) / 2;
  let centerY = (source.y + source.height / 2 + target.y + target.height / 2) / 2;

  if (target.y >= sourceBottom) {
    centerY = (sourceBottom + target.y) / 2;
  } else if (source.y >= targetBottom) {
    centerY = (targetBottom + source.y) / 2;
  } else if (target.x >= sourceRight) {
    centerX = (sourceRight + target.x) / 2;
  } else if (source.x >= targetRight) {
    centerX = (targetRight + source.x) / 2;
  }

  const preferred = {
    x: centerX - labelWidth / 2,
    y: centerY - labelHeight / 2,
  };
  const sharedCenterX = (source.x + source.width / 2 + target.x + target.width / 2) / 2;
  const sharedCenterY = (source.y + source.height / 2 + target.y + target.height / 2) / 2;
  const above = [];
  const below = [];
  const beside = [];
  for (let level = 0; level < 6; level++) {
    const offset = level * (labelHeight + CONNECTOR_LABEL_COLLISION_GAP);
    above.push({
      x: sharedCenterX - labelWidth / 2,
      y: Math.min(source.y, target.y) - labelHeight - CONNECTOR_LABEL_CLEARANCE - offset,
    });
    below.push({
      x: sharedCenterX - labelWidth / 2,
      y: Math.max(sourceBottom, targetBottom) + CONNECTOR_LABEL_CLEARANCE + offset,
    });
    if (level > 0) {
      const horizontalOffset = level * (labelWidth + CONNECTOR_LABEL_COLLISION_GAP);
      beside.push(
        { x: preferred.x - horizontalOffset, y: preferred.y },
        { x: preferred.x + horizontalOffset, y: preferred.y }
      );
    }
  }
  const isHorizontalReturn = source.x > target.x &&
    source.y < targetBottom && sourceBottom > target.y;
  const hasVerticalGap = target.y >= sourceBottom || source.y >= targetBottom;
  const right = {
    x: Math.max(sourceRight, targetRight) + CONNECTOR_LABEL_CLEARANCE,
    y: sharedCenterY - labelHeight / 2,
  };
  const left = {
    x: Math.min(source.x, target.x) - labelWidth - CONNECTOR_LABEL_CLEARANCE,
    y: sharedCenterY - labelHeight / 2,
  };
  let candidates;
  if (route?.start === "TOP" && route.end === "TOP") {
    candidates = [...above, ...below, ...beside, preferred, right, left];
  } else if (route?.start === "BOTTOM" && route.end === "BOTTOM") {
    candidates = [...below, ...above, ...beside, preferred, right, left];
  } else if (route?.start === "RIGHT" && route.end === "RIGHT") {
    candidates = [right, left, ...beside, preferred, ...above, ...below];
  } else if (route?.start === "LEFT" && route.end === "LEFT") {
    candidates = [left, right, ...beside, preferred, ...above, ...below];
  } else {
    candidates = [
      ...(isHorizontalReturn ? [] : [preferred]),
      ...(hasVerticalGap ? beside : []),
      ...(isHorizontalReturn ? below : above),
      ...(isHorizontalReturn ? above : below),
      ...(hasVerticalGap ? [] : beside),
      right,
      left,
    ];
  }
  const collisionBounds = [...obstacles, ...occupied];
  return candidates.find((candidate) =>
    candidate.x >= 0 &&
    candidate.y >= 0 &&
    collisionBounds.every((bounds) => !rectanglesOverlap(
      candidate,
      { width: labelWidth, height: labelHeight },
      bounds,
      CONNECTOR_LABEL_COLLISION_GAP
    ))
  ) || preferred;
}

export function connectorBoundsLabelPosition(
  connectorBounds: { x: number; y: number; width: number; height: number },
  parentBounds: { x: number; y: number },
  labelWidth: number,
  labelHeight: number
) {
  return {
    x: connectorBounds.x - parentBounds.x + connectorBounds.width / 2 - labelWidth / 2,
    y: connectorBounds.y - parentBounds.y + connectorBounds.height / 2 - labelHeight / 2,
  };
}

function centersLabelOnConnector(route: { start: string; end: string }) {
  return route.start === route.end && (route.start === "LEFT" || route.start === "RIGHT");
}

function rectanglesOverlap(position, size, bounds, gap) {
  return position.x < bounds.x + bounds.width + gap &&
    position.x + size.width + gap > bounds.x &&
    position.y < bounds.y + bounds.height + gap &&
    position.y + size.height + gap > bounds.y;
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
