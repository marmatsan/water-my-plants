import {
  TREE_NODE_COMPONENT_IDS,
  TREE_NODE_PROPS,
} from "../config/figma-config";
import { updateLibraryArtifactConsumerModules, updateLibraryBundleConsumerModules, updatePluginUsageBlocks } from "./figma-consumer-modules-gateway";
import type { FlattenedCatalogNode } from "../domain/design-model";
import { getComponentPropertyValue, requireTreeNodeComponent } from "./figma-node-gateway";
import {
  libraryArtifactNames,
  libraryArtifacts,
  libraryArtifactVersions,
  libraryBundles,
  sortedUnique,
} from "../domain/catalog/library-catalog-entries";
import { countNamedTextNodes, updateNamedTextNodes } from "./figma-text-gateway";
import { syncTreeNodeGroup, treeNodeLayoutNode } from "./figma-connector-gateway";

export function collectTreeNodeInstancesByLabel(section, type) {
  const instances = section.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((instance) => isTreeNodeInstance(instance, type));
  const instancesByLabel = new Map();

  for (const instance of instances) {
    const label = type === "Library"
      ? getComponentPropertyValue(instance, TREE_NODE_PROPS.libraryGroup)
      : getComponentPropertyValue(instance, TREE_NODE_PROPS.pluginId);

    if (!label || label === "Library group" || label === "Plugin ID") continue;
    if (instancesByLabel.has(label)) {
      throw new Error(`Duplicate '${label}' ${type} tree node instances were found in section '${section.name}'.`);
    }
    instancesByLabel.set(label, instance);
  }

  return instancesByLabel;
}

export async function createMissingTreeNode(
  target,
  section,
  node: FlattenedCatalogNode,
  instancesByLabel,
  componentCache,
  mutatedNodeIds
) {
  const container = requireTreeNodeContainer(section, node);
  const template = findTreeNodeTemplate(section, node);
  const instance = template
    ? template.clone()
    : (await requireTreeNodeComponent(node.type, TREE_NODE_COMPONENT_IDS, componentCache)).createInstance();

  container.appendChild(instance);
  instance.visible = true;
  instance.name = ".tree node";

  const position = nextTreeNodePosition(container, node, instancesByLabel);
  instance.x = position.x;
  instance.y = position.y;

  if (node.type === "Library") {
    await updateLibraryTreeNode(instance, node, mutatedNodeIds);
  } else {
    await updatePluginTreeNode(instance, node, mutatedNodeIds, target);
  }

  const group = syncTreeNodeGroup(instance);
  mutatedNodeIds.push(group.id);
  mutatedNodeIds.push(instance.id);
  return instance;
}

export async function updateLibraryTreeNode(instance, node, mutatedNodeIds) {
  const horizontalCenter = treeNodeHorizontalCenter(instance);
  const artifacts = libraryArtifacts(node.entries);
  const bundles = libraryBundles(node.entries);
  const artifactNames = artifacts.map((artifact) => artifact.name);
  const artifactVersions = libraryArtifactVersions(node.entries);
  const requiredByModules = sortedUnique([
    ...artifacts.flatMap((artifact) => artifact.requiredByModules),
    ...bundles.flatMap((bundle) => bundle.requiredByModules),
  ]);
  const hasCatalogEntries = artifacts.some((artifact) => artifact.isCatalogEntry === true) ||
    bundles.some((bundle) => bundle.isCatalogEntry === true);

  instance.setProperties({
    [TREE_NODE_PROPS.libraryGroup]: node.label,
    [TREE_NODE_PROPS.showArtifacts]: node.artifactsVisible && artifactNames.length > 0,
    [TREE_NODE_PROPS.showConsumerModule]: requiredByModules.length > 0 || hasCatalogEntries,
    [TREE_NODE_PROPS.type]: "Library",
  });
  mutatedNodeIds.push(instance.id);

  await updateNamedTextNodes(instance, "Library group", [node.label], mutatedNodeIds);
  await updateNamedTextNodes(instance, "artifact name", artifactNames, mutatedNodeIds, { allowExtra: true });
  await updateNamedTextNodes(instance, "artifact version", artifactVersions, mutatedNodeIds, { allowExtra: true });
  await updateLibraryArtifactConsumerModules(instance, artifacts, mutatedNodeIds);
  await updateLibraryBundleConsumerModules(instance, bundles, mutatedNodeIds);

  if (artifactNames.length === 0 && requiredByModules.length === 0 && !hasCatalogEntries) {
    resizeBareTreeNodeToFitLabel(instance, "Library group", mutatedNodeIds);
  }

  restoreTreeNodeHorizontalCenter(instance, horizontalCenter, mutatedNodeIds);
}

