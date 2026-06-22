import { CATALOG_TREE_TARGETS } from "../config/figma-config";
import { flattenCatalogNodes, requireUniqueLabels } from "../domain/catalog/flatten-catalog-nodes";
import type { DesignModel } from "../domain/design-model";
import type { CatalogTreeSyncGateway } from "../ports/sync-gateways";
import { collectTreeConnectors, createTreeConnector, hasConnector } from "./figma-connector-gateway";
import { requireSection } from "./figma-node-gateway";
import {
  collectTreeNodeInstancesByLabel,
  createMissingTreeNode,
  updateLibraryTreeNode,
  updatePluginTreeNode,
} from "./figma-tree-node-gateway";

export class FigmaCatalogTreeSyncGateway implements CatalogTreeSyncGateway {
  async syncCatalogTrees(designModel: DesignModel) {
  const updatedCatalogNodes = [];
  const createdCatalogNodes = [];
  const createdCatalogConnectors = [];
  const removedCatalogNodes = [];
  const removedCatalogConnectors = [];
  const mutatedNodeIds = [];
  const componentCache = new Map();

  for (const target of CATALOG_TREE_TARGETS) {
    const modelNodes = target.nodes(designModel);
    if (!Array.isArray(modelNodes)) {
      throw new Error(`designModel.content.catalogs.${target.name} is required for catalog tree sync.`);
    }

    const expectedNodes = flattenCatalogNodes(modelNodes, target.type);
    requireUniqueLabels(target, expectedNodes);

    const section = await requireSection(target.sectionNodeId);
    const instancesByLabel = collectTreeNodeInstancesByLabel(section, target.type);
    let connectors = collectTreeConnectors(section);
    const disconnectedConnectors = connectors.filter((connector) =>
      !connector.connectorStart?.endpointNodeId || !connector.connectorEnd?.endpointNodeId
    );
    for (const connector of disconnectedConnectors) {
      removedCatalogConnectors.push(`${target.name}/${connector.id}`);
      connector.remove();
    }
    connectors = connectors.filter((connector) => !disconnectedConnectors.includes(connector));

    for (const node of expectedNodes) {
      if (instancesByLabel.has(node.label)) continue;

      const instance = await createMissingTreeNode(
        target,
        section,
        node,
        instancesByLabel,
        componentCache,
        mutatedNodeIds
      );
      instancesByLabel.set(node.label, instance);
      createdCatalogNodes.push(`${target.name}/${node.path.join("/")}`);
    }

    for (const node of expectedNodes) {
      const instance = instancesByLabel.get(node.label);
      if (node.type === "Library") {
        await updateLibraryTreeNode(instance, node, mutatedNodeIds);
      } else {
        await updatePluginTreeNode(instance, node, mutatedNodeIds, target);
      }
      updatedCatalogNodes.push(`${target.name}/${node.path.join("/")}`);
    }

    for (const node of expectedNodes.filter((candidate) => candidate.parentPath.length > 0)) {
      const parentLabel = node.parentPath[node.parentPath.length - 1];
      const parentInstance = instancesByLabel.get(parentLabel);
      const childInstance = instancesByLabel.get(node.label);

      if (!parentInstance || !childInstance) {
        throw new Error(`Cannot create connector for ${target.name}/${node.path.join("/")}: parent or child is missing.`);
      }

      if (hasConnector(connectors, parentInstance.id, childInstance.id)) {
        continue;
      }

      const connector = createTreeConnector(section, parentInstance, childInstance);
      connectors.push(connector);
      mutatedNodeIds.push(connector.id);
      createdCatalogConnectors.push(`${target.name}/${node.parentPath.join("/")} -> ${node.path.join("/")}`);
    }

    const staleResult = removeStaleCatalogNodes(target, expectedNodes, instancesByLabel, connectors);
    removedCatalogNodes.push(...staleResult.removedCatalogNodes);
    removedCatalogConnectors.push(...staleResult.removedCatalogConnectors);
    connectors = staleResult.connectors;

    layoutCatalogTreeNodes(section, expectedNodes, instancesByLabel, mutatedNodeIds);
    resizeSectionsToFit(section, [...instancesByLabel.values()], mutatedNodeIds);
  }

  return {
    updatedCatalogNodes,
    createdCatalogNodes,
    createdCatalogConnectors,
    removedCatalogNodes,
    removedCatalogConnectors,
    mutatedNodeIds,
  };
  }
}

function removeStaleCatalogNodes(target, expectedNodes, instancesByLabel, connectors) {
  const expectedLabels = new Set(expectedNodes.map((node) => node.label));
  const staleInstances = [...instancesByLabel.entries()]
    .filter(([label]) => !expectedLabels.has(label));
  const staleInstanceIds = new Set(staleInstances.map(([, instance]) => instance.id));
  const removedCatalogNodes = [];
  const removedCatalogConnectors = [];

  for (const connector of connectors) {
    if (
      staleInstanceIds.has(connector.connectorStart?.endpointNodeId) ||
      staleInstanceIds.has(connector.connectorEnd?.endpointNodeId)
    ) {
      removedCatalogConnectors.push(`${target.name}/${connector.id}`);
      connector.remove();
    }
  }

  for (const [label, instance] of staleInstances) {
    removedCatalogNodes.push(`${target.name}/${label}`);
    instance.remove();
    instancesByLabel.delete(label);
  }

  return {
    removedCatalogNodes,
    removedCatalogConnectors,
    connectors: connectors.filter((connector) =>
      !staleInstanceIds.has(connector.connectorStart?.endpointNodeId) &&
        !staleInstanceIds.has(connector.connectorEnd?.endpointNodeId)
    ),
  };
}

