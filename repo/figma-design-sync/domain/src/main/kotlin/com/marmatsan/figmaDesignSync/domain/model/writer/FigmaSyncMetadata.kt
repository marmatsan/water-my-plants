package com.marmatsan.figmaDesignSync.domain.model.writer

/** Previous writer identity persisted as shared plugin data in Figma. */
data class FigmaSyncMetadata(
    val modelHash: String?,
    val writerHash: String?,
    val targetFingerprints: Map<String, String>?,
    val writerScopeFingerprints: Map<String, String>?,
    val writerScopeFingerprintSchemaVersion: Int?
)
