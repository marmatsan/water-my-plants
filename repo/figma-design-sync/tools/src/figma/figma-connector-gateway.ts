import { CONNECTOR_TEMPLATE_NAME, METADATA_NAMESPACE } from "@figma-design-sync/project-config";

export function collectTreeConnectors(section, traversalRoots = [section]) {
  const scopedTraversal = traversalRoots.length !== 1 || traversalRoots[0].id !== section.id;
  const sectionNodeIds = new Set(traversalRoots.flatMap((root) => [
    root.id,
    ...root.findAll().map((node) => node.id),
  ]));
  const sectionConnectors = traversalRoots.flatMap((root) =>
    root.findAllWithCriteria({ types: ["CONNECTOR"] })
  ).filter((connector) => connector.name === CONNECTOR_TEMPLATE_NAME);
  const pageConnectors = !scopedTraversal && section.parent?.type === "PAGE"
    ? section.parent.findAllWithCriteria({ types: ["CONNECTOR"] })
      .filter((connector) => connector.name === CONNECTOR_TEMPLATE_NAME)
      .filter((connector) => connectorReferencesAnyNode(connector, sectionNodeIds))
    : [];

  return [...new Set([...sectionConnectors, ...pageConnectors])];
}

export function hasConnector(connectors, parentNodeId, childNodeId) {
  return Boolean(findTreeConnector(connectors, parentNodeId, childNodeId));
}

export function findTreeConnector(connectors, parentNodeId, childNodeId) {
  const edgeKey = treeConnectorEdgeKey(parentNodeId, childNodeId);
  return connectors.find((connector) =>
    connector.getSharedPluginData?.(METADATA_NAMESPACE, TREE_CONNECTOR_EDGE_PLUGIN_DATA_KEY) === edgeKey ||
      (
        connector.connectorStart?.endpointNodeId === parentNodeId &&
        connector.connectorEnd?.endpointNodeId === childNodeId
      )
  );
}

export function connectorReferencesAnyNode(connector, nodeIds) {
  const edgeKey = connector.getSharedPluginData?.(METADATA_NAMESPACE, TREE_CONNECTOR_EDGE_PLUGIN_DATA_KEY);
  if (edgeKey) {
    const [parentNodeId, childNodeId] = edgeKey.split("->");
    return nodeIds.has(parentNodeId) || nodeIds.has(childNodeId);
  }

  return nodeIds.has(connector.connectorStart?.endpointNodeId) ||
    nodeIds.has(connector.connectorEnd?.endpointNodeId);
}

export function createTreeConnector(section, parentInstance, childInstance) {
  const childGroup = syncTreeNodeGroup(childInstance);
  const container = childGroup.parent && childGroup.parent.type === "SECTION"
    ? childGroup.parent
    : section;
  const template = findConnectorTemplate(container, section);
  const connector = template.clone();

  parentInstance.visible = true;
  childInstance.visible = true;
  container.appendChild(connector);
  connector.visible = true;
  connector.name = CONNECTOR_TEMPLATE_NAME;
  syncTreeConnector(connector, parentInstance, childInstance);
  ensureTreeConnectorContainer(section, connector, childInstance);

  return connector;
}

export function ensureTreeConnectorContainer(section, connector, childInstance) {
  const childGroup = treeNodeLayoutNode(childInstance);
  const container = childGroup.parent && childGroup.parent.type === "SECTION"
    ? childGroup.parent
    : section;
  if (connector.parent?.id !== container.id) {
    container.appendChild(connector);
  }
}

export function syncTreeConnector(connector, parentInstance, childInstance) {
  connector.setSharedPluginData(
    METADATA_NAMESPACE,
    TREE_CONNECTOR_EDGE_PLUGIN_DATA_KEY,
    treeConnectorEdgeKey(parentInstance.id, childInstance.id)
  );

  const parentGroup = syncTreeNodeGroup(parentInstance);
  const childGroup = syncTreeNodeGroup(childInstance);

  try {
    syncTreeConnectorEndpoints(connector, parentGroup, childGroup);
  } catch (_) {
    syncTreeConnectorPositions(connector, parentInstance, childInstance);
  }
}

