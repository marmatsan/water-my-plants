package com.marmatsan.figmaCatalogChecks.data.figma.dto


import kotlinx.serialization.Serializable

@Serializable
data class FigmaNode(
    val id: String,
    val name: String,
    val type: String,
    val visible: Boolean = true,
    val characters: String? = null,
    val componentId: String? = null,
    val connectorStart: FigmaConnectorEndpoint? = null,
    val connectorEnd: FigmaConnectorEndpoint? = null,
    val componentPropertyReferences: Map<String, String> = emptyMap(),
    val componentProperties: Map<String, FigmaComponentProperty> = emptyMap(),
    val children: List<FigmaNode> = emptyList()
)
