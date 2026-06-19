package com.marmatsan.figmaCatalogChecks.data.datasource.modules

import com.marmatsan.figmaCatalogChecks.data.figma.client.FigmaFileContentClient
import com.marmatsan.figmaCatalogChecks.data.figma.modules.FigmaModuleDependenciesReader
import com.marmatsan.figmaCatalogChecks.domain.model.modules.ModuleDependency
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModuleDependenciesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.FigmaModuleDependenciesSource
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaModuleDependenciesDataSource(
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaModuleDependenciesReader: FigmaModuleDependenciesReader
) : FigmaModuleDependenciesPort {
    override fun readModuleDependencies(source: FigmaModuleDependenciesSource): Set<ModuleDependency> =
        figmaModuleDependenciesReader.readSection(
            section = figmaFileContentClient.getNodeContent(
                fileKey = source.section.fileKey,
                token = source.token,
                nodeId = source.section.nodeId
            )
        )
}
