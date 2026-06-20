import type { CatalogTreeType } from "../domain/design-model";

export async function requireVariableCollection(name) {
  const collections = await figma.variables.getLocalVariableCollectionsAsync();
  const collection = collections.find((candidate) => candidate.name === name);
  if (!collection) {
    throw new Error(`Variable collection '${name}' was not found.`);
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

export async function requireTreeNodeComponent(type: CatalogTreeType, componentIds, componentCache) {
  if (componentCache.has(type)) {
    return componentCache.get(type);
  }

  const componentId = componentIds[type];
  const component = await requireComponent(componentId);
  componentCache.set(type, component);
  return component;
}
