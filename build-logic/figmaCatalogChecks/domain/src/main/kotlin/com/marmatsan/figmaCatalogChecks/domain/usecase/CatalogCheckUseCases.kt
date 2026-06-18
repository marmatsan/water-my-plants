package com.marmatsan.figmaCatalogChecks.domain.usecase

import com.marmatsan.figmaCatalogChecks.domain.model.*
import com.marmatsan.figmaCatalogChecks.domain.comparison.*
import com.marmatsan.figmaCatalogChecks.domain.port.*

import me.tatarka.inject.annotations.Inject

data class CheckVersionsUseCaseRequest(
    val page: FigmaNodeReference,
    val section: FigmaNodeReference,
    val versionComponent: FigmaNodeReference,
    val versionsFilePath: String,
    val token: String
)

@Inject
class CheckVersionsUseCase(
    private val repositoryVersionsPort: RepositoryVersionsPort,
    private val figmaVersionsPort: FigmaVersionsPort,
    private val figmaVersionsCheck: FigmaVersionsCheck
) {
    fun execute(request: CheckVersionsUseCaseRequest): FigmaVersionsCheckResult =
        figmaVersionsCheck.check(
            FigmaVersionsCheckInput(
                page = request.page,
                section = request.section,
                versionComponent = request.versionComponent,
                repositoryVersions = repositoryVersionsPort.readVersions(
                    VersionsFileSource(path = request.versionsFilePath)
                ),
                figmaVersions = figmaVersionsPort.readVersions(
                    FigmaVersionsSource(
                        section = request.section,
                        versionComponent = request.versionComponent,
                        token = request.token
                    )
                )
            )
        )
}

data class CheckLibraryCatalogTreeUseCaseRequest(
    val page: FigmaNodeReference,
    val section: FigmaNodeReference,
    val projectSource: ProjectCatalogTreeSource,
    val token: String
)

sealed interface LibraryCatalogTreeCheckResult {
    data class Match(
        val sectionNodeId: String,
        val repositoryNodeCount: Int
    ) : LibraryCatalogTreeCheckResult

    data class DifferentFiles(
        val fileKeys: List<String>
    ) : LibraryCatalogTreeCheckResult

    data class Mismatch(
        val comparison: CatalogTreeComparisonResult
    ) : LibraryCatalogTreeCheckResult
}

@Inject
class CheckLibraryCatalogTreeUseCase(
    private val projectCatalogTreesPort: ProjectCatalogTreesPort,
    private val figmaCatalogTreesPort: FigmaCatalogTreesPort,
    private val libraryCatalogTreeComparison: LibraryCatalogTreeComparison
) {
    fun execute(request: CheckLibraryCatalogTreeUseCaseRequest): LibraryCatalogTreeCheckResult {
        val fileKeys = listOf(
            request.page.fileKey,
            request.section.fileKey
        ).distinct()

        if (fileKeys.size != 1) {
            return LibraryCatalogTreeCheckResult.DifferentFiles(fileKeys.sorted())
        }

        val repositoryTree = projectCatalogTreesPort.readLibraryTree(request.projectSource)
        val figmaTree = figmaCatalogTreesPort.readLibraryTree(
            FigmaCatalogTreeSource(
                section = request.section,
                token = request.token
            )
        )
        val comparison = libraryCatalogTreeComparison.compare(
            repositoryTree = repositoryTree,
            figmaTree = figmaTree
        )

        if (!comparison.matches) {
            return LibraryCatalogTreeCheckResult.Mismatch(comparison)
        }

        return LibraryCatalogTreeCheckResult.Match(
            sectionNodeId = request.section.nodeId,
            repositoryNodeCount = repositoryTree.nodeCount()
        )
    }
}

data class CheckPluginCatalogTreeUseCaseRequest(
    val page: FigmaNodeReference,
    val section: FigmaNodeReference,
    val projectSource: ProjectCatalogTreeSource,
    val token: String
)

sealed interface PluginCatalogTreeCheckResult {
    data class Match(
        val sectionNodeId: String,
        val repositoryNodeCount: Int
    ) : PluginCatalogTreeCheckResult

