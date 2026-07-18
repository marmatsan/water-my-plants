package com.marmatsan.figmaDesignSync.domain.model.writer

/** Write-relevant tools advertised by an MCP endpoint. */
data class McpCapabilities(
    val toolNames: List<String>,
    val canUseFigma: Boolean,
    val canUploadAssets: Boolean
) {
    val writeCapable: Boolean = canUseFigma && canUploadAssets
}
