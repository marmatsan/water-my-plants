package com.marmatsan.figmaDocumentationSync.domain.port.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree

/**
 * Port for reading project catalog sources into domain catalog trees.
 *
 * Implementations live in the data layer and adapt concrete repository files to
 * [LibraryCatalogTree] and [PluginCatalogTree], which are later serialized into
 * `design-model.json` for Figma.
 *
 * @sample com.marmatsan.figmaDocumentationSync.domain.samples.DomainKDocSamples.projectCatalogTreesPortSample
 *
 * @see ProjectCatalogTreeSource
 */
interface ProjectCatalogTreesPort {
    /**
     * Reads a library catalog tree from [source].
     *
     * Sources that do not define libraries should fail fast in the adapter
     * rather than returning an empty tree that would hide an invalid generator
     * request.
     */
    fun readLibraryTree(
        source: ProjectCatalogTreeSource
    ): LibraryCatalogTree

    /**
     * Reads a plugin catalog tree from [source].
     *
     * The returned tree may represent external catalog plugins, gradle-plugins
     * convention plugins, or repository-owned Gradle plugins depending on the
     * source variant.
     */
    fun readPluginTree(
        source: ProjectCatalogTreeSource
    ): PluginCatalogTree
}
