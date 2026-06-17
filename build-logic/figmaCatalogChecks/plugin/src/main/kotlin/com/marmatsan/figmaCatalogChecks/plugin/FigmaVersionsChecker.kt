package com.marmatsan.figmaCatalogChecks.plugin

import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentClient
import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.FigmaFileVersionsReader
import com.marmatsan.figmaCatalogChecks.data.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.data.VersionsPropertiesReader
import com.marmatsan.figmaCatalogChecks.domain.FigmaVersionsCheck
import com.marmatsan.figmaCatalogChecks.domain.FigmaVersionsCheckInput
import com.marmatsan.figmaCatalogChecks.domain.FigmaVersionsCheckResult
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException
import java.io.File

internal data class FigmaVersionsCheckRequest(
    val pageUrl: String,
    val sectionUrl: String,
    val versionComponentUrl: String,
    val versionsFile: File,
    val token: String
)

internal data class FigmaVersionsTaskResult(
    val sectionNodeId: String,
    val repositoryVersionCount: Int
)

@Inject
internal class FigmaVersionsChecker(
    private val versionsPropertiesReader: VersionsPropertiesReader,
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaFileVersionsReader: FigmaFileVersionsReader,
    private val figmaVersionsCheck: FigmaVersionsCheck
) {
    fun check(
        request: FigmaVersionsCheckRequest
    ): FigmaVersionsTaskResult {
        try {
            val repositoryVersions = versionsPropertiesReader.read(request.versionsFile)
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val section = FigmaNodeUrl.parse(request.sectionUrl)
            val versionComponent = FigmaNodeUrl.parse(request.versionComponentUrl)

            val sectionContent = figmaFileContentClient.getNodeContent(
                fileKey = section.fileKey,
                token = request.token,
                nodeId = section.nodeId
            )
            val figmaVersions = figmaFileVersionsReader.readSection(
                section = sectionContent,
                sectionNodeId = section.nodeId,
                versionComponentNodeId = versionComponent.nodeId
            )

            return when (
                val result = figmaVersionsCheck.check(
                    FigmaVersionsCheckInput(
                        page = page,
                        section = section,
                        versionComponent = versionComponent,
                        repositoryVersions = repositoryVersions,
                        figmaVersions = figmaVersions
                    )
                )
            ) {
                is FigmaVersionsCheckResult.Match -> FigmaVersionsTaskResult(
                    sectionNodeId = result.sectionNodeId,
                    repositoryVersionCount = result.repositoryVersionCount
                )

                is FigmaVersionsCheckResult.DifferentFiles -> throw GradleException(
                    "Figma URLs must point to the same file. Found file keys: ${result.fileKeys.joinToString()}"
                )

                is FigmaVersionsCheckResult.VersionsMismatch -> throw GradleException(result.comparison.report())
            }
        } catch (exception: FigmaFileContentException) {
            throw GradleException(
                exception.message ?: "Figma file content request failed",
                exception
            )
        }
    }
}
