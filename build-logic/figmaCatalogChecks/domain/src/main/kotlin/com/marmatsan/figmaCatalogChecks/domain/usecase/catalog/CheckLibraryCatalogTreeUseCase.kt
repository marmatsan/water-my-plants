package com.marmatsan.figmaCatalogChecks.domain.usecase.catalog

import com.marmatsan.figmaCatalogChecks.domain.comparison.catalog.LibraryCatalogTreeComparison
import com.marmatsan.figmaCatalogChecks.domain.comparison.usage.LibraryCatalogUsageComparison
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.FigmaCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.FigmaCatalogTreesPort
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreesPort
import com.marmatsan.figmaCatalogChecks.domain.port.usage.ProjectCatalogUsagePort
import com.marmatsan.figmaCatalogChecks.domain.port.usage.ProjectCatalogUsageSource
import me.tatarka.inject.annotations.Inject

@Inject
class CheckLibraryCatalogTreeUseCase(
    private val projectCatalogTreesPort: ProjectCatalogTreesPort,
    private val figmaCatalogTreesPort: FigmaCatalogTreesPort,
    private val projectCatalogUsagePort: ProjectCatalogUsagePort,
    private val libraryCatalogTreeComparison: LibraryCatalogTreeComparison,
    private val libraryCatalogUsageComparison: LibraryCatalogUsageComparison
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

        val projectRootPath = request.projectRootPath
        if (projectRootPath != null) {
            val usageComparison = libraryCatalogUsageComparison.compare(
                figmaTree = figmaTree,
                projectUsage = projectCatalogUsagePort.readProjectUsage(
                    ProjectCatalogUsageSource(rootDirPath = projectRootPath)
                )
            )

            if (!usageComparison.matches) {
                return LibraryCatalogTreeCheckResult.UsageMismatch(usageComparison)
            }
        }

        return LibraryCatalogTreeCheckResult.Match(
            sectionNodeId = request.section.nodeId,
            repositoryNodeCount = repositoryTree.nodeCount()
        )
    }
}

private fun LibraryCatalogTree.nodeCount(): Int =
    roots.sumOf { node -> node.nodeCount() }

private fun LibraryCatalogNode.nodeCount(): Int =
    1 + children.sumOf { node -> node.nodeCount() }
