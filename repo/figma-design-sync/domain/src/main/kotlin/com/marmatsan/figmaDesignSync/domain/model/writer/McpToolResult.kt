package com.marmatsan.figmaDesignSync.domain.model.writer

/** Language-neutral result returned by one MCP tool invocation. */
data class McpToolResult(
    val isError: Boolean,
    val text: String
)
