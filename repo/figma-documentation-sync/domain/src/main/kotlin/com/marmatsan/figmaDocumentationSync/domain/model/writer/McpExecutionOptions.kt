package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Selection flags applied before an MCP runner is executed. */
data class McpExecutionOptions(
    val resume: Boolean = false,
    val retryFailed: Boolean = false,
    val reuseStaging: Boolean = false,
    val from: String? = null
)
