package com.marmatsan.figmaDocumentationSync.domain.model.writer

/**
 * Model and compiled writer payload transported to Figma through a PNG asset.
 *
 * @property payloadSchemaVersion transport payload schema version.
 * @property designModelJson canonical serialized design model.
 * @property designModelHash hash of the serialized design model.
 * @property designModelGitSha Git revision represented by the model.
 * @property designModelLength serialized model byte length.
 * @property script compiled writer script.
 * @property scriptLength compiled script byte length.
 * @property writerHash hash of writer behavior affecting visual output.
 * @property transportHash hash of payload transport behavior.
 */
data class CanonicalSyncPayload(
    val payloadSchemaVersion: Int,
    val designModelJson: String,
    val designModelHash: String,
    val designModelGitSha: String,
    val designModelLength: Int,
    val script: String,
    val scriptLength: Int,
    val writerHash: String,
    val transportHash: String,
)
