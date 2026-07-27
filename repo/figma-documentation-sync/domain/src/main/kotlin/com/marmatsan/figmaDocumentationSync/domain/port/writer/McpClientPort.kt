package com.marmatsan.figmaDocumentationSync.domain.port.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpToolResult

/** Write-capable MCP operations used by the checkpointed executor. */
interface McpClientPort : AutoCloseable {
    /** Returns the distinct tools advertised by the connected MCP endpoint. */
    suspend fun listToolNames(): List<String>

    /** Reads the text resource identified by [uri]. */
    suspend fun readTextResource(
        uri: String,
    ): String

    /** Executes generated [code] against the target Figma [fileKey]. */
    suspend fun useFigma(
        fileKey: String,
        code: String,
        description: String,
        skillNames: String,
    ): McpToolResult

    /** Requests [count] single-use asset upload slots for [fileKey]. */
    suspend fun requestAssetUpload(
        fileKey: String,
        count: Int,
    ): McpToolResult

    /** Uploads canonical [bytes] to the single-use asset [url]. */
    suspend fun uploadAsset(
        url: String,
        bytes: ByteArray,
    )
}
