package com.marmatsan.figmaCatalogChecks.data.datasource.modules


import com.marmatsan.figmaCatalogChecks.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaCatalogChecks.data.figma.modules.FigmaModuleComponentReader
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModuleComponentSource
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModulesPort
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaModulesDataSource(
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaModuleComponentReader: FigmaModuleComponentReader
) : FigmaModulesPort {
    override fun readModules(source: FigmaModuleComponentSource): Set<String> =
        figmaModuleComponentReader.readComponent(
            component = figmaFileContentClient.getNodeContent(
                fileKey = source.component.fileKey,
                token = source.token,
                nodeId = source.component.nodeId
            ),
            componentNodeId = source.component.nodeId
        )
}
