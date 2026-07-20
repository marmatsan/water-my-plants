package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Hashable visual sync decision before its canonical plan hash is attached. */
data class VisualSyncPlanBody(
    val schemaVersion: Int,
    val decision: VisualSyncDecision,
    val reason: String,
    val requiresVisualWrite: Boolean,
    val requiresMetadataWrite: Boolean,
    val executionScopes: List<String>,
    val identity: VisualSyncIdentity,
    val manifestHash: String,
)
