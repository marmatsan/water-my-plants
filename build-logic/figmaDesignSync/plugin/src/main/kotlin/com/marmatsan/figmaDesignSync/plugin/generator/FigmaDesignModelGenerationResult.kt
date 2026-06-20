package com.marmatsan.figmaDesignSync.plugin.generator

import kotlinx.serialization.json.JsonObject

internal data class FigmaDesignModelGenerationResult(
    val model: JsonObject,
    val modelHash: String
)
