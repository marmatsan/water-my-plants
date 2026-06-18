package com.marmatsan.figmaCatalogChecks.plugin

import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.domain.CheckLibraryCatalogTreeUseCase
import com.marmatsan.figmaCatalogChecks.domain.CheckLibraryCatalogTreeUseCaseRequest
import com.marmatsan.figmaCatalogChecks.domain.CheckPluginCatalogTreeUseCase
import com.marmatsan.figmaCatalogChecks.domain.CheckPluginCatalogTreeUseCaseRequest
import com.marmatsan.figmaCatalogChecks.domain.LibraryCatalogTreeCheckResult
import com.marmatsan.figmaCatalogChecks.domain.PluginCatalogTreeCheckResult
import com.marmatsan.figmaCatalogChecks.domain.ProjectCatalogTreeSource
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException
import java.io.File

internal data class FigmaCatalogTreeCheckRequest(
    val pageUrl: String,
    val sectionUrl: String,
    val projectRootDir: File,
    val token: String
)

internal data class FigmaBuildLogicCatalogTreeCheckRequest(
    val pageUrl: String,
    val sectionUrl: String,
    val settingsFile: File,
    val token: String
)

internal data class FigmaCatalogTreeTaskResult(
    val sectionNodeId: String,
    val repositoryNodeCount: Int
)

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
