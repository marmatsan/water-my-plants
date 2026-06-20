package com.marmatsan.figmaDesignSync.data.figma.dto


import kotlinx.serialization.Serializable

@Serializable
data class FigmaFileNodesResponse(
    val nodes: Map<String, FigmaFileNode>
)
