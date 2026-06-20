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
    const connectors = collectTreeConnectors(section);

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
        await updatePluginTreeNode(instance, node, mutatedNodeIds);
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
  }

  return {
    updatedCatalogNodes,
    createdCatalogNodes,
    createdCatalogConnectors,
    mutatedNodeIds,
  };
  }
}
