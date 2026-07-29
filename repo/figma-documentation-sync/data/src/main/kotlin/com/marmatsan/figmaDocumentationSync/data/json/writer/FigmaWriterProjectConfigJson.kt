package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.data.json.writer.config.FigmaWriterProjectConfigJsonProjector
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterProjectConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/** Projects [FigmaWriterProjectConfig] to the language-neutral writer schema. */
object FigmaWriterProjectConfigJson {
    private val prettyJson = Json { prettyPrint = true }
    private val projector = FigmaWriterProjectConfigJsonProjector()

    /** Encodes [config] as the portable writer's canonical JSON projection. */
    fun encode(
        config: FigmaWriterProjectConfig
    ): String =
        prettyJson.encodeToString(
            JsonObject.serializer(),
            projector.project(config)
        ) + System.lineSeparator()
}
