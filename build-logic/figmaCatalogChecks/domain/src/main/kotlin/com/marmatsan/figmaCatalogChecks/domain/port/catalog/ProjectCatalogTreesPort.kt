package com.marmatsan.figmaCatalogChecks.domain.port.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree

interface ProjectCatalogTreesPort {
    fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree
    fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree
}
