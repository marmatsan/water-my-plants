package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Failed atomic runner unit details persisted for supervised retry.
 *
 * @property message failure detail returned by execution.
 * @property durationMs execution duration before failure.
 * @property failedAt failure timestamp in canonical string form.
 */
data class McpExecutionFailure(
    val message: String,
    val durationMs: Long,
    val failedAt: String,
)
