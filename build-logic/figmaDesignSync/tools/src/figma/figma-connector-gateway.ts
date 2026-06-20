import { CONNECTOR_TEMPLATE_NAME } from "../config/figma-config";

export function collectTreeConnectors(section) {
  return section.findAllWithCriteria({ types: ["CONNECTOR"] })
    .filter((connector) => connector.name === CONNECTOR_TEMPLATE_NAME);
}

export function hasConnector(connectors, parentNodeId, childNodeId) {
  return connectors.some((connector) =>
    connector.connectorStart?.endpointNodeId === parentNodeId &&
      connector.connectorEnd?.endpointNodeId === childNodeId
  );
}

export function createTreeConnector(section, parentInstance, childInstance) {
  const container = childInstance.parent && childInstance.parent.type === "SECTION"
    ? childInstance.parent
    : section;
  const template = findConnectorTemplate(container, section);
  const connector = template.clone();

  container.appendChild(connector);
  connector.name = CONNECTOR_TEMPLATE_NAME;
  connector.connectorStart = {
    endpointNodeId: parentInstance.id,
    magnet: "BOTTOM",
  };
  connector.connectorEnd = {
    endpointNodeId: childInstance.id,
    magnet: "TOP",
  };

  return connector;
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
