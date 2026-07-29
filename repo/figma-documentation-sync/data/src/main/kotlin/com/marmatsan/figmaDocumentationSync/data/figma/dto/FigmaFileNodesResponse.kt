package com.marmatsan.figmaDocumentationSync.data.figma.dto

import kotlinx.serialization.Serializable

/**
 * Response body for Figma node content requests keyed by requested node id.
 *
 * @property nodes requested node wrappers keyed by request id.
 */
@Serializable
data class FigmaFileNodesResponse(
    val nodes: Map<String, FigmaFileNode>
)
