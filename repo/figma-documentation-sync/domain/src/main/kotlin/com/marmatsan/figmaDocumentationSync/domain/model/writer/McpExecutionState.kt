package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Durable checkpoint for deterministic MCP runner execution.
 *
 * @property schemaVersion checkpoint schema version.
 * @property identity cryptographic runner identity required for resume.
 * @property startedAt initial execution timestamp.
 * @property updatedAt most recent checkpoint timestamp.
 * @property completedFiles successful atomic runner units.
 * @property plannedFiles ordered files selected for execution.
 * @property failedFile runner file that most recently failed.
 * @property failure failure detail retained for supervised recovery.
 */
data class McpExecutionState(
    val schemaVersion: Int,
    val identity: McpExecutionIdentity,
    val startedAt: String,
    val updatedAt: String,
    val completedFiles: List<McpCompletedFile>,
    val plannedFiles: List<String>,
    val failedFile: String?,
    val failure: McpExecutionFailure? = null
)
