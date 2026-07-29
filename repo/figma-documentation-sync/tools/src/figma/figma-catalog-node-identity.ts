import {
  METADATA_NAMESPACE,
  TREE_NODE_PROPS,
} from "@figma-documentation-sync/project-config";
import { getComponentPropertyValue } from "./figma-node-gateway";

export function collectTreeNodeInstancesByPath(
  section,
  type,
  expectedNodes,
  traversalRoots = [section],
  mutatedNodeIds = []
) {
  const instances = traversalRoots.flatMap((root) =>
    root.findAllWithCriteria({ types: ["INSTANCE"] })
  )
    .filter((instance) => isTreeNodeInstance(instance, type));
  const instancesByPath = new Map();
  const unresolvedByLabel = new Map();
  const expectedPathsByLabel = new Map();

  for (const node of expectedNodes) {
    expectedPathsByLabel.set(
      node.label,
      [...(expectedPathsByLabel.get(node.label) || []), catalogNodePathKey(node.path)]
    );
  }

  for (const instance of instances) {
    const label = treeNodeLabel(instance, type);

    if (!label || label === "Library group" || label === "Plugin ID") continue;
    const storedPath = readTreeNodePath(instance);
    const storedKey = storedPath ? catalogNodePathKey(storedPath) : null;
    if (storedKey && !instancesByPath.has(storedKey)) {
      instancesByPath.set(storedKey, instance);
      continue;
    }

    unresolvedByLabel.set(
      label,
      [...(unresolvedByLabel.get(label) || []), instance]
    );
  }

  for (const [label, unresolvedInstances] of unresolvedByLabel.entries()) {
    const availablePaths = (expectedPathsByLabel.get(label) || [])
      .filter((path) => !instancesByPath.has(path));

    if (unresolvedInstances.length === 1 && availablePaths.length === 1) {
      const [instance] = unresolvedInstances;
      const [path] = availablePaths;
      instancesByPath.set(path, instance);
      syncTreeNodePath(instance, JSON.parse(path), mutatedNodeIds);
      continue;
    }

    for (const instance of unresolvedInstances) {
      instancesByPath.set(unresolvedTreeNodeKey(instance), instance);
    }
  }

  return instancesByPath;
}

export function catalogNodePathKey(path) {
  return JSON.stringify(path);
}

export function syncTreeNodePath(instance, path, mutatedNodeIds) {
  const serializedPath = catalogNodePathKey(path);
  if (instance.getSharedPluginData?.(METADATA_NAMESPACE, TREE_NODE_PATH_PLUGIN_DATA_KEY) === serializedPath) {
    return;
  }

  instance.setSharedPluginData(METADATA_NAMESPACE, TREE_NODE_PATH_PLUGIN_DATA_KEY, serializedPath);
  mutatedNodeIds.push(instance.id);
}

function treeNodeLabel(instance, type) {
  return type === "Library"
    ? getComponentPropertyValue(instance, TREE_NODE_PROPS.libraryGroup)
    : getComponentPropertyValue(instance, TREE_NODE_PROPS.pluginId);
}

function isTreeNodeInstance(instance, type) {
  return instance.name === ".tree node" &&
    getComponentPropertyValue(instance, TREE_NODE_PROPS.type) === type;
}

function readTreeNodePath(instance) {
  const value = instance.getSharedPluginData?.(METADATA_NAMESPACE, TREE_NODE_PATH_PLUGIN_DATA_KEY);
  if (!value) return null;

  try {
    const path = JSON.parse(value);
    return Array.isArray(path) && path.every((segment) => typeof segment === "string")
      ? path
      : null;
  } catch (_) {
    return null;
  }
}

function unresolvedTreeNodeKey(instance) {
  return `${UNRESOLVED_TREE_NODE_PREFIX}${instance.id}`;
}

export const TREE_NODE_PATH_PLUGIN_DATA_KEY = "treeNodePath";
const UNRESOLVED_TREE_NODE_PREFIX = "unresolved:";
