package com.marmatsan.figmaDesignSync.data.figma.dto


import kotlinx.serialization.Serializable

@Serializable
data class FigmaNode(
    val id: String,
    val name: String,
    val type: String,
    val visible: Boolean = true,
    val characters: String? = null,
    val sharedPluginData: Map<String, Map<String, String>> = emptyMap(),
    val children: List<FigmaNode> = emptyList()
)
