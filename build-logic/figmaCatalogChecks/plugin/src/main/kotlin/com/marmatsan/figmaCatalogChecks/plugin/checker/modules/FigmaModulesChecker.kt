package com.marmatsan.figmaCatalogChecks.plugin.checker.modules

import com.marmatsan.figmaCatalogChecks.data.figma.client.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.figma.common.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.CheckModulesUseCase
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.CheckModulesUseCaseRequest
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.ModulesCheckResult
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException

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
