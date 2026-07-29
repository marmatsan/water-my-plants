package com.marmatsan.figmaDocumentationSync.domain.port.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource

/**
 * Public Figma-owned boundary for a repository dependency DSL.
 *
 * Product composition implements this port and maps its catalog API into the
 * Figma domain. The reusable Figma engine never imports the source catalog's
 * implementation or model types.
 */
interface DependencyDslCatalogProvider {
    /** Reads the library tree with stable version aliases and usage metadata. */
    fun readLibraryTreeWithVersionAliases(
        rootDirPath: String,
        conventionPluginIncludedBuilds: List<IncludedBuildSource>
    ): LibraryCatalogTree

    /** Reads the plugin tree with stable version aliases and usage metadata. */
    fun readPluginTreeWithVersionAliases(
        rootDirPath: String,
        conventionPluginIncludedBuilds: List<IncludedBuildSource>
    ): PluginCatalogTree
}
