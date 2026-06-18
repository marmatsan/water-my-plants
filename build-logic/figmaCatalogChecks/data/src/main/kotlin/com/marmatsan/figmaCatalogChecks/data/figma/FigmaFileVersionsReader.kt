package com.marmatsan.figmaCatalogChecks.data.figma

import com.marmatsan.figmaCatalogChecks.domain.model.*

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
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
        val projectVersionComponents = descendants()
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

    private fun FigmaNode.descendants(): Sequence<FigmaNode> = sequence {
        yield(this@descendants)
        children.forEach { child ->
            yieldAll(child.descendants())
        }
    }

    private fun Map<String, FigmaComponentProperty>.findProperty(
        name: String
    ): FigmaComponentProperty? {
        return entries
            .firstOrNull { (key, _) -> key == name || key.startsWith("$name#") }
            ?.value
    }

    private fun JsonElement.toPropertyValue(): String {
        return when (this) {
            is JsonPrimitive -> when {
                isString -> content
                booleanOrNull != null -> booleanOrNull.toString()
                else -> content
            }

            else -> toString()
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
        return descendants()
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
