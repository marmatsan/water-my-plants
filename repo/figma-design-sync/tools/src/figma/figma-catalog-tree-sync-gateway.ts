import { CATALOG_TREE_TARGETS, METADATA_NAMESPACE } from "../config/figma-config";
import { flattenCatalogNodes, requireUniqueLabels } from "../domain/catalog/flatten-catalog-nodes";
import type { DesignModel } from "../domain/design-model";
import type { CatalogTreeSyncGateway, CatalogTreeSyncOptions } from "../ports/sync-gateways";
import {
  collectTreeConnectors,
  connectorReferencesAnyNode,
  createTreeConnector,
  ensureTreeConnectorContainer,
  findTreeConnector,
  hasConnector,
  removeTreeNodeWithGroup,
  syncTreeConnector,
  syncTreeNodeGroup,
  TREE_CONNECTOR_EDGE_PLUGIN_DATA_KEY,
  treeNodeLayoutNode,
} from "./figma-connector-gateway";
import {
  lockOnlyRootSection,
  removeCatalogTreeSectionFills,
  requireSection,
  resizeAncestorSectionsToFit,
  resizeNodeToFit,
  stackAncestorSectionSiblingsWithGap,
  stackDescendantSectionsWithGap,
  unlockSectionTreeForMutation,
} from "./figma-node-gateway";
import {
  collectTreeNodeInstancesByLabel,
  createMissingTreeNode,
  updateLibraryTreeNode,
  updatePluginTreeNode,
} from "./figma-tree-node-gateway";

