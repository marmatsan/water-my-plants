package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Write-relevant tools advertised by an MCP endpoint.
 *
 * @property toolNames all advertised MCP tool names.
 * @property canUseFigma whether the endpoint exposes the Figma execution tool.
 * @property canUploadAssets whether the endpoint accepts asset uploads.
 * @property writeCapable whether all capabilities required for visual writes are present.
 */
data class McpCapabilities(
    val toolNames: List<String>,
    val canUseFigma: Boolean,
    val canUploadAssets: Boolean,
) {
    val writeCapable: Boolean = canUseFigma && canUploadAssets
}
