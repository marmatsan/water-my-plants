package com.marmatsan.figmaCatalogChecks.plugin

import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentClient
import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.FigmaModuleComponentReader
import com.marmatsan.figmaCatalogChecks.data.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.data.GradleProjectModulesReader
import com.marmatsan.figmaCatalogChecks.domain.ModuleNamesComparison
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException
import java.io.File

internal data class FigmaModulesCheckRequest(
    val pageUrl: String,
    val moduleComponentUrl: String,
    val rootSettingsFile: File,
    val buildLogicSettingsFile: File,
    val token: String
)

internal data class FigmaModulesTaskResult(
    val moduleComponentNodeId: String,
    val repositoryModuleCount: Int
)

@Inject
internal class FigmaModulesChecker(
    private val gradleProjectModulesReader: GradleProjectModulesReader,
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaModuleComponentReader: FigmaModuleComponentReader,
    private val moduleNamesComparison: ModuleNamesComparison
) {
    fun check(
        request: FigmaModulesCheckRequest
    ): FigmaModulesTaskResult {
        try {
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val moduleComponent = FigmaNodeUrl.parse(request.moduleComponentUrl)

            if (page.fileKey != moduleComponent.fileKey) {
                throw GradleException(
                    "Figma URLs must point to the same file. Found file keys: ${page.fileKey}, ${moduleComponent.fileKey}"
                )
            }

            val repositoryModules = gradleProjectModulesReader.readModules(
                rootSettingsFile = request.rootSettingsFile,
                buildLogicSettingsFile = request.buildLogicSettingsFile
            )
            val figmaModules = figmaModuleComponentReader.readComponent(
                component = figmaFileContentClient.getNodeContent(
                    fileKey = moduleComponent.fileKey,
                    token = request.token,
                    nodeId = moduleComponent.nodeId
                ),
                componentNodeId = moduleComponent.nodeId
            )
            val comparison = moduleNamesComparison.compare(
                repositoryModules = repositoryModules,
                figmaModules = figmaModules
            )

            if (!comparison.matches) {
                throw GradleException(comparison.report())
            }

            return FigmaModulesTaskResult(
                moduleComponentNodeId = moduleComponent.nodeId,
                repositoryModuleCount = repositoryModules.size
            )
        } catch (exception: FigmaFileContentException) {
            throw GradleException(
                exception.message ?: "Figma file content request failed",
                exception
            )
        }
    }
}
