package com.marmatsan.figmaDesignSync.plugin.generator

import kotlinx.serialization.json.JsonObject

/**
 * Generated design model and its stable content hash.
 *
 * [model] is written to `design-model.json`. [modelHash] is also stored in
 * Figma shared plugin data so CI can verify that the Figma document was synced
 * from the same repository snapshot.
 */
internal data class FigmaDesignModelGenerationResult(
    val model: JsonObject,
    val modelHash: String
)
