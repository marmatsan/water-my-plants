package com.marmatsan.figmaDesignSync.domain.model.writer

/** Failed atomic runner unit details persisted for supervised retry. */
data class McpExecutionFailure(
    val message: String,
    val durationMs: Long,
    val failedAt: String
)