export class FigmaCatalogTreeSyncGateway implements CatalogTreeSyncGateway {
  async syncCatalogTrees(designModel: DesignModel, options: CatalogTreeSyncOptions = {}) {
  const updatedCatalogNodes = [];
  const createdCatalogNodes = [];
  const createdCatalogConnectors = [];
  const removedCatalogNodes = [];
  const removedCatalogConnectors = [];
  const mutatedNodeIds = [];
  const componentCache = new Map();
  const targetNames = new Set(options.targetNames || CATALOG_TREE_TARGETS.map((target) => target.name));
  const targets = CATALOG_TREE_TARGETS.filter((target) =>
    targetNames.has(target.name) || (target.aliases || []).some((alias) => targetNames.has(alias))
  );

  {
    const knownTargetNames = new Set(
      CATALOG_TREE_TARGETS.flatMap((target) => [target.name, ...(target.aliases || [])])
    );
    const unknownTargetNames = [...targetNames].filter((targetName) => !knownTargetNames.has(targetName));
    if (unknownTargetNames.length > 0) {
      throw new Error(`Unknown catalog tree target(s): ${unknownTargetNames.join(", ")}.`);
    }
  }

  for (const target of targets) {
    const modelNodes = target.nodes(designModel);
    if (!Array.isArray(modelNodes)) {
      throw new Error(`designModel.content.catalogs.${target.name} is required for catalog tree sync.`);
    }

    const rootFilter = options.rootFilters?.[target.name];
    const isPartialRootSync = Boolean(rootFilter && rootFilter.length > 0);
    const scopedModelNodes = filterModelRoots(target, modelNodes, rootFilter);
    const scopedRootLabels = scopedModelNodes.map((node) => rootLabel(target, node));
    const expectedNodes = flattenCatalogNodes(scopedModelNodes, target.type);
    requireUniqueLabels(target, expectedNodes);

    const sectionNodeId = options.sectionNodeOverrides?.[target.name] || target.sectionNodeId;
    const section = await requireSection(sectionNodeId);
    unlockSectionTreeForMutation(section, mutatedNodeIds);
    const instancesByLabel = collectTreeNodeInstancesByLabel(section, target.type);
    let connectors = collectTreeConnectors(section);

    if (!isPartialRootSync) {
      const disconnectedConnectors = connectors.filter((connector) =>
        !connector.getSharedPluginData?.(METADATA_NAMESPACE, TREE_CONNECTOR_EDGE_PLUGIN_DATA_KEY) &&
          (!connector.connectorStart?.endpointNodeId || !connector.connectorEnd?.endpointNodeId)
      );
      for (const connector of disconnectedConnectors) {
        removedCatalogConnectors.push(`${target.name}/${connector.id}`);
        connector.remove();
      }
      connectors = connectors.filter((connector) => !disconnectedConnectors.includes(connector));
    }

    const partialScope = isPartialRootSync
      ? buildPartialCatalogSyncScope({
          expectedNodes,
          rootLabels: scopedRootLabels,
          instancesByLabel,
          connectors,
        })
      : null;

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

    const staleResult = removeStaleCatalogNodes(
      target,
      expectedNodes,
      instancesByLabel,
      connectors,
      partialScope?.labels
    );
    removedCatalogNodes.push(...staleResult.removedCatalogNodes);
    removedCatalogConnectors.push(...staleResult.removedCatalogConnectors);
    connectors = staleResult.connectors;

    connectors = createMissingCatalogConnectors(
      target,
      section,
      expectedNodes,
      instancesByLabel,
      connectors,
      createdCatalogConnectors,
      mutatedNodeIds
    );

    layoutCatalogTreeNodes(section, expectedNodes, instancesByLabel, mutatedNodeIds, {
      scopeLabels: partialScope?.labels,
      preserveRootPosition: isPartialRootSync,
    });
    syncCatalogConnectors(section, expectedNodes, instancesByLabel, connectors, mutatedNodeIds);
    const nodesToResize = partialScope
      ? [...partialScope.labels]
          .map((label) => instancesByLabel.get(label))
          .filter(Boolean)
      : [...instancesByLabel.values()];
    resizeSectionsToFit(section, nodesToResize, mutatedNodeIds);
    stackDescendantSectionsWithGap(section, mutatedNodeIds);
    stackAncestorSectionSiblingsWithGap(section, mutatedNodeIds);
    resizeAncestorSectionsToFit(section, mutatedNodeIds);
    removeCatalogTreeSectionFills(section, mutatedNodeIds);
    lockOnlyRootSection(section, mutatedNodeIds);
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

export function filterModelRoots(target, modelNodes, rootFilter) {
  if (!rootFilter || rootFilter.length === 0) {
    return modelNodes;
  }

  const requestedRoots = new Set(rootFilter);
  const filteredNodes = modelNodes.filter((node) => requestedRoots.has(rootLabel(target, node)));
  const missingRoots = [...requestedRoots].filter((root) =>
    !filteredNodes.some((node) => rootLabel(target, node) === root)
  );

  if (missingRoots.length > 0) {
    const availableRoots = modelNodes.map((node) => rootLabel(target, node)).join(", ");
    throw new Error(
      `${target.name} does not contain root filter(s): ${missingRoots.join(", ")}. ` +
        `Available roots: ${availableRoots || "<none>"}.`
    );
  }

  return filteredNodes;
}

function rootLabel(target, node) {
  return target.type === "Library" ? node.group : node.id;
}

export function buildPartialCatalogSyncScope({ expectedNodes, rootLabels, instancesByLabel, connectors }) {
  const labels = new Set<string>(expectedNodes.map((node) => node.label));
  const instanceIdByLabel = new Map();
  const labelByInstanceId = new Map();

  for (const [label, instance] of instancesByLabel.entries()) {
    instanceIdByLabel.set(label, instance.id);
    labelByInstanceId.set(instance.id, label);
  }

  const childrenByParentInstanceId = new Map();
  for (const connector of connectors) {
    const edgeKey = connector.getSharedPluginData?.(METADATA_NAMESPACE, TREE_CONNECTOR_EDGE_PLUGIN_DATA_KEY);
    if (!edgeKey) continue;

    const [parentInstanceId, childInstanceId] = edgeKey.split("->");
    if (!labelByInstanceId.has(parentInstanceId) || !labelByInstanceId.has(childInstanceId)) {
      continue;
    }

    childrenByParentInstanceId.set(
      parentInstanceId,
      [...(childrenByParentInstanceId.get(parentInstanceId) || []), childInstanceId]
    );
  }

  for (const rootLabel of rootLabels) {
    labels.add(rootLabel);
    const rootInstanceId = instanceIdByLabel.get(rootLabel);
    if (!rootInstanceId) continue;

    const pendingInstanceIds = [rootInstanceId];
    const visitedInstanceIds = new Set();
    while (pendingInstanceIds.length > 0) {
      const instanceId = pendingInstanceIds.pop();
      if (!instanceId || visitedInstanceIds.has(instanceId)) continue;

      visitedInstanceIds.add(instanceId);
      const label = labelByInstanceId.get(instanceId);
      if (label) labels.add(label);

      for (const childInstanceId of childrenByParentInstanceId.get(instanceId) || []) {
        pendingInstanceIds.push(childInstanceId);
      }
    }
  }

  return { labels };
}

function errorMessage(error) {
  return error instanceof Error ? error.message : String(error);
}

function createMissingCatalogConnectors(
  target,
  section,
  expectedNodes,
  instancesByLabel,
  connectors,
  createdCatalogConnectors,
  mutatedNodeIds
) {
  const syncedConnectors = [...connectors];

  for (const node of expectedNodes.filter((candidate) => candidate.parentPath.length > 0)) {
    const parentLabel = node.parentPath[node.parentPath.length - 1];
    const parentInstance = instancesByLabel.get(parentLabel);
    const childInstance = instancesByLabel.get(node.label);

    if (!parentInstance || !childInstance) {
      throw new Error(`Cannot create connector for ${target.name}/${node.path.join("/")}: parent or child is missing.`);
    }

    if (hasConnector(syncedConnectors, parentInstance.id, childInstance.id)) {
      continue;
    }

    let connector;
    try {
      connector = createTreeConnector(section, parentInstance, childInstance);
    } catch (error) {
      throw new Error(
        `Cannot create connector for ${target.name}/${node.parentPath.join("/")} -> ${node.path.join("/")}: ` +
          `parent=${parentInstance.id} child=${childInstance.id}. ${errorMessage(error)}`
      );
    }
    syncedConnectors.push(connector);
    mutatedNodeIds.push(connector.id);
    createdCatalogConnectors.push(`${target.name}/${node.parentPath.join("/")} -> ${node.path.join("/")}`);
  }

  return syncedConnectors;
}

export function removeStaleCatalogNodes(target, expectedNodes, instancesByLabel, connectors, scopeLabels) {
  const expectedLabels = new Set(expectedNodes.map((node) => node.label));
  const staleInstances = [...instancesByLabel.entries()]
    .filter(([label]) => !expectedLabels.has(label))
    .filter(([label]) => !scopeLabels || scopeLabels.has(label));
  const staleInstanceIds = new Set(staleInstances.map(([, instance]) => instance.id));
  const removedCatalogNodes = [];
  const removedCatalogConnectors = [];
  const removedConnectorIds = new Set();

  for (const connector of connectors) {
    if (connectorReferencesAnyNode(connector, staleInstanceIds)) {
      removedCatalogConnectors.push(`${target.name}/${connector.id}`);
      removedConnectorIds.add(connector.id);
      connector.remove();
    }
  }

  for (const [label, instance] of staleInstances) {
    removedCatalogNodes.push(`${target.name}/${label}`);
    removeTreeNodeWithGroup(instance);
    instancesByLabel.delete(label);
  }

  return {
    removedCatalogNodes,
    removedCatalogConnectors,
    connectors: connectors.filter((connector) => !removedConnectorIds.has(connector.id)),
  };
}

function layoutCatalogTreeNodes(
  section,
  nodes,
  instancesByLabel,
  mutatedNodeIds,
  options: { scopeLabels?: Set<string>; preserveRootPosition?: boolean } = {}
) {
  const instancesToGroup = [...instancesByLabel.entries()]
    .filter(([label]) => !options.scopeLabels || options.scopeLabels.has(label))
    .map(([, instance]) => instance);

  for (const instance of instancesToGroup) {
    const group = syncTreeNodeGroup(instance);
    mutatedNodeIds.push(group.id);
  }

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
    const rootLayoutNode = rootInstance ? treeNodeLayoutNode(rootInstance) : null;
    const container = rootLayoutNode?.parent?.type === "SECTION"
      ? rootLayoutNode.parent
      : section;
    containers.set(
      container.id,
      [...(containers.get(container.id) || []), root]
    );
  }

  for (const containerRoots of containers.values()) {
    let nextX = CATALOG_TREE_LAYOUT_PADDING;
    for (const root of containerRoots) {
      const rootInstance = instancesByLabel.get(root.label);
      const rootLayoutNode = rootInstance ? treeNodeLayoutNode(rootInstance) : null;
      const startX = options.preserveRootPosition && rootLayoutNode
        ? rootLayoutNode.x
        : nextX;
      const startY = options.preserveRootPosition && rootLayoutNode
        ? rootLayoutNode.y
        : CATALOG_TREE_LAYOUT_PADDING;
      const layout = layoutCatalogSubtree(
        root,
        nodesByPath,
        childrenByParentPath,
        instancesByLabel,
        startX,
        startY
      );
      if (options.preserveRootPosition && rootInstance && rootLayoutNode) {
        const offset = alignCatalogPlacementsToCurrentRoot(layout.placements, rootInstance, rootLayoutNode);
        layout.minX += offset.x;
        layout.maxX += offset.x;
        layout.nextX += offset.x;
      }
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
  const layoutNode = treeNodeLayoutNode(instance);

  const children = (childrenByParentPath.get(pathKey(node.path)) || [])
    .map((child) => nodesByPath.get(pathKey(child.path)))
    .filter(Boolean);
  const placements = new Map();

  if (children.length === 0) {
    placements.set(instance.id, { instance, layoutNode, x, y });
    return {
      placements,
      minX: x,
      maxX: x + layoutNode.width,
      nextX: x + layoutNode.width + CATALOG_TREE_SIBLING_GAP,
    };
  }

  let childX = x;
  const childY = y + layoutNode.height + CATALOG_TREE_PARENT_CHILD_GAP;
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
    const childLayoutNode = treeNodeLayoutNode(childInstance);
    const childPlacement = childLayout.placements.get(childInstance.id);
    const childCenter = childPlacement.x + childLayoutNode.width / 2;
    firstChildCenter = firstChildCenter ?? childCenter;
    lastChildCenter = childCenter;
    childX = childLayout.nextX;
  }

  let parentX = ((firstChildCenter + lastChildCenter) / 2) - layoutNode.width / 2;
  placements.set(instance.id, { instance, layoutNode, x: parentX, y });
  minX = Math.min(minX, parentX);
  maxX = Math.max(maxX, parentX + layoutNode.width);

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
    placement.layoutNode.x = placement.x;
    placement.layoutNode.y = placement.y;
    mutatedNodeIds.push(placement.layoutNode.id);
  }
}

function alignCatalogPlacementsToCurrentRoot(placements, rootInstance, rootLayoutNode) {
  const rootPlacement = placements.get(rootInstance.id);
  if (!rootPlacement) return { x: 0, y: 0 };

  const offsetX = rootLayoutNode.x - rootPlacement.x;
  const offsetY = rootLayoutNode.y - rootPlacement.y;
  for (const placement of placements.values()) {
    placement.x += offsetX;
    placement.y += offsetY;
  }
  return { x: offsetX, y: offsetY };
}

function syncCatalogConnectors(section, nodes, instancesByLabel, connectors, mutatedNodeIds) {
  for (const node of nodes.filter((candidate) => candidate.parentPath.length > 0)) {
    const parentLabel = node.parentPath[node.parentPath.length - 1];
    const parentInstance = instancesByLabel.get(parentLabel);
    const childInstance = instancesByLabel.get(node.label);

    if (!parentInstance || !childInstance) {
      continue;
    }

    const connector = findTreeConnector(connectors, parentInstance.id, childInstance.id);
    if (!connector) {
      continue;
    }

    ensureTreeConnectorContainer(section, connector, childInstance);
    syncTreeConnector(connector, parentInstance, childInstance);
    ensureTreeConnectorContainer(section, connector, childInstance);
    mutatedNodeIds.push(connector.id);
  }
}

function resizeSectionsToFit(section, nodes, mutatedNodeIds) {
  const nodesBySection = new Map();
  for (const node of nodes) {
    const layoutNode = treeNodeLayoutNode(node);
    const parentSection = layoutNode.parent?.type === "SECTION" ? layoutNode.parent : section;
    nodesBySection.set(
      parentSection.id,
      {
        section: parentSection,
        nodes: [...(nodesBySection.get(parentSection.id)?.nodes || []), layoutNode],
      }
    );
  }

  for (const { section: targetSection, nodes: sectionNodes } of nodesBySection.values()) {
    resizeSectionToFit(targetSection, sectionNodes, mutatedNodeIds);
  }

  resizeSectionToFit(section, section.children.filter((child) => child.visible !== false), mutatedNodeIds);
}

function resizeSectionToFit(section, nodes, mutatedNodeIds) {
  resizeNodeToFit(section, nodes, mutatedNodeIds);
}

function pathKey(path) {
  return path.join("\u0000");
}

const CATALOG_TREE_LAYOUT_PADDING = 100;
const CATALOG_TREE_SIBLING_GAP = 120;
const CATALOG_TREE_PARENT_CHILD_GAP = 128;
