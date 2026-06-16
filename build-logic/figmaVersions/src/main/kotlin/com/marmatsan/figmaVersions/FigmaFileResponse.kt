package com.marmatsan.figmaVersions

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class FigmaFileResponse(
    val document: FigmaNode
)

@Serializable
internal data class FigmaFileNodesResponse(
    val nodes: Map<String, FigmaFileNode>
)

@Serializable
internal data class FigmaFileNode(
    val document: FigmaNode? = null
)

@Serializable
internal data class FigmaNode(
    val id: String,
    val name: String,
    val type: String,
    val characters: String? = null,
    val componentProperties: Map<String, FigmaComponentProperty> = emptyMap(),
    val children: List<FigmaNode> = emptyList()
)

@Serializable
internal data class FigmaComponentProperty(
    val type: String,
    val value: JsonElement
)
