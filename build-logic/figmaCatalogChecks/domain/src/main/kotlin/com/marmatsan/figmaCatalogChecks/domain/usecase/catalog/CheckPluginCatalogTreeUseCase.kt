package com.marmatsan.figmaCatalogChecks.domain.usecase.catalog

import com.marmatsan.figmaCatalogChecks.domain.comparison.catalog.PluginCatalogTreeComparison
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.FigmaCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.FigmaCatalogTreesPort
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreesPort
import me.tatarka.inject.annotations.Inject

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

private fun PluginCatalogTree.nodeCount(): Int =
    roots.sumOf { node -> node.nodeCount() }

private fun PluginCatalogNode.nodeCount(): Int =
    1 + children.sumOf { node -> node.nodeCount() }
