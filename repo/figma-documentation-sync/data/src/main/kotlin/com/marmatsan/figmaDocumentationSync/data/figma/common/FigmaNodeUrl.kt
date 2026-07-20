package com.marmatsan.figmaDocumentationSync.data.figma.common

import com.marmatsan.figmaDocumentationSync.domain.model.figma.FigmaNodeReference
import java.net.URI

/**
 * Parser for Figma design URLs used by Gradle configuration.
 *
 * It extracts the file key and converts Figma's URL-safe `node-id` query value
 * into the colon-separated node id expected by the Figma API.
 */
object FigmaNodeUrl {
    /**
     * Parses URLs shaped like
     * `https://www.figma.com/design/<file-key>/<name>?node-id=123-456`.
     */
    fun parse(
        url: String,
    ): FigmaNodeReference {
        val uri = URI(url)
        val pathSegments =
            uri.path
                .split("/")
                .filter(String::isNotBlank)
        val designIndex = pathSegments.indexOf("design")
        if (designIndex == -1) {
            error("Figma URL '$url' is not a design URL")
        }

        val fileKey =
            pathSegments.getOrNull(designIndex + 1)
                ?: error("Figma URL '$url' does not contain a design file key")
        val nodeId =
            uri.query
                ?.split("&")
                ?.mapNotNull { parameter ->
                    val parts =
                        parameter.split(
                            "=",
                            limit = 2,
                        )
                    if (parts.firstOrNull() == "node-id") parts.getOrNull(1) else null
                }?.firstOrNull()
                ?.replace(
                    '-',
                    ':',
                )
                ?: error("Figma URL '$url' does not contain a node-id query parameter")

        return FigmaNodeReference(
            fileKey = fileKey,
            nodeId = nodeId,
        )
    }
}
