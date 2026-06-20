package com.marmatsan.figmaDesignSync.data.figma.dto


import kotlinx.serialization.Serializable

@Serializable
data class FigmaFileNode(
    val document: FigmaNode? = null
)
