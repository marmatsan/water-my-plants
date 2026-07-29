package com.marmatsan.figmaDocumentationSync.data.gradle.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode

/** Builds deterministic catalog tree nodes from fully qualified Gradle plugin ids. */
internal object PluginCatalogTreeBuilder {
    /** Builds sorted roots for [pluginIds] and attaches their consuming module identities. */
    fun build(
        pluginIds: Set<String>,
        usageByPluginId: Map<String, Set<String>>,
    ): List<PluginCatalogNode> =
        pluginIds
            .map { pluginId -> pluginId.split(".") }
            .fold(emptyList<PluginCatalogNode>()) { nodes, segments ->
                nodes.withPath(
                    segments = segments,
                    usageByPluginId = usageByPluginId,
                )
            }.sortedBy(PluginCatalogNode::id)

    private fun List<PluginCatalogNode>.withPath(
        segments: List<String>,
        usageByPluginId: Map<String, Set<String>>,
        parentId: String = "",
    ): List<PluginCatalogNode> {
        if (segments.isEmpty()) {
            return this
        }

        val head = segments.first()
        val tail = segments.drop(1)
        val pluginId =
            listOf(
                parentId,
                head,
            ).filter(String::isNotBlank)
                .joinToString(".")
        val existingNode = firstOrNull { node -> node.id == head }
        val updatedNode =
            existingNode
                ?.copy(
                    appliedToModules = usageByPluginId[pluginId].orEmpty().sorted(),
                    children =
                        existingNode.children.withPath(
                            segments = tail,
                            usageByPluginId = usageByPluginId,
                            parentId = pluginId,
                        ),
                )
                ?: PluginCatalogNode(
                    id = head,
                    appliedToModules = usageByPluginId[pluginId].orEmpty().sorted(),
                    children =
                        emptyList<PluginCatalogNode>().withPath(
                            segments = tail,
                            usageByPluginId = usageByPluginId,
                            parentId = pluginId,
                        ),
                )

        return filterNot { node -> node.id == head }
            .plus(
                element = updatedNode,
            ).sortedBy(PluginCatalogNode::id)
    }
}
