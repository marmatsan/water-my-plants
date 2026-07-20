package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Durable checkpoint for deterministic MCP runner execution. */
data class McpExecutionState(
    val schemaVersion: Int,
    val identity: McpExecutionIdentity,
    val startedAt: String,
    val updatedAt: String,
    val completedFiles: List<McpCompletedFile>,
    val plannedFiles: List<String>,
    val failedFile: String?,
    val failure: McpExecutionFailure? = null,
)
