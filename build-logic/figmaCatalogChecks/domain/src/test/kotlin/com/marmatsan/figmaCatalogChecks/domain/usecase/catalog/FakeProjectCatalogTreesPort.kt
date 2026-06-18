package com.marmatsan.figmaCatalogChecks.domain.usecase.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreesPort

internal class FakeProjectCatalogTreesPort(
    private val libraryTree: LibraryCatalogTree = LibraryCatalogTree(roots = emptyList()),
    private val pluginTree: PluginCatalogTree = PluginCatalogTree(roots = emptyList())
) : ProjectCatalogTreesPort {
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree = libraryTree

    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree = pluginTree
}
