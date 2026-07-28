package com.marmatsan.figmaDocumentationSync.data.json.writer.config

import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaCatalogTreeTargetConfig
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterProjectConfig
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject

/** Derived values shared by ordered writer-config JSON sections. */
internal data class FigmaWriterProjectConfigJsonContext(
    /** Typed writer configuration being projected. */
    val config: FigmaWriterProjectConfig,
    /** Catalog target names reused by catalog and aggregate target fields. */
    val catalogTargetNames: List<String>,
)

/** One cohesive ordered section of the portable writer-config JSON schema. */
internal fun interface FigmaWriterProjectConfigJsonSection {
    /** Appends this section's fields to [json] using values from [context]. */
    fun write(
        context: FigmaWriterProjectConfigJsonContext,
        json: JsonObjectBuilder,
    )
}

/** Projects typed writer configuration through the ordered schema sections. */
internal class FigmaWriterProjectConfigJsonProjector(
    private val sections: List<FigmaWriterProjectConfigJsonSection> =
        listOf(
            WriterIdentityConfigJsonSection,
            WriterCiConfigJsonSection,
            WriterVersionsConfigJsonSection,
            WriterVisualStructureConfigJsonSection,
            WriterRepositoryConfigJsonSection,
            WriterCatalogConfigJsonSection,
        ),
) {
    /** Builds the canonical language-neutral JSON object for [config]. */
    fun project(
        config: FigmaWriterProjectConfig,
    ): JsonObject {
        val context =
            FigmaWriterProjectConfigJsonContext(
                config = config,
                catalogTargetNames =
                    config.catalogTreeTargets.map(
                        transform = FigmaCatalogTreeTargetConfig::name,
                    ),
            )
        return buildJsonObject {
            sections.forEach { section ->
                section.write(
                    context = context,
                    json = this,
                )
            }
        }
    }
}