function syncTreeConnectorEndpoints(connector, parentGroup, childGroup) {
  const nextStart = {
    endpointNodeId: parentGroup.id,
    magnet: "BOTTOM",
  };
  const nextEnd = {
    endpointNodeId: childGroup.id,
    magnet: "TOP",
  };

  if (connector.connectorEnd?.endpointNodeId === parentGroup.id) {
    connector.connectorEnd = nextEnd;
    connector.connectorStart = nextStart;
    return;
  }

  if (connector.connectorStart?.endpointNodeId === childGroup.id) {
    connector.connectorStart = nextStart;
    connector.connectorEnd = nextEnd;
    return;
  }

  connector.connectorEnd = nextEnd;
  connector.connectorStart = nextStart;
}

export function syncTreeNodeGroup(instance) {
  const currentGroup = treeNodeGroup(instance);
  if (currentGroup) {
    syncTreeNodeGroupMetadata(currentGroup, instance);
    return currentGroup;
  }

  const container = instance.parent;
  if (!container || !("children" in container)) {
    return instance;
  }

  removeLegacyTreeConnectorAnchors(container, instance);
  const instanceIndex = container.children.indexOf(instance);
  const group = figma.group([instance], container, Math.max(0, instanceIndex));
  syncTreeNodeGroupMetadata(group, instance);
  return group;
}

export function treeNodeLayoutNode(instance) {
  return treeNodeGroup(instance) || instance;
}

export function removeTreeNodeWithGroup(instance) {
  treeNodeLayoutNode(instance).remove();
}

function treeNodeGroup(instance) {
  const parent = instance.parent;
  if (parent?.type === "GROUP" && parent.name === TREE_NODE_GROUP_NAME) {
    return parent;
  }

  return null;
}

function syncTreeNodeGroupMetadata(group, instance) {
  group.name = TREE_NODE_GROUP_NAME;
  group.visible = true;
  group.locked = false;
  group.setSharedPluginData(METADATA_NAMESPACE, TREE_NODE_GROUP_NODE_PLUGIN_DATA_KEY, instance.id);
}

function removeLegacyTreeConnectorAnchors(container, instance) {
  const anchors = container.findAllWithCriteria({ types: ["FRAME"] })
    .filter((node) =>
      node.name === LEGACY_TREE_CONNECTOR_ANCHOR_NAME &&
        node.getSharedPluginData?.(METADATA_NAMESPACE, LEGACY_TREE_CONNECTOR_ANCHOR_NODE_PLUGIN_DATA_KEY) === instance.id
    );
  for (const anchor of anchors) {
    anchor.remove();
  }
}

function syncTreeConnectorPositions(connector, parentInstance, childInstance) {
  connector.connectorStart = {
    position: connectorPosition(parentInstance, "BOTTOM"),
  };
  connector.connectorEnd = {
    position: connectorPosition(childInstance, "TOP"),
  };
}

function connectorPosition(node, magnet) {
  const bounds = absoluteBounds(node);
  const x = bounds.x + bounds.width / 2;
  const y = magnet === "BOTTOM"
    ? bounds.y + bounds.height
    : bounds.y;

  return { x, y };
}

function absoluteBounds(node) {
  if (node.absoluteBoundingBox) {
    return node.absoluteBoundingBox;
  }

  let x = node.x;
  let y = node.y;
  let parent = node.parent;

  while (parent && parent.type !== "PAGE") {
    x += parent.x || 0;
    y += parent.y || 0;
    parent = parent.parent;
  }

  return {
    x,
    y,
    width: node.width,
    height: node.height,
  };
}

function treeConnectorEdgeKey(parentInstanceId, childInstanceId) {
  return `${parentInstanceId}->${childInstanceId}`;
}

function findConnectorTemplate(container, section) {
  const containerTemplate = container.findAllWithCriteria({ types: ["CONNECTOR"] })
    .find((connector) => connector.name === CONNECTOR_TEMPLATE_NAME);
  if (containerTemplate) {
    return containerTemplate;
  }

  const sectionTemplate = collectTreeConnectors(section)[0];
  if (sectionTemplate) {
    return sectionTemplate;
  }

  throw new Error(`No '${CONNECTOR_TEMPLATE_NAME}' connector template was found in section '${section.name}'.`);
}

export const TREE_CONNECTOR_EDGE_PLUGIN_DATA_KEY = "treeConnectorEdge";
export const TREE_NODE_GROUP_NAME = ".tree node group";
export const TREE_NODE_GROUP_NODE_PLUGIN_DATA_KEY = "treeNodeGroupNode";
const LEGACY_TREE_CONNECTOR_ANCHOR_NAME = ".tree connector anchor";
const LEGACY_TREE_CONNECTOR_ANCHOR_NODE_PLUGIN_DATA_KEY = "treeConnectorAnchorNode";
