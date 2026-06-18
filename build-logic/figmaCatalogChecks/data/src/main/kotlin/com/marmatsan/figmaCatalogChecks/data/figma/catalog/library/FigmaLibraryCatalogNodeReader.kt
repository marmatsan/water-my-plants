package com.marmatsan.figmaCatalogChecks.data.figma.catalog.library

import com.marmatsan.figmaCatalogChecks.data.figma.common.booleanProperty
import com.marmatsan.figmaCatalogChecks.data.figma.common.childIdsByParentId
import com.marmatsan.figmaCatalogChecks.data.figma.common.getValueOrEmpty
import com.marmatsan.figmaCatalogChecks.data.figma.common.parentByChildId
import com.marmatsan.figmaCatalogChecks.data.figma.common.requiredTextProperty
import com.marmatsan.figmaCatalogChecks.data.figma.common.textProperty
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogNode
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaLibraryCatalogNodeReader(
    private val entryReader: FigmaLibraryCatalogEntryReader
) {
    fun readRoots(rootSection: FigmaNode): List<LibraryCatalogNode> {
        val treeNodes = rootSection.children
            .filter { node -> node.visible && node.isLibraryTreeNode() }
            .associateBy(FigmaNode::id)

        if (treeNodes.isEmpty()) {
            error("Figma library root section '${rootSection.id}' contains no library tree nodes")
        }

        val parentByChildId = rootSection.children.parentByChildId(treeNodes)
        val childIdsByParentId = parentByChildId.childIdsByParentId()

        return treeNodes
            .values
            .filter { node -> node.id !in parentByChildId }
            .sortedWith(compareBy<FigmaNode>({ node -> node.libraryGroup() }, FigmaNode::id))
            .map { rootNode ->
                rootNode.toLibraryCatalogNode(
                    treeNodes = treeNodes,
                    childIdsByParentId = childIdsByParentId,
                    visitedNodeIds = emptySet()
                )
            }
    }

    private fun FigmaNode.toLibraryCatalogNode(
        treeNodes: Map<String, FigmaNode>,
        childIdsByParentId: Map<String, List<String>>,
        visitedNodeIds: Set<String>
    ): LibraryCatalogNode {
        check(id !in visitedNodeIds) {
            "Cycle found in Figma library tree at node '$id'"
        }

        val entries = entryReader.readEntries(this)
        val nextVisitedNodeIds = visitedNodeIds + id

        return LibraryCatalogNode(
            group = libraryGroup(),
            entries = entries,
            artifactsVisible = showArtifacts(default = entries.isNotEmpty()),
            children = childIdsByParentId
                .getValueOrEmpty(id)
                .mapNotNull(treeNodes::get)
                .sortedWith(compareBy<FigmaNode>({ node -> node.libraryGroup() }, FigmaNode::id))
                .map { child ->
                    child.toLibraryCatalogNode(
                        treeNodes = treeNodes,
                        childIdsByParentId = childIdsByParentId,
                        visitedNodeIds = nextVisitedNodeIds
                    )
                }
        )
    }

    private fun FigmaNode.isLibraryTreeNode(): Boolean =
        type == "INSTANCE" &&
            name.equals(".tree node", ignoreCase = true) &&
            textProperty(TYPE_PROPERTY) == LIBRARY_TYPE

    private fun FigmaNode.libraryGroup(): String =
        requiredTextProperty(LIBRARY_GROUP_PROPERTY)

    private fun FigmaNode.showArtifacts(default: Boolean): Boolean =
        booleanProperty(SHOW_ARTIFACTS_PROPERTY, default)

    private companion object {
        const val TYPE_PROPERTY = "Type"
        const val LIBRARY_TYPE = "Library"
        const val LIBRARY_GROUP_PROPERTY = "Library group"
        const val SHOW_ARTIFACTS_PROPERTY = "Show artifacts"
    }
}
