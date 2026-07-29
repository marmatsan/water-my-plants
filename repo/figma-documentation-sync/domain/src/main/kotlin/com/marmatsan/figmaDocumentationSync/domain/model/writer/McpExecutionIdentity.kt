package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Cryptographic identity that makes checkpoints safe to resume.
 *
 * @property modelHash design-model hash.
 * @property gitSha Git revision represented by the runner.
 * @property writerHash writer behavior hash.
 * @property transportHash MCP transport behavior hash.
 * @property manifestHash complete executable runner manifest hash.
 */
data class McpExecutionIdentity(
    val modelHash: String,
    val gitSha: String,
    val writerHash: String,
    val transportHash: String,
    val manifestHash: String
)
