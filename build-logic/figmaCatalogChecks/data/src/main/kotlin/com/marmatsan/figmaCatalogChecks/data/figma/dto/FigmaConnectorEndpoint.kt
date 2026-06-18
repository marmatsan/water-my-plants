package com.marmatsan.figmaCatalogChecks.data.figma.dto


import kotlinx.serialization.Serializable

@Serializable
data class FigmaConnectorEndpoint(
    val endpointNodeId: String? = null,
    val magnet: String? = null
)
