package com.marmatsan.figmaCatalogChecks.data.figma

import com.marmatsan.figmaCatalogChecks.domain.model.*

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
    val visible: Boolean = true,
    val characters: String? = null,
    val componentId: String? = null,
    val absoluteBoundingBox: FigmaRectangle? = null,
    val connectorStart: FigmaConnectorEndpoint? = null,
    val connectorEnd: FigmaConnectorEndpoint? = null,
    val componentPropertyReferences: Map<String, String> = emptyMap(),
    val componentProperties: Map<String, FigmaComponentProperty> = emptyMap(),
    val children: List<FigmaNode> = emptyList()
)

@Serializable
data class FigmaComponentProperty(
    val type: String,
    val value: JsonElement
)

@Serializable
data class FigmaRectangle(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double
)

@Serializable
data class FigmaConnectorEndpoint(
    val endpointNodeId: String? = null,
    val magnet: String? = null
)
