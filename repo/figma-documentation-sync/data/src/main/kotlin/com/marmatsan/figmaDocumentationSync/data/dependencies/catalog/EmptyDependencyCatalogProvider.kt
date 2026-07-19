package com.marmatsan.figmaDocumentationSync.data.dependencies.catalog

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import java.io.File

/** Portable adapter for repositories that do not publish a primary catalog. */
class EmptyDependencyCatalogProvider : DependencyCatalogProvider {
    override fun resolved(rootDir: File): DependencyCatalogTrees = emptyCatalog()

    override fun withVersionAliases(): DependencyCatalogTrees = emptyCatalog()

    private fun emptyCatalog(): DependencyCatalogTrees =
        DependencyCatalogTrees(
            libraries = emptyList(),
            plugins = emptyList()
        )
}
