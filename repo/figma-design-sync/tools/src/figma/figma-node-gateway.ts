import type { CatalogTreeType } from "../domain/design-model";
import {
  OUTLINE_COLOR_VARIABLE_NAME,
  PARENT_SECTION_NODE_IDS,
  PARENT_SECTION_SIBLING_GAP,
  SECTION_SIBLING_GAP,
} from "../config/figma-config";

const SECTION_STROKE_WEIGHT = 2;
const SECTION_STROKE_ALIGN = "INSIDE";

export async function requireVariableCollection(nameOrNames) {
  const names = Array.isArray(nameOrNames) ? nameOrNames : [nameOrNames];
  const collections = await figma.variables.getLocalVariableCollectionsAsync();
  const collection = collections.find((candidate) => names.includes(candidate.name));
  if (!collection) {
    throw new Error(`Variable collection '${names.join("' or '")}' was not found.`);
  }
  return collection;
}

export function requireModeId(collection, modeName) {
  const mode = collection.modes.find((candidate) => candidate.name === modeName);
  if (!mode) {
    throw new Error(`Mode '${modeName}' was not found in '${collection.name}'.`);
  }
  return mode.modeId;
}

export async function loadVariablesByVersionKey(collection) {
  const variables = new Map();

  for (const variableId of collection.variableIds) {
    const variable = await figma.variables.getVariableByIdAsync(variableId);
    if (!variable) continue;

    const versionKey = variable.name.split("/").at(-1);
    variables.set(versionKey, variable);
  }

  return variables;
}

export async function requireOutlineColorVariable() {
  const variables = await figma.variables.getLocalVariablesAsync("COLOR");
  const variable = variables.find((candidate) => candidate.name === OUTLINE_COLOR_VARIABLE_NAME);
  if (!variable) {
    throw new Error(`Color variable '${OUTLINE_COLOR_VARIABLE_NAME}' was not found.`);
  }
  return variable;
}

export async function requireComponent(nodeId) {
  const node = await figma.getNodeByIdAsync(nodeId);
  if (!node || node.type !== "COMPONENT") {
    throw new Error(`Expected '${nodeId}' to be a COMPONENT.`);
  }
  return node;
}

export async function requireComponentSet(nodeId) {
  const node = await figma.getNodeByIdAsync(nodeId);
  if (!node || node.type !== "COMPONENT_SET") {
    throw new Error(`Expected '${nodeId}' to be a COMPONENT_SET.`);
  }
  return node;
}

export async function requireFrame(nodeId) {
  const node = await figma.getNodeByIdAsync(nodeId);
  if (!node || node.type !== "FRAME") {
    throw new Error(`Expected '${nodeId}' to be a FRAME.`);
  }
  return node;
}

export async function requireFrameOrSection(nodeId) {
  const node = await figma.getNodeByIdAsync(nodeId);
  if (!node || (node.type !== "FRAME" && node.type !== "SECTION")) {
    throw new Error(`Expected '${nodeId}' to be a FRAME or SECTION.`);
  }
  return node;
}

export async function requirePage(nodeId) {
  const node = await figma.getNodeByIdAsync(nodeId);
  if (!node || node.type !== "PAGE") {
    throw new Error(`Expected '${nodeId}' to be a PAGE.`);
  }
  return node;
}

export async function requireSection(nodeId) {
  const node = await figma.getNodeByIdAsync(nodeId);
  if (!node || node.type !== "SECTION") {
    throw new Error(`Expected '${nodeId}' to be a SECTION.`);
  }
  return node;
}

export async function findSection(nodeId) {
  const node = await figma.getNodeByIdAsync(nodeId);
  if (!node) return null;
  if (node.type !== "SECTION") {
    throw new Error(`Expected '${nodeId}' to be a SECTION.`);
  }
  return node;
}

export function getComponentPropertyValue(instance, propertyName) {
  return instance.componentProperties?.[propertyName]?.value;
}

export function removeSectionFill(section, mutatedNodeIds) {
  if (!("fills" in section)) return;
  if (Array.isArray(section.fills) && section.fills.length === 0) return;

  section.fills = [];
  mutatedNodeIds.push(section.id);
}

export function removeCatalogTreeSectionFills(section, mutatedNodeIds) {
  const sections = [
    section,
    ...section.findAllWithCriteria({ types: ["SECTION"] }),
  ];

  for (const candidate of sections) {
    if (PARENT_SECTION_NODE_IDS.includes(candidate.id)) continue;

    removeSectionFill(candidate, mutatedNodeIds);
  }
}

export function applySectionStrokeContractTree(section, outlineVariable, mutatedNodeIds) {
  const sections = [
    section,
    ...section.findAllWithCriteria({ types: ["SECTION"] }),
  ];

  for (const candidate of sections) {
    applySectionStrokeContract(candidate, outlineVariable, mutatedNodeIds);
  }
}

