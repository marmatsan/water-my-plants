package com.marmatsan.figmaVersions

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

internal data class FigmaVersionsCheckResult(
    val sectionNodeId: String,
    val repositoryVersionCount: Int
)

@Inject
internal class FigmaVersionsChecker(
    private val versionsPropertiesReader: VersionsPropertiesReader,
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaFileVersionsReader: FigmaFileVersionsReader,
    private val versionsComparison: VersionsComparison
) {
    fun check(
        request: FigmaVersionsCheckRequest
    ): FigmaVersionsCheckResult {
        val repositoryVersions = versionsPropertiesReader.read(request.versionsFile)
        val page = FigmaNodeUrl.parse(request.pageUrl)
        val section = FigmaNodeUrl.parse(request.sectionUrl)
        val versionComponent = FigmaNodeUrl.parse(request.versionComponentUrl)

        validateSameFile(
            page = page,
            section = section,
            versionComponent = versionComponent
        )

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
        val result = versionsComparison.compare(
            repositoryVersions = repositoryVersions,
            figmaVersions = figmaVersions
        )

        if (!result.matches) {
            throw GradleException(result.report())
        }

        return FigmaVersionsCheckResult(
            sectionNodeId = section.nodeId,
            repositoryVersionCount = repositoryVersions.size
        )
    }

    private fun validateSameFile(
        page: FigmaNodeUrl,
        section: FigmaNodeUrl,
        versionComponent: FigmaNodeUrl
    ) {
        val fileKeys = setOf(
            page.fileKey,
            section.fileKey,
            versionComponent.fileKey
        )

        if (fileKeys.size != 1) {
            throw GradleException(
                "Figma URLs must point to the same file. Found file keys: ${fileKeys.sorted().joinToString()}"
            )
        }
    }
}
