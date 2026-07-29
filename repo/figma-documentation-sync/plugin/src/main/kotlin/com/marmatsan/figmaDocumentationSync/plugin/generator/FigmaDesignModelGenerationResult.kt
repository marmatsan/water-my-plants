package com.marmatsan.figmaDocumentationSync.plugin.generator

import kotlinx.serialization.json.JsonObject

/**
 * Generated design model and its stable content hash.
 *
 * [model] is written to `design-model.json`. [modelHash] is also stored in
 * Figma shared plugin data so CI can verify that the Figma document was synced
 * from the same repository snapshot.
 *
 * @property model canonical JSON design model produced from repository sources.
 * @property modelHash deterministic content hash embedded in the model and Figma metadata.
 */
internal data class FigmaDesignModelGenerationResult(
    val model: JsonObject,
    val modelHash: String
)