export async function updatePluginTreeNode(instance, node, mutatedNodeIds, target?) {
  const horizontalCenter = treeNodeHorizontalCenter(instance);
  const versionValue = node.version?.visible && node.version?.value
    ? node.version.value
    : "Plugin version";
  const appliedToModules = sortedUnique(node.appliedToModules || []);
  const providedByConventionPlugins = node.providedByConventionPlugins || [];
  const effectiveAppliedToModules = sortedUnique([
    ...appliedToModules,
    ...providedByConventionPlugins.flatMap((usage) => usage.requiredByModules || []),
  ]);
  const isGradleConventionPlugin = target?.gradleConventionPluginNodes === true && node.children.length === 0;
  const isUnusedCatalogEntry = (isPluginCatalogEntry(node) || isGradleConventionPlugin) &&
    effectiveAppliedToModules.length === 0 &&
    providedByConventionPlugins.length === 0;

  instance.setProperties({
    [TREE_NODE_PROPS.pluginId]: node.label,
    [TREE_NODE_PROPS.pluginVersion]: versionValue,
    [TREE_NODE_PROPS.showPluginVersion]: node.version?.visible === true && Boolean(node.version?.value),
    [TREE_NODE_PROPS.showConsumerModule]: effectiveAppliedToModules.length > 0 || providedByConventionPlugins.length > 0 || isUnusedCatalogEntry,
    [TREE_NODE_PROPS.showIsGradleConventionPlugin]: isGradleConventionPlugin,
    [TREE_NODE_PROPS.type]: "Plugin",
  });
  mutatedNodeIds.push(instance.id);

  await updatePluginUsageBlocks(
    instance,
    effectiveAppliedToModules,
    providedByConventionPlugins,
    isUnusedCatalogEntry,
    mutatedNodeIds
  );

  if (effectiveAppliedToModules.length === 0 &&
    providedByConventionPlugins.length === 0 &&
    !isUnusedCatalogEntry &&
    !isGradleConventionPlugin &&
    !hasVisiblePluginVersion(node)
  ) {
    resizeBareTreeNodeToFitLabel(instance, "Plugin ID", mutatedNodeIds);
  }

  restoreTreeNodeHorizontalCenter(instance, horizontalCenter, mutatedNodeIds);
}

function hasVisiblePluginVersion(node: FlattenedCatalogNode) {
  return node.version?.visible === true && Boolean(node.version?.value);
}

function isPluginCatalogEntry(node: FlattenedCatalogNode) {
  return node.version !== null && node.version !== undefined;
}

function resizeBareTreeNodeToFitLabel(instance, labelName, mutatedNodeIds) {
  const label = findVisibleTextNode(instance, labelName);
  if (!label) return;

  const targetWidth = Math.ceil(label.width + TREE_NODE_LABEL_HORIZONTAL_PADDING);
  const targetHeight = Math.ceil(Math.max(instance.height, label.height + TREE_NODE_LABEL_VERTICAL_PADDING));
  if (Math.ceil(instance.width) === targetWidth && Math.ceil(instance.height) === targetHeight) return;

  instance.resizeWithoutConstraints(targetWidth, targetHeight);
  mutatedNodeIds.push(instance.id);
}

function treeNodeHorizontalCenter(instance) {
  const layoutNode = treeNodeLayoutNode(instance);
  return layoutNode.x + layoutNode.width / 2;
}

function restoreTreeNodeHorizontalCenter(instance, horizontalCenter, mutatedNodeIds) {
  const layoutNode = treeNodeLayoutNode(instance);
  const nextX = horizontalCenter - layoutNode.width / 2;
  if (Math.abs(layoutNode.x - nextX) <= 0.01) return;

  layoutNode.x = nextX;
  mutatedNodeIds.push(layoutNode.id);
}

function findVisibleTextNode(root, name) {
  return root.findAllWithCriteria({ types: ["TEXT"] })
    .find((textNode) => textNode.name === name && isVisibleInside(textNode, root));
}