    data class DifferentFiles(
        val fileKeys: List<String>
    ) : PluginCatalogTreeCheckResult

    data class Mismatch(
        val comparison: CatalogTreeComparisonResult
    ) : PluginCatalogTreeCheckResult
}

@Inject
class CheckPluginCatalogTreeUseCase(
    private val projectCatalogTreesPort: ProjectCatalogTreesPort,
    private val figmaCatalogTreesPort: FigmaCatalogTreesPort,
    private val pluginCatalogTreeComparison: PluginCatalogTreeComparison
) {
    fun execute(request: CheckPluginCatalogTreeUseCaseRequest): PluginCatalogTreeCheckResult {
        val fileKeys = listOf(
            request.page.fileKey,
            request.section.fileKey
        ).distinct()

        if (fileKeys.size != 1) {
            return PluginCatalogTreeCheckResult.DifferentFiles(fileKeys.sorted())
        }

        val repositoryTree = projectCatalogTreesPort.readPluginTree(request.projectSource)
        val figmaTree = figmaCatalogTreesPort.readPluginTree(
            FigmaCatalogTreeSource(
                section = request.section,
                token = request.token
            )
        )
        val comparison = pluginCatalogTreeComparison.compare(
            repositoryTree = repositoryTree,
            figmaTree = figmaTree
        )

        if (!comparison.matches) {
            return PluginCatalogTreeCheckResult.Mismatch(comparison)
        }

        return PluginCatalogTreeCheckResult.Match(
            sectionNodeId = request.section.nodeId,
            repositoryNodeCount = repositoryTree.nodeCount()
        )
    }
}

data class CheckModulesUseCaseRequest(
    val page: FigmaNodeReference,
    val moduleComponent: FigmaNodeReference,
    val rootSettingsFilePath: String,
    val buildLogicSettingsFilePath: String,
    val token: String
)

sealed interface ModulesCheckResult {
    data class Match(
        val moduleComponentNodeId: String,
        val repositoryModuleCount: Int
    ) : ModulesCheckResult

    data class DifferentFiles(
        val fileKeys: List<String>
    ) : ModulesCheckResult

    data class Mismatch(
        val comparison: ModuleNamesComparisonResult
    ) : ModulesCheckResult
}

@Inject
class CheckModulesUseCase(
    private val projectModulesPort: ProjectModulesPort,
    private val figmaModulesPort: FigmaModulesPort,
    private val moduleNamesComparison: ModuleNamesComparison
) {
    fun execute(request: CheckModulesUseCaseRequest): ModulesCheckResult {
        val fileKeys = listOf(
            request.page.fileKey,
            request.moduleComponent.fileKey
        ).distinct()

        if (fileKeys.size != 1) {
            return ModulesCheckResult.DifferentFiles(fileKeys.sorted())
        }

        val repositoryModules = projectModulesPort.readModules(
            ProjectModulesSource(
                rootSettingsFilePath = request.rootSettingsFilePath,
                buildLogicSettingsFilePath = request.buildLogicSettingsFilePath
            )
        )
        val figmaModules = figmaModulesPort.readModules(
            FigmaModuleComponentSource(
                component = request.moduleComponent,
                token = request.token
            )
        )
        val comparison = moduleNamesComparison.compare(
            repositoryModules = repositoryModules,
            figmaModules = figmaModules
        )

        if (!comparison.matches) {
            return ModulesCheckResult.Mismatch(comparison)
        }

        return ModulesCheckResult.Match(
            moduleComponentNodeId = request.moduleComponent.nodeId,
            repositoryModuleCount = repositoryModules.size
        )
    }
}

private fun LibraryCatalogTree.nodeCount(): Int =
    roots.sumOf { node -> node.nodeCount() }

private fun LibraryCatalogNode.nodeCount(): Int =
    1 + children.sumOf { node -> node.nodeCount() }

private fun PluginCatalogTree.nodeCount(): Int =
    roots.sumOf { node -> node.nodeCount() }

private fun PluginCatalogNode.nodeCount(): Int =
    1 + children.sumOf { node -> node.nodeCount() }
