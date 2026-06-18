package com.marmatsan.figmaCatalogChecks.data.figma.dto


import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FigmaComponentProperty(
    val type: String,
    val value: JsonElement
)
