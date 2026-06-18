package com.marmatsan.figmaCatalogChecks.domain.comparison.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import me.tatarka.inject.annotations.Inject

@Inject
class PluginCatalogTreeComparison {
    fun compare(
        repositoryTree: PluginCatalogTree,
        figmaTree: PluginCatalogTree
    ): CatalogTreeComparisonResult =
        compareCatalogFacts(
            subject = "plugin",
            repositoryFacts = repositoryTree.toFacts(),
            figmaFacts = figmaTree.toFacts()
        )

    private fun PluginCatalogTree.toFacts(): Map<String, String> =
        roots
            .flatMap { root -> root.toFacts(parentPath = null) }
            .toMap()
            .toSortedMap()

    private fun PluginCatalogNode.toFacts(parentPath: String?): List<Pair<String, String>> {
        val path = buildPath(parentPath, id)
        val nodeFact = "plugin $path" to "version=${version.render()}"
        val childFacts = children.flatMap { child -> child.toFacts(parentPath = path) }

        return listOf(nodeFact) + childFacts
    }
}
