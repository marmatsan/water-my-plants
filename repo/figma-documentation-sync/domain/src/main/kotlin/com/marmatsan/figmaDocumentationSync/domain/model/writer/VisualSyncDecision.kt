package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Amount of official visual work selected from current and previous identities. */
enum class VisualSyncDecision(
    val wireValue: String
) {
    FULL("full"),
    PARTIAL("partial"),
    NONE("none")
}
