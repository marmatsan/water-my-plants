package com.marmatsan.figmaDesignSync.domain.port.writer

import com.marmatsan.figmaDesignSync.domain.model.writer.McpToolResult

/** Write-capable MCP operations used by the checkpointed executor. */
interface McpClientPort : AutoCloseable {
    suspend fun listToolNames(): List<String>

    suspend fun readTextResource(uri: String): String

    suspend fun useFigma(
        fileKey: String,
        code: String,
        description: String,
        skillNames: String
    ): McpToolResult

    suspend fun requestAssetUpload(fileKey: String, count: Int): McpToolResult

    suspend fun uploadAsset(url: String, bytes: ByteArray)
}
