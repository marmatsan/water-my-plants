package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Successful atomic runner unit persisted in an execution checkpoint. */
data class McpCompletedFile(
    val file: String,
    val fileHash: String,
    val durationMs: Long,
    val completedAt: String,
    val summary: String?
)
