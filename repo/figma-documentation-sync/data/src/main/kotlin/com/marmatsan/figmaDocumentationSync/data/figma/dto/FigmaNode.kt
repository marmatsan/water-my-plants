package com.marmatsan.figmaDocumentationSync.data.figma.dto

import kotlinx.serialization.Serializable

/**
 * Minimal Figma node DTO needed by the sync checker.
 *
 * The API exposes many more fields; unknown fields are ignored by
 * [com.marmatsan.figmaDocumentationSync.data.figma.client.FigmaFileContentClient].
 */
@Serializable
data class FigmaNode(
    val id: String,
    val name: String,
    val type: String,
    val visible: Boolean = true,
    val characters: String? = null,
    val sharedPluginData: Map<String, Map<String, String>> = emptyMap(),
    val children: List<FigmaNode> = emptyList(),
)
