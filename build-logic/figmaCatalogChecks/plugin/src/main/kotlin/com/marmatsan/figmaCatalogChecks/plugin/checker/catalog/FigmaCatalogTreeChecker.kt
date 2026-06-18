package com.marmatsan.figmaCatalogChecks.plugin.checker.catalog

import com.marmatsan.figmaCatalogChecks.data.figma.client.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.figma.common.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.usecase.catalog.CheckLibraryCatalogTreeUseCase
import com.marmatsan.figmaCatalogChecks.domain.usecase.catalog.CheckLibraryCatalogTreeUseCaseRequest
import com.marmatsan.figmaCatalogChecks.domain.usecase.catalog.CheckPluginCatalogTreeUseCase
import com.marmatsan.figmaCatalogChecks.domain.usecase.catalog.CheckPluginCatalogTreeUseCaseRequest
import com.marmatsan.figmaCatalogChecks.domain.usecase.catalog.LibraryCatalogTreeCheckResult
import com.marmatsan.figmaCatalogChecks.domain.usecase.catalog.PluginCatalogTreeCheckResult
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException

@Inject
internal class FigmaCatalogTreeChecker(
    private val checkLibraryCatalogTreeUseCase: CheckLibraryCatalogTreeUseCase,
    private val checkPluginCatalogTreeUseCase: CheckPluginCatalogTreeUseCase
) {
    fun checkLibraryTree(
        request: FigmaCatalogTreeCheckRequest
    ): FigmaCatalogTreeTaskResult {
        try {
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val section = FigmaNodeUrl.parse(request.sectionUrl)

            return checkLibraryCatalogTreeUseCase.execute(
                CheckLibraryCatalogTreeUseCaseRequest(
                    page = page,
                    section = section,
                    projectSource = ProjectCatalogTreeSource.DependenciesDslVersionAliases,
                    token = request.token
                )
            )
                .toTaskResult()
        } catch (exception: FigmaFileContentException) {
            throw GradleException(
                exception.message ?: "Figma file content request failed",
                exception
            )
        }
    }

    fun checkPluginTree(
        request: FigmaCatalogTreeCheckRequest
    ): FigmaCatalogTreeTaskResult {
        try {
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val section = FigmaNodeUrl.parse(request.sectionUrl)

            return checkPluginCatalogTreeUseCase.execute(
                CheckPluginCatalogTreeUseCaseRequest(
                    page = page,
                    section = section,
                    projectSource = ProjectCatalogTreeSource.DependenciesDslVersionAliases,
                    token = request.token
                )
            )
                .toTaskResult()
        } catch (exception: FigmaFileContentException) {
            throw GradleException(
                exception.message ?: "Figma file content request failed",
                exception
            )
        }
    }

    fun checkBuildLogicLibraryTree(
        request: FigmaBuildLogicCatalogTreeCheckRequest
    ): FigmaCatalogTreeTaskResult {
        try {
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val section = FigmaNodeUrl.parse(request.sectionUrl)

            return checkLibraryCatalogTreeUseCase.execute(
                CheckLibraryCatalogTreeUseCaseRequest(
                    page = page,
                    section = section,
                    projectSource = ProjectCatalogTreeSource.BuildLogicSettings(
                        settingsFilePath = request.settingsFile.absolutePath
                    ),
                    token = request.token
                )
            )
                .toTaskResult()
        } catch (exception: FigmaFileContentException) {
            throw GradleException(
                exception.message ?: "Figma file content request failed",
                exception
            )
        }
    }

    fun checkBuildLogicPluginTree(
        request: FigmaBuildLogicCatalogTreeCheckRequest
    ): FigmaCatalogTreeTaskResult {
        try {
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val section = FigmaNodeUrl.parse(request.sectionUrl)

            return checkPluginCatalogTreeUseCase.execute(
                CheckPluginCatalogTreeUseCaseRequest(
                    page = page,
                    section = section,
                    projectSource = ProjectCatalogTreeSource.BuildLogicSettings(
                        settingsFilePath = request.settingsFile.absolutePath
                    ),
                    token = request.token
                )
            )
                .toTaskResult()
        } catch (exception: FigmaFileContentException) {
            throw GradleException(
                exception.message ?: "Figma file content request failed",
                exception
            )
        }
    }

    private fun LibraryCatalogTreeCheckResult.toTaskResult(): FigmaCatalogTreeTaskResult =
        when (this) {
            is LibraryCatalogTreeCheckResult.Match -> FigmaCatalogTreeTaskResult(
                sectionNodeId = sectionNodeId,
                repositoryNodeCount = repositoryNodeCount
            )

            is LibraryCatalogTreeCheckResult.DifferentFiles -> throw GradleException(
                "Figma URLs must point to the same file. Found file keys: ${fileKeys.joinToString()}"
            )

            is LibraryCatalogTreeCheckResult.Mismatch -> throw GradleException(comparison.report())
        }

    private fun PluginCatalogTreeCheckResult.toTaskResult(): FigmaCatalogTreeTaskResult =
        when (this) {
            is PluginCatalogTreeCheckResult.Match -> FigmaCatalogTreeTaskResult(
                sectionNodeId = sectionNodeId,
                repositoryNodeCount = repositoryNodeCount
            )

            is PluginCatalogTreeCheckResult.DifferentFiles -> throw GradleException(
                "Figma URLs must point to the same file. Found file keys: ${fileKeys.joinToString()}"
            )

            is PluginCatalogTreeCheckResult.Mismatch -> throw GradleException(comparison.report())
        }
}