export function applyAncestorSectionStrokeContract(section, outlineVariable, mutatedNodeIds) {
  let current = section.parent;

  while (current && current.type === "SECTION") {
    applySectionStrokeContract(current, outlineVariable, mutatedNodeIds);
    current = current.parent;
  }
}

export function sectionStrokeContractSatisfied(section, outlineVariableId) {
  if (hasDirectHeader(section)) {
    return Array.isArray(section.strokes) && section.strokes.length === 0;
  }
  if (section.strokeAlign !== SECTION_STROKE_ALIGN || section.strokeWeight !== SECTION_STROKE_WEIGHT) {
    return false;
  }
  if (!Array.isArray(section.strokes) || section.strokes.length !== 1) return false;

  const stroke = section.strokes[0];
  return stroke.type === "SOLID" && stroke.boundVariables?.color?.id === outlineVariableId;
}

function applySectionStrokeContract(section, outlineVariable, mutatedNodeIds) {
  if (sectionStrokeContractSatisfied(section, outlineVariable.id)) return;

  if (hasDirectHeader(section)) {
    section.strokes = [];
    mutatedNodeIds.push(section.id);
    return;
  }

  const existingSolidStroke = section.strokes.find((stroke) => stroke.type === "SOLID");
  const stroke = figma.variables.setBoundVariableForPaint(
    {
      type: "SOLID",
      color: existingSolidStroke?.color || { r: 0, g: 0, b: 0 },
      opacity: 1,
      visible: true,
      blendMode: "NORMAL",
    },
    "color",
    outlineVariable
  );
  section.strokes = [stroke];
  section.strokeAlign = SECTION_STROKE_ALIGN;
  section.strokeWeight = SECTION_STROKE_WEIGHT;
  mutatedNodeIds.push(section.id);
}

function hasDirectHeader(section) {
  return section.children.some((child) => child.type === "INSTANCE" && child.name === ".Header");
}

export function resizeNodeToFit(node, children, mutatedNodeIds, padding = 100) {
  const visibleChildren = children.filter((child) => child && child.visible !== false);
  if (visibleChildren.length === 0) return;

  const contentChildren = node.type === "SECTION"
    ? visibleChildren.filter((child) => !(child.type === "INSTANCE" && child.name === ".Header"))
    : visibleChildren;
  const boundsChildren = contentChildren.length > 0 ? contentChildren : visibleChildren;
  const directHeaderHugWidth = node.type === "SECTION" ? maxDirectHeaderHugWidth(node) : 0;
  const maxRight = Math.max(...boundsChildren.map((child) => child.x + child.width));
  const maxBottom = Math.max(...boundsChildren.map((child) => child.y + child.height));
  node.resizeWithoutConstraints(
    Math.max(1, maxRight + padding, directHeaderHugWidth),
    Math.max(1, maxBottom + padding)
  );
  mutatedNodeIds.push(node.id);

  if (node.type === "SECTION") {
    resizeDirectHeadersToSectionWidth(node, mutatedNodeIds);
  }
}

export function stackChildSectionsFromPadding(parent, mutatedNodeIds, gap = SECTION_SIBLING_GAP, padding = 100) {
  stackDirectChildSectionsWithGap(parent, mutatedNodeIds, gap, padding, padding);
}

export function resizeAncestorSectionsToFit(node, mutatedNodeIds, padding = 100) {
  let current = node.parent;

  while (current && current.type === "SECTION") {
    resizeNodeToFit(
      current,
      current.children.filter((child) => child.visible !== false),
      mutatedNodeIds,
      padding
    );
    current = current.parent;
  }
}

export function stackDescendantSectionsWithGap(section, mutatedNodeIds, gap = SECTION_SIBLING_GAP) {
  for (const childSection of directChildSections(section)) {
    stackDescendantSectionsWithGap(childSection, mutatedNodeIds, gap);
  }

  stackDirectChildSectionsWithGap(section, mutatedNodeIds, gap);
  resizeNodeToFit(
    section,
    section.children.filter((child) => child.visible !== false),
    mutatedNodeIds
  );
}

export function stackAncestorSectionSiblingsWithGap(section, mutatedNodeIds, gap = SECTION_SIBLING_GAP) {
  let current = section.parent;

  while (current && current.type === "SECTION") {
    stackDirectChildSectionsWithGap(current, mutatedNodeIds, gap);
    resizeNodeToFit(
      current,
      current.children.filter((child) => child.visible !== false),
      mutatedNodeIds
    );
    current = current.parent;
  }

  stackConfiguredPageSectionsWithGap(section, mutatedNodeIds);
}

