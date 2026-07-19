package com.marmatsan.figmaDocumentationSync.domain.model.writer

/** Model and compiled writer payload transported to Figma through a PNG asset. */
data class OfficialSyncPayload(
    val payloadSchemaVersion: Int,
    val designModelJson: String,
    val designModelHash: String,
    val designModelGitSha: String,
    val designModelLength: Int,
    val script: String,
    val scriptLength: Int,
    val writerHash: String,
    val transportHash: String
)
