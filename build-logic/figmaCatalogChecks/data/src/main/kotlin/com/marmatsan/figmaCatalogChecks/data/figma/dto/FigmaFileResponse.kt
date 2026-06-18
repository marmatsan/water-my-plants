package com.marmatsan.figmaCatalogChecks.data.figma.dto


import kotlinx.serialization.Serializable

@Serializable
data class FigmaFileResponse(
    val document: FigmaNode
)
