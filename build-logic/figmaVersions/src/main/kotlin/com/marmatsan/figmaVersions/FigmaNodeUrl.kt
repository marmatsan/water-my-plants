package com.marmatsan.figmaVersions

import java.net.URI

internal data class FigmaNodeUrl(
    val fileKey: String,
    val nodeId: String
) {
    companion object {
        fun parse(url: String): FigmaNodeUrl {
            val uri = URI(url)
            val pathSegments = uri.path
                .split("/")
                .filter(String::isNotBlank)
            val designIndex = pathSegments.indexOf("design")
            if (designIndex == -1) {
                error("Figma URL '$url' is not a design URL")
            }

            val fileKey = pathSegments.getOrNull(designIndex + 1)
                ?: error("Figma URL '$url' does not contain a design file key")
            val nodeId = uri.query
                ?.split("&")
                ?.mapNotNull { parameter ->
                    val parts = parameter.split("=", limit = 2)
                    if (parts.firstOrNull() == "node-id") parts.getOrNull(1) else null
                }
                ?.firstOrNull()
                ?.replace('-', ':')
                ?: error("Figma URL '$url' does not contain a node-id query parameter")

            return FigmaNodeUrl(
                fileKey = fileKey,
                nodeId = nodeId
            )
        }
    }
}
