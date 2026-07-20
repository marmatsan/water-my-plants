package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Observable identity and execution scope of one generated MCP runner set. */
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
