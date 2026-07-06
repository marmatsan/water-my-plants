import { CONNECTOR_TEMPLATE_NAME, METADATA_NAMESPACE } from "../config/figma-config";

export function collectTreeConnectors(section) {
  return section.findAllWithCriteria({ types: ["CONNECTOR"] })
    .filter((connector) => connector.name === CONNECTOR_TEMPLATE_NAME);
}

export function hasConnector(connectors, parentNodeId, childNodeId) {
  const edgeKey = treeConnectorEdgeKey(parentNodeId, childNodeId);
  return connectors.some((connector) =>
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
  const container = childInstance.parent && childInstance.parent.type === "SECTION"
    ? childInstance.parent
    : section;
  const template = findConnectorTemplate(container, section);
  const connector = template.clone();

  parentInstance.visible = true;
  childInstance.visible = true;
  container.appendChild(connector);
  connector.visible = true;
  connector.name = CONNECTOR_TEMPLATE_NAME;
  connector.setSharedPluginData(
    METADATA_NAMESPACE,
    TREE_CONNECTOR_EDGE_PLUGIN_DATA_KEY,
    treeConnectorEdgeKey(parentInstance.id, childInstance.id)
  );
  connector.connectorStart = {
    position: connectorPosition(parentInstance, "BOTTOM"),
  };
  connector.connectorEnd = {
    position: connectorPosition(childInstance, "TOP"),
  };

  return connector;
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

const TREE_CONNECTOR_EDGE_PLUGIN_DATA_KEY = "treeConnectorEdge";
