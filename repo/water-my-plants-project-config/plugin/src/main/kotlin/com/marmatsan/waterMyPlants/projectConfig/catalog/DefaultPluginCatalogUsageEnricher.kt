package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree

/** Pure enrichment of plugin catalog entries with their repository usage. */
internal class DefaultPluginCatalogUsageEnricher : PluginCatalogUsageEnricher {
    /** Applies [mainUsages] and [conventionPluginUsages] to every node in [tree]. */
    override fun enrich(
        tree: PluginCatalogTree,
        mainUsages: Map<String, Set<String>>,
        conventionPluginUsages: Map<String, List<PluginCatalogNode.ConventionPluginUsage>>
    ): PluginCatalogTree =
        tree.copy(
            roots =
                tree.roots.map { node ->
                    enrichNode(
                        node = node,
                        mainUsages = mainUsages,
                        conventionPluginUsages = conventionPluginUsages
                    )
                }
        )

    private fun enrichNode(
        node: PluginCatalogNode,
        mainUsages: Map<String, Set<String>>,
        conventionPluginUsages: Map<String, List<PluginCatalogNode.ConventionPluginUsage>>,
        parentId: String = ""
    ): PluginCatalogNode {
        val pluginId =
            listOf(
                parentId,
                node.id
            ).filter(String::isNotBlank)
                .joinToString(".")

        return node.copy(
            appliedToModules = mainUsages[pluginId].orEmpty().sorted(),
            providedByConventionPlugins = conventionPluginUsages[pluginId].orEmpty(),
            children =
                node.children.map { child ->
                    enrichNode(
                        node = child,
                        mainUsages = mainUsages,
                        conventionPluginUsages = conventionPluginUsages,
                        parentId = pluginId
                    )
                }
        )
    }
}
