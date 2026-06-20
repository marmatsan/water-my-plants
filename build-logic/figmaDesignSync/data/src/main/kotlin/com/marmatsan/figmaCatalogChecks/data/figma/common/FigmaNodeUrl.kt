package com.marmatsan.figmaDesignSync.data.figma.common

import com.marmatsan.figmaDesignSync.domain.model.figma.FigmaNodeReference
import java.net.URI

object FigmaNodeUrl {
    fun parse(url: String): FigmaNodeReference {
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

        return FigmaNodeReference(
            fileKey = fileKey,
            nodeId = nodeId
        )
    }
}
