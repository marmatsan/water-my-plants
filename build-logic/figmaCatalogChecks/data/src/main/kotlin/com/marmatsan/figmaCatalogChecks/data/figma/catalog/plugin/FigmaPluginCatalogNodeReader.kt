package com.marmatsan.figmaCatalogChecks.data.figma.catalog.plugin

import com.marmatsan.figmaCatalogChecks.data.figma.common.booleanProperty
import com.marmatsan.figmaCatalogChecks.data.figma.common.childIdsByParentId
import com.marmatsan.figmaCatalogChecks.data.figma.common.getValueOrEmpty
import com.marmatsan.figmaCatalogChecks.data.figma.common.parentByChildId
import com.marmatsan.figmaCatalogChecks.data.figma.common.renderedTextValues
import com.marmatsan.figmaCatalogChecks.data.figma.common.requiredTextProperty
import com.marmatsan.figmaCatalogChecks.data.figma.common.textProperty
import com.marmatsan.figmaCatalogChecks.data.figma.common.visibleDescendants
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.CatalogVersion
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogNode
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaPluginCatalogNodeReader {
    fun readRoots(rootSection: FigmaNode): List<PluginCatalogNode> {
        val treeNodes = rootSection.children
            .filter { node -> node.visible && node.isPluginTreeNode() }
            .associateBy(FigmaNode::id)

        if (treeNodes.isEmpty()) {
            error("Figma plugin root section '${rootSection.id}' contains no plugin tree nodes")
        }

        val parentByChildId = rootSection.children.parentByChildId(treeNodes)
        val childIdsByParentId = parentByChildId.childIdsByParentId()

        return treeNodes
            .values
            .filter { node -> node.id !in parentByChildId }
            .sortedWith(compareBy<FigmaNode>({ node -> node.pluginId() }, FigmaNode::id))
            .map { rootNode ->
                rootNode.toPluginCatalogNode(
                    treeNodes = treeNodes,
                    childIdsByParentId = childIdsByParentId,
                    visitedNodeIds = emptySet()
                )
            }
    }

    private fun FigmaNode.toPluginCatalogNode(
        treeNodes: Map<String, FigmaNode>,
        childIdsByParentId: Map<String, List<String>>,
        visitedNodeIds: Set<String>
    ): PluginCatalogNode {
        check(id !in visitedNodeIds) {
            "Cycle found in Figma plugin tree at node '$id'"
        }

        val nextVisitedNodeIds = visitedNodeIds + id

        return PluginCatalogNode(
            id = pluginId(),
            version = pluginVersion(),
            appliedToModules = readAppliedToModules(),
            children = childIdsByParentId
                .getValueOrEmpty(id)
                .mapNotNull(treeNodes::get)
                .sortedWith(compareBy<FigmaNode>({ node -> node.pluginId() }, FigmaNode::id))
                .map { child ->
                    child.toPluginCatalogNode(
                        treeNodes = treeNodes,
                        childIdsByParentId = childIdsByParentId,
                        visitedNodeIds = nextVisitedNodeIds
                    )
                }
        )
    }

    private fun FigmaNode.isPluginTreeNode(): Boolean =
        type == "INSTANCE" &&
            name.equals(".tree node", ignoreCase = true) &&
            textProperty(TYPE_PROPERTY) == PLUGIN_TYPE

    private fun FigmaNode.pluginId(): String =
        requiredTextProperty(PLUGIN_ID_PROPERTY)

    private fun FigmaNode.pluginVersion(): CatalogVersion? {
        val visible = booleanProperty(
            name = SHOW_PLUGIN_VERSION_PROPERTY,
            default = false
        )

        return if (visible) {
            CatalogVersion(requiredTextProperty(PLUGIN_VERSION_PROPERTY))
        } else {
            null
        }
    }

    private fun FigmaNode.readAppliedToModules(): List<String> =
        visibleDescendants()
            .filter { node -> node.visible && node.isModule() }
            .mapNotNull { module -> module.renderedTextValues().firstOrNull() }
            .distinct()
            .sorted()
            .toList()

    private fun FigmaNode.isModule(): Boolean =
        type == "INSTANCE" && name == MODULE_COMPONENT_NAME

    private companion object {
        const val TYPE_PROPERTY = "Type"
        const val PLUGIN_TYPE = "Plugin"
        const val PLUGIN_ID_PROPERTY = "Plugin ID"
        const val PLUGIN_VERSION_PROPERTY = "Plugin version"
        const val SHOW_PLUGIN_VERSION_PROPERTY = "Show plugin version"
        const val MODULE_COMPONENT_NAME = ".module"
    }
}
