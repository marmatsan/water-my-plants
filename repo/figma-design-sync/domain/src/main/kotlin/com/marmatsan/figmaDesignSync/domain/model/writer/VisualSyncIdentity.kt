package com.marmatsan.figmaDesignSync.domain.model.writer

/** Current model, writer, and transport identity evaluated by the visual plan. */
data class VisualSyncIdentity(
    val modelHash: String,
    val writerHash: String,
    val transportHash: String,
    val writerScopeFingerprintSchemaVersion: Int
)
