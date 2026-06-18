package com.marmatsan.figmaCatalogChecks.data.datasource.catalog


import com.marmatsan.figmaCatalogChecks.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaCatalogChecks.data.figma.catalog.library.FigmaFileLibraryCatalogTreeReader
import com.marmatsan.figmaCatalogChecks.data.figma.catalog.plugin.FigmaFilePluginCatalogTreeReader
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.FigmaCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.FigmaCatalogTreesPort
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaCatalogTreesDataSource(
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaFileLibraryCatalogTreeReader: FigmaFileLibraryCatalogTreeReader,
    private val figmaFilePluginCatalogTreeReader: FigmaFilePluginCatalogTreeReader
) : FigmaCatalogTreesPort {
    override fun readLibraryTree(source: FigmaCatalogTreeSource): LibraryCatalogTree =
        figmaFileLibraryCatalogTreeReader.readSection(
            section = figmaFileContentClient.getNodeContent(
                fileKey = source.section.fileKey,
                token = source.token,
                nodeId = source.section.nodeId
            ),
            sectionNodeId = source.section.nodeId
        )

    override fun readPluginTree(source: FigmaCatalogTreeSource): PluginCatalogTree =
        figmaFilePluginCatalogTreeReader.readSection(
            section = figmaFileContentClient.getNodeContent(
                fileKey = source.section.fileKey,
                token = source.token,
                nodeId = source.section.nodeId
            ),
            sectionNodeId = source.section.nodeId
        )
}
