package com.marmatsan.figmaCatalogChecks.plugin.checker.modules

import com.marmatsan.figmaCatalogChecks.data.figma.client.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.figma.common.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModuleDependenciesScope
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.CheckModuleDependenciesUseCase
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.CheckModuleDependenciesUseCaseRequest
import com.marmatsan.figmaCatalogChecks.domain.usecase.modules.ModuleDependenciesCheckResult
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException

@Inject
internal class FigmaModuleDependenciesChecker(
    private val checkModuleDependenciesUseCase: CheckModuleDependenciesUseCase
) {
    fun check(request: FigmaModuleDependenciesCheckRequest): FigmaModuleDependenciesTaskResult {
        try {
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val mainSection = FigmaNodeUrl.parse(request.mainSectionUrl)
            val buildLogicSection = FigmaNodeUrl.parse(request.buildLogicSectionUrl)

            val mainResult = checkModuleDependenciesUseCase.execute(
                CheckModuleDependenciesUseCaseRequest(
                    page = page,
                    section = mainSection,
                    rootDirPath = request.projectRootDirectory.absolutePath,
                    scope = ProjectModuleDependenciesScope.Main,
                    token = request.token
                )
            ).toDependencyCount()

            val buildLogicResult = checkModuleDependenciesUseCase.execute(
                CheckModuleDependenciesUseCaseRequest(
                    page = page,
                    section = buildLogicSection,
                    rootDirPath = request.buildLogicRootDirectory.absolutePath,
                    scope = ProjectModuleDependenciesScope.BuildLogic,
                    token = request.token
                )
            ).toDependencyCount()

            return FigmaModuleDependenciesTaskResult(
                mainDependencyCount = mainResult,
                buildLogicDependencyCount = buildLogicResult
            )
        } catch (exception: FigmaFileContentException) {
            throw GradleException(
                exception.message ?: "Figma file content request failed",
                exception
            )
        }
    }

    private fun ModuleDependenciesCheckResult.toDependencyCount(): Int =
        when (this) {
            is ModuleDependenciesCheckResult.Match -> repositoryDependencyCount

            is ModuleDependenciesCheckResult.DifferentFiles -> throw GradleException(
                "Figma URLs must point to the same file. Found file keys: ${fileKeys.joinToString()}"
            )

            is ModuleDependenciesCheckResult.Mismatch -> throw GradleException(comparison.report())
        }
}
