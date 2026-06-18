package com.marmatsan.figmaCatalogChecks.domain.comparison.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogEntry
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import me.tatarka.inject.annotations.Inject

@Inject
class LibraryCatalogTreeComparison {
    fun compare(
        repositoryTree: LibraryCatalogTree,
        figmaTree: LibraryCatalogTree
    ): CatalogTreeComparisonResult =
        compareCatalogFacts(
            subject = "library",
            repositoryFacts = repositoryTree.toFacts(),
            figmaFacts = figmaTree.toFacts()
        )

    private fun LibraryCatalogTree.toFacts(): Map<String, String> =
        roots
            .flatMap { root -> root.toFacts(parentPath = null) }
            .toMap()
            .toSortedMap()

    private fun LibraryCatalogNode.toFacts(parentPath: String?): List<Pair<String, String>> {
        val path = buildPath(parentPath, group)
        val nodeFact = "library $path" to "artifactsVisible=$artifactsVisible"
        val entryFacts = entries.map { entry -> entry.toFact(path) }
        val childFacts = children.flatMap { child -> child.toFacts(parentPath = path) }

        return listOf(nodeFact) + entryFacts + childFacts
    }

    private fun LibraryCatalogEntry.toFact(path: String): Pair<String, String> =
        when (this) {
            is LibraryCatalogEntry.Artifact -> {
                val key = "library $path artifact $artifact"
                val value = "version=${version.render()}"

                key to value
            }

            is LibraryCatalogEntry.ArtifactsBundle -> {
                val key = "library $path bundle $alias"
                val value = listOf(
                    "artifacts=${artifacts.renderList()}",
                    "version=${version.render()}"
                ).joinToString("; ")

                key to value
            }
        }
}
