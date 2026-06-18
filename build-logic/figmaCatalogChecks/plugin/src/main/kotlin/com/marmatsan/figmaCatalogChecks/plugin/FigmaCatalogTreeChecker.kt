package com.marmatsan.figmaCatalogChecks.plugin

import com.marmatsan.figmaCatalogChecks.data.DependenciesCatalogTreesReader
import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentClient
import com.marmatsan.figmaCatalogChecks.data.FigmaFileContentException
import com.marmatsan.figmaCatalogChecks.data.FigmaFileLibraryCatalogTreeReader
import com.marmatsan.figmaCatalogChecks.data.FigmaFilePluginCatalogTreeReader
import com.marmatsan.figmaCatalogChecks.data.FigmaNodeUrl
import com.marmatsan.figmaCatalogChecks.domain.LibraryCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.LibraryCatalogTreeComparison
import com.marmatsan.figmaCatalogChecks.domain.PluginCatalogNode
import com.marmatsan.figmaCatalogChecks.domain.PluginCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.PluginCatalogTreeComparison
import me.tatarka.inject.annotations.Inject
import org.gradle.api.GradleException
import java.io.File

internal data class FigmaCatalogTreeCheckRequest(
    val pageUrl: String,
    val sectionUrl: String,
    val projectRootDir: File,
    val token: String
)

internal data class FigmaCatalogTreeTaskResult(
    val sectionNodeId: String,
    val repositoryNodeCount: Int
)

@Inject
internal class FigmaCatalogTreeChecker(
    private val dependenciesCatalogTreesReader: DependenciesCatalogTreesReader,
    private val figmaFileContentClient: FigmaFileContentClient,
    private val figmaFileLibraryCatalogTreeReader: FigmaFileLibraryCatalogTreeReader,
    private val figmaFilePluginCatalogTreeReader: FigmaFilePluginCatalogTreeReader,
    private val libraryCatalogTreeComparison: LibraryCatalogTreeComparison,
    private val pluginCatalogTreeComparison: PluginCatalogTreeComparison
) {
    fun checkLibraryTree(
        request: FigmaCatalogTreeCheckRequest
    ): FigmaCatalogTreeTaskResult {
        try {
            val page = FigmaNodeUrl.parse(request.pageUrl)
            val section = FigmaNodeUrl.parse(request.sectionUrl)
            ensureSameFile(page.fileKey, section.fileKey)

            val repositoryTree = dependenciesCatalogTreesReader.readLibraryTreeWithVersionAliases()
            val figmaTree = figmaFileLibraryCatalogTreeReader.readSection(
                section = figmaFileContentClient.getNodeContent(
                    fileKey = section.fileKey,
                    token = request.token,
                    nodeId = section.nodeId
                ),
                sectionNodeId = section.nodeId
            )
            val comparison = libraryCatalogTreeComparison.compare(
                repositoryTree = repositoryTree,
                figmaTree = figmaTree
            )

            if (!comparison.matches) {
                throw GradleException(comparison.report())
            }

            return FigmaCatalogTreeTaskResult(
                sectionNodeId = section.nodeId,
                repositoryNodeCount = repositoryTree.nodeCount()
            )
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
            ensureSameFile(page.fileKey, section.fileKey)

            val repositoryTree = dependenciesCatalogTreesReader.readPluginTreeWithVersionAliases()
            val figmaTree = figmaFilePluginCatalogTreeReader.readSection(
                section = figmaFileContentClient.getNodeContent(
                    fileKey = section.fileKey,
                    token = request.token,
                    nodeId = section.nodeId
                ),
                sectionNodeId = section.nodeId
            )
            val comparison = pluginCatalogTreeComparison.compare(
                repositoryTree = repositoryTree,
                figmaTree = figmaTree
            )

            if (!comparison.matches) {
                throw GradleException(comparison.report())
            }

            return FigmaCatalogTreeTaskResult(
                sectionNodeId = section.nodeId,
                repositoryNodeCount = repositoryTree.nodeCount()
            )
        } catch (exception: FigmaFileContentException) {
            throw GradleException(
                exception.message ?: "Figma file content request failed",
                exception
            )
        }
    }

    private fun ensureSameFile(
        pageFileKey: String,
        sectionFileKey: String
    ) {
        if (pageFileKey != sectionFileKey) {
            throw GradleException(
                "Figma URLs must point to the same file. Found file keys: $pageFileKey, $sectionFileKey"
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
}