function layoutCatalogTreeNodes(section, nodes, instancesByLabel, mutatedNodeIds) {
  const nodesByPath = new Map(nodes.map((node) => [pathKey(node.path), node]));
  const childrenByParentPath = new Map();

  for (const node of nodes) {
    const parentKey = pathKey(node.parentPath);
    childrenByParentPath.set(
      parentKey,
      [...(childrenByParentPath.get(parentKey) || []), node]
    );
  }

  const roots = childrenByParentPath.get(pathKey([])) || [];
  const containers = new Map();

  for (const root of roots) {
    const rootInstance = instancesByLabel.get(root.label);
    const container = rootInstance?.parent?.type === "SECTION"
      ? rootInstance.parent
      : section;
    containers.set(
      container.id,
      [...(containers.get(container.id) || []), root]
    );
  }

  for (const containerRoots of containers.values()) {
    let nextX = CATALOG_TREE_LAYOUT_PADDING;
    for (const root of containerRoots) {
      const layout = layoutCatalogSubtree(
        root,
        nodesByPath,
        childrenByParentPath,
        instancesByLabel,
        nextX,
        CATALOG_TREE_LAYOUT_PADDING
      );
      applyCatalogPlacements(layout.placements, mutatedNodeIds);
      nextX = layout.maxX + CATALOG_TREE_SIBLING_GAP;
    }
  }
}

function layoutCatalogSubtree(
  node,
  nodesByPath,
  childrenByParentPath,
  instancesByLabel,
  x,
  y
) {
  const instance = instancesByLabel.get(node.label);
  if (!instance) {
    throw new Error(`Cannot layout catalog node '${node.path.join("/")}' because its Figma instance is missing.`);
  }

  const children = (childrenByParentPath.get(pathKey(node.path)) || [])
    .map((child) => nodesByPath.get(pathKey(child.path)))
    .filter(Boolean);
  const placements = new Map();

  if (children.length === 0) {
    placements.set(instance.id, { instance, x, y });
    return {
      placements,
      minX: x,
      maxX: x + instance.width,
      nextX: x + instance.width + CATALOG_TREE_SIBLING_GAP,
    };
  }

  let childX = x;
  const childY = y + instance.height + CATALOG_TREE_PARENT_CHILD_GAP;
  let minX = Number.POSITIVE_INFINITY;
  let maxX = Number.NEGATIVE_INFINITY;
  let firstChildCenter;
  let lastChildCenter;

  for (const child of children) {
    const childLayout = layoutCatalogSubtree(
      child,
      nodesByPath,
      childrenByParentPath,
      instancesByLabel,
      childX,
      childY
    );
    for (const [id, placement] of childLayout.placements) {
      placements.set(id, placement);
    }
    minX = Math.min(minX, childLayout.minX);
    maxX = Math.max(maxX, childLayout.maxX);
    const childInstance = instancesByLabel.get(child.label);
    const childPlacement = childLayout.placements.get(childInstance.id);
    const childCenter = childPlacement.x + childInstance.width / 2;
    firstChildCenter = firstChildCenter ?? childCenter;
    lastChildCenter = childCenter;
    childX = childLayout.nextX;
  }

  let parentX = ((firstChildCenter + lastChildCenter) / 2) - instance.width / 2;
  placements.set(instance.id, { instance, x: parentX, y });
  minX = Math.min(minX, parentX);
  maxX = Math.max(maxX, parentX + instance.width);

  if (minX < x) {
    const offset = x - minX;
    for (const placement of placements.values()) {
      placement.x += offset;
    }
    minX += offset;
    maxX += offset;
  }

  return {
    placements,
    minX,
    maxX,
    nextX: maxX + CATALOG_TREE_SIBLING_GAP,
  };
}

function applyCatalogPlacements(placements, mutatedNodeIds) {
  for (const placement of placements.values()) {
    placement.instance.x = placement.x;
    placement.instance.y = placement.y;
    mutatedNodeIds.push(placement.instance.id);
  }
}

function resizeSectionsToFit(section, nodes, mutatedNodeIds) {
  const nodesBySection = new Map();
  for (const node of nodes) {
    const parentSection = node.parent?.type === "SECTION" ? node.parent : section;
    nodesBySection.set(
      parentSection.id,
      {
        section: parentSection,
        nodes: [...(nodesBySection.get(parentSection.id)?.nodes || []), node],
      }
    );
  }

  for (const { section: targetSection, nodes: sectionNodes } of nodesBySection.values()) {
    resizeSectionToFit(targetSection, sectionNodes, mutatedNodeIds);
  }

  resizeSectionToFit(section, section.children.filter((child) => child.visible !== false), mutatedNodeIds);
}

function resizeSectionToFit(section, nodes, mutatedNodeIds) {
  if (nodes.length === 0) return;

  const maxRight = Math.max(...nodes.map((node) => node.x + node.width));
  const maxBottom = Math.max(...nodes.map((node) => node.y + node.height));
  section.resizeWithoutConstraints(
    Math.max(section.width, maxRight + 100),
    Math.max(section.height, maxBottom + 100)
  );
  mutatedNodeIds.push(section.id);
}

function pathKey(path) {
  return path.join("\u0000");
}

const CATALOG_TREE_LAYOUT_PADDING = 100;
const CATALOG_TREE_SIBLING_GAP = 120;
const CATALOG_TREE_PARENT_CHILD_GAP = 128;
