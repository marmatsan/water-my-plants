package com.marmatsan.projectConfig.figma.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode as SourceLibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode as SourcePluginCatalogNode

/** Maps the public dependency-catalog model into Figma-owned domain trees. */
internal interface CatalogTreeMapper {
    /** Maps library [roots] without reading repository state. */
    fun libraryTree(
        roots: List<SourceLibraryCatalogNode>
    ): LibraryCatalogTree

    /** Maps plugin [roots] without reading repository state. */
    fun pluginTree(
        roots: List<SourcePluginCatalogNode>
    ): PluginCatalogTree
}
