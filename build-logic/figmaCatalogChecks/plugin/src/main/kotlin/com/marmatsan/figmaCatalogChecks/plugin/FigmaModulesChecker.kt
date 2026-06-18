package com.marmatsan.figmaCatalogChecks.plugin

import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.domain.CheckModulesUseCase
import com.marmatsan.figmaCatalogChecks.domain.CheckModulesUseCaseRequest
import com.marmatsan.figmaCatalogChecks.domain.ModulesCheckResult
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
    private val checkModulesUseCase: CheckModulesUseCase
) {
    fun check(
        request: FigmaModulesCheckRequest
    ): FigmaModulesTaskResult {
        try {
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val moduleComponent = FigmaNodeUrl.parse(request.moduleComponentUrl)

            return when (
                val result = checkModulesUseCase.execute(
                    CheckModulesUseCaseRequest(
                        page = page,
                        moduleComponent = moduleComponent,
                        rootSettingsFilePath = request.rootSettingsFile.absolutePath,
                        buildLogicSettingsFilePath = request.buildLogicSettingsFile.absolutePath,
                        token = request.token
                    )
                )
            ) {
                is ModulesCheckResult.Match -> FigmaModulesTaskResult(
                    moduleComponentNodeId = result.moduleComponentNodeId,
                    repositoryModuleCount = result.repositoryModuleCount
                )

                is ModulesCheckResult.DifferentFiles -> throw GradleException(
                    "Figma URLs must point to the same file. Found file keys: ${result.fileKeys.joinToString()}"
                )

                is ModulesCheckResult.Mismatch -> throw GradleException(result.comparison.report())
            }
        } catch (exception: FigmaFileContentException) {
            throw GradleException(
                exception.message ?: "Figma file content request failed",
                exception
            )
        }
    }
}
