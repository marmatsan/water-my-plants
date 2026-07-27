package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Language-neutral result returned by one MCP tool invocation.
 *
 * @property isError whether the MCP tool reported an error result.
 * @property text textual result content returned by the endpoint.
 */
data class McpToolResult(
    val isError: Boolean,
    val text: String,
)
