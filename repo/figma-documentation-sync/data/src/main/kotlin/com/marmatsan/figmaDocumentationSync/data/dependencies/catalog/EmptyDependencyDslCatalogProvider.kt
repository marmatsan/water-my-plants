package com.marmatsan.figmaDocumentationSync.data.dependencies.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.DependencyDslCatalogProvider
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource

/** Portable adapter for repositories that do not publish a primary catalog. */
class EmptyDependencyDslCatalogProvider : DependencyDslCatalogProvider {
    override fun readLibraryTreeWithVersionAliases(
        rootDirPath: String,
        conventionPluginIncludedBuilds: List<IncludedBuildSource>,
    ): LibraryCatalogTree =
        LibraryCatalogTree(
            roots = emptyList(),
        )

    override fun readPluginTreeWithVersionAliases(
        rootDirPath: String,
        conventionPluginIncludedBuilds: List<IncludedBuildSource>,
    ): PluginCatalogTree =
        PluginCatalogTree(
            roots = emptyList(),
        )
}
