package com.marmatsan.figmaCatalogChecks.domain.port.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree

interface FigmaCatalogTreesPort {
    fun readLibraryTree(source: FigmaCatalogTreeSource): LibraryCatalogTree
    fun readPluginTree(source: FigmaCatalogTreeSource): PluginCatalogTree
}
