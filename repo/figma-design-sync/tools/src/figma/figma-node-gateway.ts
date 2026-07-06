import type { CatalogTreeType } from "../domain/design-model";

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

export async function requireComponent(nodeId) {
  const node = await figma.getNodeByIdAsync(nodeId);
  if (!node || node.type !== "COMPONENT") {
    throw new Error(`Expected '${nodeId}' to be a COMPONENT.`);
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

export function getComponentPropertyValue(instance, propertyName) {
  return instance.componentProperties?.[propertyName]?.value;
}

export function removeSectionFill(section, mutatedNodeIds) {
  section.fills = [];
  mutatedNodeIds.push(section.id);
}

export function resizeNodeToFit(node, children, mutatedNodeIds, padding = 100) {
  const visibleChildren = children.filter((child) => child && child.visible !== false);
  if (visibleChildren.length === 0) return;

  const contentChildren = node.type === "SECTION"
    ? visibleChildren.filter((child) => !(child.type === "INSTANCE" && child.name === ".Header"))
    : visibleChildren;
  const boundsChildren = contentChildren.length > 0 ? contentChildren : visibleChildren;
  const maxRight = Math.max(...boundsChildren.map((child) => child.x + child.width));
  const maxBottom = Math.max(...boundsChildren.map((child) => child.y + child.height));
  node.resizeWithoutConstraints(
    Math.max(1, maxRight + padding),
    Math.max(1, maxBottom + padding)
  );
  mutatedNodeIds.push(node.id);

  if (node.type === "SECTION") {
    resizeDirectHeadersToSectionWidth(node, mutatedNodeIds);
  }
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
    header.resizeWithoutConstraints(section.width, header.height);
    mutatedNodeIds.push(header.id);
  }
}
