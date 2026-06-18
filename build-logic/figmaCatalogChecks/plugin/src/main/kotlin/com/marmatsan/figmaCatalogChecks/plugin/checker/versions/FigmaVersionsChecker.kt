package com.marmatsan.figmaCatalogChecks.plugin.checker.versions

import com.marmatsan.figmaCatalogChecks.data.figma.client.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.figma.common.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.domain.usecase.versions.CheckVersionsUseCase
import com.marmatsan.figmaCatalogChecks.domain.usecase.versions.CheckVersionsUseCaseRequest
import com.marmatsan.figmaCatalogChecks.domain.usecase.versions.FigmaVersionsCheckResult
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException

@Inject
internal class FigmaVersionsChecker(
    private val checkVersionsUseCase: CheckVersionsUseCase
) {
    fun check(
        request: FigmaVersionsCheckRequest
    ): FigmaVersionsTaskResult {
        try {
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val section = FigmaNodeUrl.parse(request.sectionUrl)
            val versionComponent = FigmaNodeUrl.parse(request.versionComponentUrl)

            return when (
                val result = checkVersionsUseCase.execute(
                    CheckVersionsUseCaseRequest(
                        page = page,
                        section = section,
                        versionComponent = versionComponent,
                        versionsFilePath = request.versionsFile.absolutePath,
                        token = request.token
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
