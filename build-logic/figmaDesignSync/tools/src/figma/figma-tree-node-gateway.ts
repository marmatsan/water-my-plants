import {
  TREE_NODE_COMPONENT_IDS,
  TREE_NODE_PROPS,
} from "../config/figma-config";
import { updateConsumerModuleInstances, updateLibraryArtifactConsumerModules, updateLibraryBundleConsumerModules } from "./figma-consumer-modules-gateway";
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

export function collectTreeNodeInstancesByLabel(section, type) {
  const instances = section.findAllWithCriteria({ types: ["INSTANCE"] })
    .filter((instance) => getComponentPropertyValue(instance, TREE_NODE_PROPS.type) === type);
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
  instance.name = ".tree node";

  const position = nextTreeNodePosition(container, node, instancesByLabel);
  instance.x = position.x;
  instance.y = position.y;

  if (node.type === "Library") {
    await updateLibraryTreeNode(instance, node, mutatedNodeIds);
  } else {
    await updatePluginTreeNode(instance, node, mutatedNodeIds, target);
  }

  mutatedNodeIds.push(instance.id);
  return instance;
}

export async function updateLibraryTreeNode(instance, node, mutatedNodeIds) {
  const artifacts = libraryArtifacts(node.entries);
  const bundles = libraryBundles(node.entries);
  const artifactNames = artifacts.map((artifact) => artifact.name);
  const artifactVersions = libraryArtifactVersions(node.entries);
  const requiredByModules = sortedUnique([
    ...artifacts.flatMap((artifact) => artifact.requiredByModules),
    ...bundles.flatMap((bundle) => bundle.requiredByModules),
  ]);

  instance.setProperties({
    [TREE_NODE_PROPS.libraryGroup]: node.label,
    [TREE_NODE_PROPS.showArtifacts]: node.artifactsVisible && artifactNames.length > 0,
    [TREE_NODE_PROPS.showConsumerModule]: requiredByModules.length > 0,
    [TREE_NODE_PROPS.type]: "Library",
  });
  mutatedNodeIds.push(instance.id);

  await updateNamedTextNodes(instance, "Library group", [node.label], mutatedNodeIds);
  await updateNamedTextNodes(instance, "artifact name", artifactNames, mutatedNodeIds);
  await updateNamedTextNodes(instance, "artifact version", artifactVersions, mutatedNodeIds);
  await updateLibraryArtifactConsumerModules(instance, artifacts, mutatedNodeIds);
  await updateLibraryBundleConsumerModules(instance, bundles, mutatedNodeIds);
}

export async function updatePluginTreeNode(instance, node, mutatedNodeIds, target?) {
  const versionValue = node.version?.visible && node.version?.value
    ? node.version.value
    : "Plugin version";
  const appliedToModules = sortedUnique(node.appliedToModules || []);
  const isGradleConventionPlugin = target?.gradleConventionPluginNodes === true && node.children.length === 0;

  instance.setProperties({
    [TREE_NODE_PROPS.pluginId]: node.label,
    [TREE_NODE_PROPS.pluginVersion]: versionValue,
    [TREE_NODE_PROPS.showPluginVersion]: node.version?.visible === true && Boolean(node.version?.value),
    [TREE_NODE_PROPS.showConsumerModule]: appliedToModules.length > 0,
    [TREE_NODE_PROPS.showIsGradleConventionPlugin]: isGradleConventionPlugin,
    [TREE_NODE_PROPS.type]: "Plugin",
  });
  mutatedNodeIds.push(instance.id);

  await updateConsumerModuleInstances(instance, "Applied by", appliedToModules, mutatedNodeIds);
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
    .filter((instance) => getComponentPropertyValue(instance, TREE_NODE_PROPS.type) === node.type);

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

function nextTreeNodePosition(container, node, instancesByLabel) {
  if (node.parentPath.length > 0) {
    const parentLabel = node.parentPath[node.parentPath.length - 1];
    const parentInstance = instancesByLabel.get(parentLabel);
    if (parentInstance) {
      const siblingInstances = container.children
        .filter((child) => child.type === "INSTANCE" && child.id !== parentInstance.id)
        .sort((first, second) => first.y - second.y || first.x - second.x);
      const existingSiblingRowY = siblingInstances[0]?.y;
      const lastSibling = siblingInstances[siblingInstances.length - 1];
      const x = lastSibling
        ? lastSibling.x + lastSibling.width + 120
        : parentInstance.x;
      return {
        x,
        y: existingSiblingRowY ?? parentInstance.y + parentInstance.height + TREE_NODE_PARENT_CHILD_GAP,
      };
    }
  }

  const instances = container.children
    .filter((child) => child.type === "INSTANCE")
    .sort((first, second) => first.y - second.y || first.x - second.x);

  if (instances.length === 0) {
    return { x: 100, y: 100 };
  }

  const last = instances[instances.length - 1];
  return {
    x: last.x + last.width + 120,
    y: last.y,
  };
}

const TREE_NODE_PARENT_CHILD_GAP = 128;
