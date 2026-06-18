package com.marmatsan.figmaCatalogChecks.data.figma.versions

import com.marmatsan.figmaCatalogChecks.data.figma.common.allDescendants
import com.marmatsan.figmaCatalogChecks.data.figma.common.findProperty
import com.marmatsan.figmaCatalogChecks.data.figma.common.toPropertyValue
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import me.tatarka.inject.annotations.Inject

@Inject
class FigmaFileVersionsReader {
    private companion object {
        const val VERSION_ALIAS_PROPERTY = "Version alias"
    }

    fun readSection(
        section: FigmaNode,
        sectionNodeId: String,
        versionComponentNodeId: String
    ): Map<String, String> {
        val componentVersions = section.readProjectVersionComponents(
            sectionNodeId = sectionNodeId,
            versionComponentNodeId = versionComponentNodeId
        )

        return componentVersions
            .toSortedMap()
    }

    private fun FigmaNode.readProjectVersionComponents(
        sectionNodeId: String,
        versionComponentNodeId: String
    ): Map<String, String> {
        val projectVersionComponents = allDescendants()
            .filter { node -> node.type == "INSTANCE" }
            .filter { node -> node.componentId == versionComponentNodeId }
            .toList()

        if (projectVersionComponents.isEmpty()) {
            error(
                "Figma section '$sectionNodeId' contains no instances of component '$versionComponentNodeId'"
            )
        }

        return projectVersionComponents
            .associate { node ->
                val versionAlias = node.versionAlias()
                val versionNumber = node.renderedVersionNumber(versionAlias)

                versionAlias to versionNumber
            }
    }

    private fun FigmaNode.versionAlias(): String {
        return componentProperties
            .findProperty(VERSION_ALIAS_PROPERTY)
            ?.value
            ?.toPropertyValue()
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: error("Figma version instance '${id}' is missing '$VERSION_ALIAS_PROPERTY'")
    }

    private fun FigmaNode.renderedVersionNumber(
        versionAlias: String
    ): String {
        return allDescendants()
            .filter { node -> node.type == "TEXT" }
            .map { node -> node.characters.orEmpty().trim() }
            .firstOrNull { text ->
                text.isNotBlank() &&
                    text != versionAlias &&
                    text.any(Char::isDigit) &&
                    !text.contains("Version", ignoreCase = true)
            }
            ?: error("Figma version instance '${id}' has no rendered version number for '$versionAlias'")
    }
}
