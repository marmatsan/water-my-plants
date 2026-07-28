package com.marmatsan.figmaDocumentationSync.domain.port.figma

import com.github.michaelbull.result.Result
import com.marmatsan.figmaDocumentationSync.domain.model.figma.FigmaNodeContent
import com.marmatsan.figmaDocumentationSync.domain.model.figma.FigmaNodeContentError

/** Consumer-owned boundary for reading the Figma node metadata needed by synchronization. */
fun interface FigmaNodeContentSource {
    /** Reads one node without exposing HTTP, Ktor, or wire DTO types to callers. */
    fun readNodeContent(
        fileKey: String,
        token: String,
        nodeId: String,
        pluginData: String?,
    ): Result<FigmaNodeContent, FigmaNodeContentError>
}
