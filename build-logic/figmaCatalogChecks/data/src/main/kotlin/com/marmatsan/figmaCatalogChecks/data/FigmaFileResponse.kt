package com.marmatsan.figmaCatalogChecks.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class FigmaFileResponse(
    val document: FigmaNode
)

@Serializable
data class FigmaFileNodesResponse(
    val nodes: Map<String, FigmaFileNode>
)

@Serializable
data class FigmaFileNode(
    val document: FigmaNode? = null
)

@Serializable
data class FigmaNode(
    val id: String,
    val name: String,
    val type: String,
    val characters: String? = null,
    val componentId: String? = null,
    val componentProperties: Map<String, FigmaComponentProperty> = emptyMap(),
    val children: List<FigmaNode> = emptyList()
)

@Serializable
data class FigmaComponentProperty(
    val type: String,
    val value: JsonElement
)
