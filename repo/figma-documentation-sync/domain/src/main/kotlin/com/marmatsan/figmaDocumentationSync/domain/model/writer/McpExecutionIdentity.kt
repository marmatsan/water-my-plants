package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Cryptographic identity that makes checkpoints safe to resume. */
data class McpExecutionIdentity(
    val modelHash: String,
    val gitSha: String,
    val writerHash: String,
    val transportHash: String,
    val manifestHash: String,
)
