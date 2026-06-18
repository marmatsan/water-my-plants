package com.marmatsan.figmaCatalogChecks.plugin

import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.domain.FigmaVersionsCheckResult
import com.marmatsan.figmaCatalogChecks.domain.CheckVersionsUseCase
import com.marmatsan.figmaCatalogChecks.domain.CheckVersionsUseCaseRequest
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
