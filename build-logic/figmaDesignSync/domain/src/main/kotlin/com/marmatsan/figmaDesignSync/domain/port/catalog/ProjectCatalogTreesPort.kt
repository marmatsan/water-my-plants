package com.marmatsan.figmaDesignSync.domain.port.catalog

import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree

/**
 * Port for reading project catalog sources into domain catalog trees.
 *
 * Implementations live in the data layer and adapt concrete repository files to
 * [LibraryCatalogTree] and [PluginCatalogTree], which are later serialized into
 * `design-model.json` for Figma.
 *
 * Example:
 * ```
 * val source = ProjectCatalogTreeSource.BuildLogicSettings(
 *     settingsFilePath = "build-logic/settings.gradle.kts"
 * )
 * val libraryTree = port.readLibraryTree(source)
 * val pluginTree = port.readPluginTree(source)
 * ```
 */
interface ProjectCatalogTreesPort {
    fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree
    fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree
}
