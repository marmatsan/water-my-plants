package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Versioned, hashed execution plan for the official visual publication. */
data class VisualSyncPlan(
    val body: VisualSyncPlanBody,
    val planHash: String,
)
