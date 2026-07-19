package com.marmatsan.figmaDocumentationSync.data.figma.dto


import kotlinx.serialization.Serializable

/**
 * Node wrapper returned by Figma's `/files/{fileKey}/nodes` endpoint.
 *
 * Figma may omit [document] for missing or inaccessible nodes, so callers must
 * validate it before using the node content.
 */
@Serializable
data class FigmaFileNode(
    val document: FigmaNode? = null
)
