package com.marmatsan.figmaCatalogChecks.data.figma.catalog.plugin

import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaFilePluginCatalogTreeReader(
    private val nodeReader: FigmaPluginCatalogNodeReader
) {
    fun readSection(
        section: FigmaNode,
        sectionNodeId: String
    ): PluginCatalogTree {
        val rootSections = section.children.filter { node -> node.type == "SECTION" }

        if (rootSections.isEmpty()) {
            error("Figma plugin tree section '$sectionNodeId' contains no root sections")
        }

        return PluginCatalogTree(
            roots = rootSections.flatMap(nodeReader::readRoots)
        )
    }
}
