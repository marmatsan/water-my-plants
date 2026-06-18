package com.marmatsan.figmaCatalogChecks.domain.usecase.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.FigmaCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.FigmaCatalogTreesPort

internal class FakeFigmaCatalogTreesPort(
    private val libraryTree: LibraryCatalogTree = LibraryCatalogTree(roots = emptyList()),
    private val pluginTree: PluginCatalogTree = PluginCatalogTree(roots = emptyList())
) : FigmaCatalogTreesPort {
    override fun readLibraryTree(source: FigmaCatalogTreeSource): LibraryCatalogTree = libraryTree

    override fun readPluginTree(source: FigmaCatalogTreeSource): PluginCatalogTree = pluginTree
}
