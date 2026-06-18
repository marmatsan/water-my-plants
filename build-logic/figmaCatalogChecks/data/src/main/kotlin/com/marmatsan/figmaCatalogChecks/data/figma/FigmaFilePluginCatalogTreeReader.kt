package com.marmatsan.figmaCatalogChecks.data.figma

import com.marmatsan.figmaCatalogChecks.domain.model.*

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaFilePluginCatalogTreeReader {
    fun readSection(
        section: FigmaNode,
        sectionNodeId: String
    ): PluginCatalogTree {
        val rootSections = section.children.filter { node -> node.type == "SECTION" }

        if (rootSections.isEmpty()) {
            error("Figma plugin tree section '$sectionNodeId' contains no root sections")
        }

        return PluginCatalogTree(
            roots = rootSections.flatMap { rootSection -> rootSection.readPluginRoots() }
        )
    }

    private fun FigmaNode.readPluginRoots(): List<PluginCatalogNode> {
        val treeNodes = children
            .filter { node -> node.visible && node.isPluginTreeNode() }
            .associateBy(FigmaNode::id)

        if (treeNodes.isEmpty()) {
            error("Figma plugin root section '$id' contains no plugin tree nodes")
        }

        val parentByChildId = children
            .filter { node -> node.type == "CONNECTOR" }
            .mapNotNull { connector -> connector.toChildParentPair(treeNodes) }
            .toMap()

        val childIdsByParentId = parentByChildId
            .entries
            .groupBy(
                keySelector = { (_, parentId) -> parentId },
                valueTransform = { (childId, _) -> childId }
            )

        return treeNodes
            .values
            .filter { node -> node.id !in parentByChildId }
            .sortedByPosition()
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
                .sortedByPosition()
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
        descendants()
            .filter { node -> node.visible && node.isModule() }
            .mapNotNull { module -> module.renderedTextValues().firstOrNull() }
            .distinct()
            .sorted()
            .toList()

    private fun FigmaNode.isModule(): Boolean =
        type == "INSTANCE" && name == MODULE_COMPONENT_NAME

    private fun FigmaNode.requiredTextProperty(name: String): String =
        textProperty(name)
            ?.takeIf(String::isNotBlank)
            ?: error("Figma node '$id' is missing '$name'")

    private fun FigmaNode.textProperty(name: String): String? =
        componentProperties
            .findPropertyEntry(name)
            ?.let { (propertyKey, property) ->
                renderedTextPropertyValue(propertyKey)
                    ?: property.value.toPropertyValue().trim()
            }

    private fun FigmaNode.booleanProperty(
        name: String,
        default: Boolean
    ): Boolean =
        componentProperties
            .findPropertyEntry(name)
            ?.value
            ?.value
            ?.toBooleanPropertyValue()
            ?: default

    private fun FigmaNode.renderedTextValues(): Sequence<String> =
        descendants()
            .filter { node -> node.type == "TEXT" }
            .map { node -> node.characters.orEmpty().trim() }
            .filter(String::isNotBlank)

    private fun FigmaNode.descendants(): Sequence<FigmaNode> = sequence {
        if (visible) {
            yield(this@descendants)
            children.forEach { child ->
                yieldAll(child.descendants())
            }
        }
    }

    private fun Iterable<FigmaNode>.sortedByPosition(): List<FigmaNode> =
        sortedWith(
            compareBy<FigmaNode>(
                { node -> node.absoluteBoundingBox?.y ?: 0.0 },
                { node -> node.absoluteBoundingBox?.x ?: 0.0 },
                FigmaNode::id
            )
        )

    private fun Map<String, List<String>>.getValueOrEmpty(key: String): List<String> =
        this[key].orEmpty()

    private fun FigmaNode.toChildParentPair(treeNodes: Map<String, FigmaNode>): Pair<String, String>? {
        val startId = connectorStart?.endpointNodeId
        val endId = connectorEnd?.endpointNodeId

        if (startId !in treeNodes || endId !in treeNodes || startId == null || endId == null) {
            return null
        }

        return endId to startId
    }

    private fun FigmaNode.renderedTextPropertyValue(propertyKey: String): String? =
        descendants()
            .filter { node -> node.type == "TEXT" }
            .firstOrNull { node -> node.componentPropertyReferences["characters"] == propertyKey }
            ?.characters
            ?.trim()
            ?.takeIf(String::isNotBlank)

    private fun Map<String, FigmaComponentProperty>.findPropertyEntry(
        name: String
    ): Map.Entry<String, FigmaComponentProperty>? =
        entries
            .firstOrNull { (key, _) -> key == name || key.startsWith("$name#") }

    private fun JsonElement.toPropertyValue(): String =
        when (this) {
            is JsonPrimitive -> when {
                isString -> content
                booleanOrNull != null -> booleanOrNull.toString()
                else -> content
            }

            else -> toString()
        }

    private fun JsonElement.toBooleanPropertyValue(): Boolean? =
        when (this) {
            is JsonPrimitive -> booleanOrNull ?: content.toBooleanStrictOrNull()
            else -> null
        }

    private companion object {
        const val TYPE_PROPERTY = "Type"
        const val PLUGIN_TYPE = "Plugin"
        const val PLUGIN_ID_PROPERTY = "Plugin ID"
        const val PLUGIN_VERSION_PROPERTY = "Plugin version"
        const val SHOW_PLUGIN_VERSION_PROPERTY = "Show plugin version"
        const val MODULE_COMPONENT_NAME = ".module"
    }
}
