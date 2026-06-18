package com.marmatsan.figmaCatalogChecks.data.figma.common

import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaComponentProperty
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

internal fun FigmaNode.requiredTextProperty(name: String): String =
    textProperty(name)
        ?.takeIf(String::isNotBlank)
        ?: error("Figma node '$id' is missing '$name'")

internal fun FigmaNode.textProperty(name: String): String? =
    componentProperties
        .findPropertyEntry(name)
        ?.let { (propertyKey, property) ->
            renderedTextPropertyValue(propertyKey)
                ?: property.value.toPropertyValue().trim()
        }

internal fun FigmaNode.booleanProperty(
    name: String,
    default: Boolean
): Boolean =
    componentProperties
        .findPropertyEntry(name)
        ?.value
        ?.value
        ?.toBooleanPropertyValue()
        ?: default

internal fun FigmaNode.renderedTextValues(): Sequence<String> =
    visibleDescendants()
        .filter { node -> node.type == "TEXT" }
        .map { node -> node.characters.orEmpty().trim() }
        .filter(String::isNotBlank)

internal fun FigmaNode.renderedTextPropertyValue(propertyKey: String): String? =
    visibleDescendants()
        .filter { node -> node.type == "TEXT" }
        .firstOrNull { node -> node.componentPropertyReferences["characters"] == propertyKey }
        ?.characters
        ?.trim()
        ?.takeIf(String::isNotBlank)

internal fun Map<String, FigmaComponentProperty>.findPropertyEntry(
    name: String
): Map.Entry<String, FigmaComponentProperty>? =
    entries
        .firstOrNull { (key, _) -> key == name || key.startsWith("$name#") }

internal fun Map<String, FigmaComponentProperty>.findProperty(
    name: String
): FigmaComponentProperty? =
    findPropertyEntry(name)?.value

internal fun JsonElement.toPropertyValue(): String =
    when (this) {
        is JsonPrimitive -> when {
            isString -> content
            booleanOrNull != null -> booleanOrNull.toString()
            else -> content
        }

        else -> toString()
    }

internal fun JsonElement.toBooleanPropertyValue(): Boolean? =
    when (this) {
        is JsonPrimitive -> booleanOrNull ?: content.toBooleanStrictOrNull()
        else -> null
    }