function isVisibleInside(node, boundary) {
  let current = node;
  while (current && current.id !== boundary.id) {
    if (current.visible === false) return false;
    current = current.parent;
  }
  return boundary.visible !== false;
}

function requireTreeNodeContainer(section, node: FlattenedCatalogNode) {
  const rootLabel = node.path[0];
  const existingContainer = section.children.find((child) => child.type === "SECTION" && child.name === rootLabel);

  if (existingContainer) {
    return existingContainer;
  }

  if (node.path.length > 1) {
    throw new Error(`Cannot create ${node.path.join("/")}: root section '${rootLabel}' is missing.`);
  }

  const newSection = figma.createSection();
  newSection.name = rootLabel;
  section.appendChild(newSection);

  const position = nextTopLevelSectionPosition(section);
  newSection.x = position.x;
  newSection.y = position.y;
  newSection.resizeWithoutConstraints(1200, 900);

  return newSection;
}

function nextTopLevelSectionPosition(section) {
  const sections = section.children
    .filter((child) => child.type === "SECTION")
    .sort((first, second) => first.y - second.y || first.x - second.x);

  if (sections.length === 0) {
    return { x: 100, y: 100 };
  }

  const last = sections[sections.length - 1];
  return {
    x: last.x + last.width + 100,
    y: last.y,
  };
}

function findTreeNodeTemplate(section, node) {
  const candidates = section.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((instance) => isTreeNodeInstance(instance, node.type));

  if (node.type === "Plugin") {
    const showPluginVersion = node.version?.visible === true && Boolean(node.version?.value);
    return candidates.find((instance) =>
      getComponentPropertyValue(instance, TREE_NODE_PROPS.showPluginVersion) === showPluginVersion
    ) || candidates[0];
  }

  const artifactNameCount = libraryArtifactNames(node.entries).length;
  const artifactVersionCount = libraryArtifactVersions(node.entries).length;
  return candidates.find((instance) =>
    countNamedTextNodes(instance, "artifact name") === artifactNameCount &&
      countNamedTextNodes(instance, "artifact version") === artifactVersionCount
  ) || candidates.find((instance) =>
    getComponentPropertyValue(instance, TREE_NODE_PROPS.showArtifacts) === (node.artifactsVisible && artifactNameCount > 0)
  ) || candidates[0];
}

function isTreeNodeInstance(instance, type) {
  return instance.name === ".tree node" &&
    getComponentPropertyValue(instance, TREE_NODE_PROPS.type) === type;
}

function nextTreeNodePosition(container, node, instancesByLabel) {
  if (node.parentPath.length > 0) {
    const parentLabel = node.parentPath[node.parentPath.length - 1];
    const parentInstance = instancesByLabel.get(parentLabel);
    if (parentInstance) {
      const parentLayoutNode = treeNodeLayoutNode(parentInstance);
      const siblingNodes = [...instancesByLabel.values()]
        .map((instance) => treeNodeLayoutNode(instance))
        .filter((layoutNode) => layoutNode.parent?.id === container.id && layoutNode.id !== parentLayoutNode.id)
        .sort((first, second) => first.y - second.y || first.x - second.x);
      const existingSiblingRowY = siblingNodes[0]?.y;
      const lastSibling = siblingNodes[siblingNodes.length - 1];
      const x = lastSibling
        ? lastSibling.x + lastSibling.width + 120
        : parentLayoutNode.x;
      return {
        x,
        y: existingSiblingRowY ?? parentLayoutNode.y + parentLayoutNode.height + TREE_NODE_PARENT_CHILD_GAP,
      };
    }
  }

  const layoutNodes = [...instancesByLabel.values()]
    .map((instance) => treeNodeLayoutNode(instance))
    .filter((layoutNode) => layoutNode.parent?.id === container.id)
    .sort((first, second) => first.y - second.y || first.x - second.x);

  if (layoutNodes.length === 0) {
    return { x: 100, y: 100 };
  }

  const last = layoutNodes[layoutNodes.length - 1];
  return {
    x: last.x + last.width + 120,
    y: last.y,
  };
}

const TREE_NODE_PARENT_CHILD_GAP = 128;
const TREE_NODE_LABEL_HORIZONTAL_PADDING = 64;
const TREE_NODE_LABEL_VERTICAL_PADDING = 64;
