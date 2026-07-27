package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Observable identity and execution scope of one generated MCP runner set.
 *
 * @property path repository-relative runner directory.
 * @property fullVisualSync whether the runner performs the complete visual write.
 * @property writeMetadata whether the runner writes canonical metadata.
 * @property modelHash canonical design-model hash.
 * @property writerHash writer behavior hash.
 * @property transportHash MCP transport behavior hash.
 * @property targetFingerprints visual target fingerprints keyed by target.
 * @property writerScopeFingerprints writer-scope fingerprints keyed by scope.
 * @property writerScopeFingerprintSchemaVersion fingerprint calculation schema version.
 * @property executionScopes file-to-execution-scope mapping.
 * @property manifestHash hash covering the runner manifest identity.
 */
data class RunnerManifest(
    val path: String,
    val fullVisualSync: Boolean,
    val writeMetadata: Boolean,
    val modelHash: String,
    val writerHash: String,
    val transportHash: String,
    val targetFingerprints: Map<String, String>,
    val writerScopeFingerprints: Map<String, String>,
    val writerScopeFingerprintSchemaVersion: Int,
    val executionScopes: Map<String, String>,
    val manifestHash: String,
)
