package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Versioned, hashed execution plan for the canonical visual publication.
 *
 * @property body hashable plan decision and identity.
 * @property planHash canonical hash of [body].
 */
data class VisualSyncPlan(
    val body: VisualSyncPlanBody,
    val planHash: String,
)
