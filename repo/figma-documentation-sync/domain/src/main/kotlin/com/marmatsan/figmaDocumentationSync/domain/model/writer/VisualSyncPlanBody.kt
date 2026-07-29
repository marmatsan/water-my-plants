package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Hashable visual sync decision before its canonical plan hash is attached.
 *
 * @property schemaVersion visual plan schema version.
 * @property decision amount of visual work selected.
 * @property reason deterministic explanation for the decision.
 * @property requiresVisualWrite whether visual nodes must be updated.
 * @property requiresMetadataWrite whether canonical metadata must be updated.
 * @property executionScopes writer scopes selected for execution.
 * @property identity current model, writer, and transport identity.
 * @property manifestHash executable runner manifest hash.
 */
data class VisualSyncPlanBody(
    val schemaVersion: Int,
    val decision: VisualSyncDecision,
    val reason: String,
    val requiresVisualWrite: Boolean,
    val requiresMetadataWrite: Boolean,
    val executionScopes: List<String>,
    val identity: VisualSyncIdentity,
    val manifestHash: String
)
