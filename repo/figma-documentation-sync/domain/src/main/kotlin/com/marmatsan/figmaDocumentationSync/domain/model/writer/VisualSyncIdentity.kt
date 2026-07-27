package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Current model, writer, and transport identity evaluated by the visual plan.
 *
 * @property modelHash current design-model hash.
 * @property writerHash current writer behavior hash.
 * @property transportHash current MCP transport behavior hash.
 * @property writerScopeFingerprintSchemaVersion schema used for writer-scope fingerprints.
 */
data class VisualSyncIdentity(
    val modelHash: String,
    val writerHash: String,
    val transportHash: String,
    val writerScopeFingerprintSchemaVersion: Int,
)
