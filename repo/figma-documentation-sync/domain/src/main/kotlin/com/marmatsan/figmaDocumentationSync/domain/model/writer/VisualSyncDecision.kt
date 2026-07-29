package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Amount of canonical visual work selected from current and previous identities.
 *
 * @property wireValue serialized decision persisted in canonical artifacts.
 */
enum class VisualSyncDecision(
    val wireValue: String
) {
    FULL("full"),
    PARTIAL("partial"),
    NONE("none")
}