export function unlockSectionTreeForMutation(section, mutatedNodeIds) {
  const root = rootSection(section);
  setNodeLocked(root, false, mutatedNodeIds);
  setDescendantsLocked(root, false, mutatedNodeIds);
}

export function lockOnlyRootSection(section, mutatedNodeIds) {
  const root = rootSection(section);
  setDescendantsLocked(root, false, mutatedNodeIds);
  setNodeLocked(root, true, mutatedNodeIds);
}

export async function requireTreeNodeComponent(type: CatalogTreeType, componentIds, componentCache) {
  if (componentCache.has(type)) {
    return componentCache.get(type);
  }

  const componentId = componentIds[type];
  const component = await requireComponent(componentId);
  componentCache.set(type, component);
  return component;
}

function resizeDirectHeadersToSectionWidth(section, mutatedNodeIds) {
  const headers = section.children
    .filter((child) => child.type === "INSTANCE" && child.name === ".Header");

  for (const header of headers) {
    header.x = 0;
    if (header.layoutMode === "VERTICAL" || header.layoutMode === "HORIZONTAL") {
      header.counterAxisSizingMode = "FIXED";
      header.primaryAxisSizingMode = "AUTO";
    }
    header.resizeWithoutConstraints(section.width, header.height);
    mutatedNodeIds.push(header.id);
  }
}

function maxDirectHeaderHugWidth(section) {
  return Math.max(
    0,
    ...section.children
      .filter((child) => child.type === "INSTANCE" && child.name === ".Header")
      .map((header) => directHeaderHugWidth(header))
  );
}

function directHeaderHugWidth(header) {
  if (header.layoutMode !== "VERTICAL" && header.layoutMode !== "HORIZONTAL") {
    return header.width;
  }

  const originalCounterAxisSizingMode = header.counterAxisSizingMode;
  header.counterAxisSizingMode = "AUTO";
  const hugWidth = header.width;
  header.counterAxisSizingMode = originalCounterAxisSizingMode;
  return hugWidth;
}

function stackConfiguredPageSectionsWithGap(section, mutatedNodeIds, gap = PARENT_SECTION_SIBLING_GAP) {
  const root = rootSection(section);
  if (!PARENT_SECTION_NODE_IDS.includes(root.id)) return;
  if (root.parent?.type !== "PAGE") return;

  const sections = PARENT_SECTION_NODE_IDS
    .map((sectionId) => root.parent.children.find((child) => child.id === sectionId))
    .filter((child) => child?.type === "SECTION" && child.visible !== false);

  if (sections.length < 2) return;

  let nextX = sections[0].x;
  for (const siblingSection of sections) {
    if (Math.abs(siblingSection.x - nextX) > 0.01) {
      siblingSection.x = nextX;
      mutatedNodeIds.push(siblingSection.id);
    }
    nextX = siblingSection.x + siblingSection.width + gap;
  }
}

export function stackDirectChildSectionsWithGap(
  parent,
  mutatedNodeIds,
  gap = SECTION_SIBLING_GAP,
  startY = firstSectionStartY(parent),
  startX = 100
) {
  const sections = directChildSections(parent)
    .sort((first, second) => first.y - second.y || first.x - second.x);

  if (sections.length === 0) return;

  let nextY = startY;
  for (const section of sections) {
    let positionChanged = false;
    if (Math.abs(section.x - startX) > 0.01) {
      section.x = startX;
      positionChanged = true;
    }
    if (Math.abs(section.y - nextY) > 0.01) {
      section.y = nextY;
      positionChanged = true;
    }
    if (positionChanged) mutatedNodeIds.push(section.id);
    nextY = section.y + section.height + gap;
  }
}

function firstSectionStartY(parent) {
  const nonSectionVisibleChildren = parent.children
    .filter((child) => child.type !== "SECTION" && child.visible !== false);

  if (nonSectionVisibleChildren.length === 0) {
    return 100;
  }

  return Math.max(
    100,
    Math.max(...nonSectionVisibleChildren.map((child) => child.y + child.height + SECTION_SIBLING_GAP))
  );
}

function directChildSections(parent) {
  return parent.children
    .filter((child) => child.type === "SECTION" && child.visible !== false);
}

function rootSection(section) {
  let current = section;
  while (current.parent?.type === "SECTION") {
    current = current.parent;
  }
  return current;
}

function setDescendantsLocked(node, locked, mutatedNodeIds) {
  if (!("findAll" in node)) return;

  for (const descendant of node.findAll(() => true)) {
    setNodeLocked(descendant, locked, mutatedNodeIds);
  }
}

function setNodeLocked(node, locked, mutatedNodeIds) {
  if (!("locked" in node) || node.locked === locked) return;

  node.locked = locked;
  mutatedNodeIds.push(node.id);
}
