package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Successful atomic runner unit persisted in an execution checkpoint.
 *
 * @property file runner file that completed successfully.
 * @property fileHash content hash verified before execution.
 * @property durationMs execution duration in milliseconds.
 * @property completedAt completion timestamp in canonical string form.
 * @property summary optional MCP result summary.
 */
data class McpCompletedFile(
    val file: String,
    val fileHash: String,
    val durationMs: Long,
    val completedAt: String,
    val summary: String?
)
