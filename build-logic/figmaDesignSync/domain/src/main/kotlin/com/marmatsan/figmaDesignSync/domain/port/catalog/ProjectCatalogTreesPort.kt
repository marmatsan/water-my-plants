package com.marmatsan.figmaDesignSync.domain.port.catalog

import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree

interface ProjectCatalogTreesPort {
    fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree
    fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree
}
