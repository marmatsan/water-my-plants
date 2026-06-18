package com.marmatsan.figmaCatalogChecks.data.datasource.versions


import com.marmatsan.figmaCatalogChecks.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaCatalogChecks.data.figma.versions.FigmaFileVersionsReader
import com.marmatsan.figmaCatalogChecks.domain.port.versions.FigmaVersionsPort
import com.marmatsan.figmaCatalogChecks.domain.port.versions.FigmaVersionsSource
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaVersionsDataSource(
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaFileVersionsReader: FigmaFileVersionsReader
) : FigmaVersionsPort {
    override fun readVersions(source: FigmaVersionsSource): Map<String, String> =
        figmaFileVersionsReader.readSection(
            section = figmaFileContentClient.getNodeContent(
                fileKey = source.section.fileKey,
                token = source.token,
                nodeId = source.section.nodeId
            ),
            sectionNodeId = source.section.nodeId,
            versionComponentNodeId = source.versionComponent.nodeId
        )
}
