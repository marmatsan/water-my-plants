package com.marmatsan.figmaCatalogChecks.data.figma.catalog.library

import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaFileLibraryCatalogTreeReader(
    private val nodeReader: FigmaLibraryCatalogNodeReader
) {
    fun readSection(
        section: FigmaNode,
        sectionNodeId: String
    ): LibraryCatalogTree {
        val rootSections = section.children.filter { node -> node.type == "SECTION" }

        if (rootSections.isEmpty()) {
            error("Figma library tree section '$sectionNodeId' contains no root sections")
        }

        return LibraryCatalogTree(
            roots = rootSections.flatMap(nodeReader::readRoots)
        )
    }
}
