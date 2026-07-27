package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Previous writer identity persisted as shared plugin data in Figma.
 *
 * @property modelHash last synchronized design-model hash.
 * @property writerHash last synchronized writer behavior hash.
 * @property targetFingerprints last synchronized visual target fingerprints.
 * @property writerScopeFingerprints last synchronized writer-scope fingerprints.
 * @property writerScopeFingerprintSchemaVersion schema used to calculate stored fingerprints.
 */
data class FigmaSyncMetadata(
    val modelHash: String?,
    val writerHash: String?,
    val targetFingerprints: Map<String, String>?,
    val writerScopeFingerprints: Map<String, String>?,
    val writerScopeFingerprintSchemaVersion: Int?,
)
