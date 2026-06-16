package com.marmatsan.figmaVersions

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

internal object FigmaFileVersionsReader {
    private const val VERSION_COMPONENT_NAME = ".project version"
    private const val VERSION_ALIAS_PROPERTY = "Version alias"

    fun read(
        response: FigmaFileResponse,
        pageName: String,
        sectionName: String
    ): Map<String, String> {
        return readSection(
            section = findSection(
                response = response,
                pageName = pageName,
                sectionName = sectionName
            ),
            sectionName = sectionName
        )
    }

    fun findSection(
        response: FigmaFileResponse,
        pageName: String,
        sectionName: String
    ): FigmaNode {
        val page = response.document
            .children
            .singleOrNull { node -> node.type == "CANVAS" && node.name == pageName }
            ?: error("Figma page '$pageName' was not found. Available pages: ${response.document.pageNames()}")

        val matchingSections = page
            .descendants()
            .filter { node -> node.type == "SECTION" && node.name == sectionName }
            .toList()

        val section = when (matchingSections.size) {
            0 -> error("Figma section '$sectionName' was not found in page '$pageName'")
            1 -> matchingSections.single()
            else -> error("Found ${matchingSections.size} Figma sections named '$sectionName' in page '$pageName'")
        }

        return section
    }

    fun readSection(
        section: FigmaNode,
        sectionName: String
    ): Map<String, String> {
        val componentVersions = section.readProjectVersionComponents(sectionName)

        return componentVersions
            .toSortedMap()
    }

    private fun FigmaNode.readProjectVersionComponents(
        sectionName: String
    ): Map<String, String> {
        val projectVersionComponents = descendants()
            .filter { node -> node.type == "INSTANCE" }
            .filter { node -> node.name == VERSION_COMPONENT_NAME }
            .toList()

        if (projectVersionComponents.isEmpty()) {
            error("Figma section '$sectionName' contains no '$VERSION_COMPONENT_NAME' instances")
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

    private fun FigmaNode.pageNames(): String {
        return children
            .filter { node -> node.type == "CANVAS" }
            .joinToString { node -> "'${node.name}'" }
            .ifBlank { "<none>" }
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
            ?: error("Figma '$VERSION_COMPONENT_NAME' instance '${id}' is missing '$VERSION_ALIAS_PROPERTY'")
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
            ?: error("Figma '$VERSION_COMPONENT_NAME' instance '${id}' has no rendered version number for '$versionAlias'")
    }
}
