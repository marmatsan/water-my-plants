package com.marmatsan.figmaDocumentationSync.data.figma.dto

import kotlinx.serialization.Serializable

/**
 * Minimal Figma node DTO needed by the sync checker.
 *
 * The API exposes many more fields; unknown fields are ignored by
 * [com.marmatsan.figmaDocumentationSync.data.figma.client.FigmaFileContentClient].
 *
 * @property id stable Figma node id.
 * @property name current Figma layer name.
 * @property type Figma node type wire value.
 * @property visible whether the node participates in visible rendering.
 * @property characters text content for text nodes.
 * @property sharedPluginData plugin data keyed by namespace and key.
 * @property children recursively decoded child nodes.
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
