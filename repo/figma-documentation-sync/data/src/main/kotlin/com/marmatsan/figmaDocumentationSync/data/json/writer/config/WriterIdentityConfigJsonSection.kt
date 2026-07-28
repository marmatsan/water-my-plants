package com.marmatsan.figmaDocumentationSync.data.json.writer.config

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

/** Writes schema identity, project identity, and staging namespace fields. */
internal object WriterIdentityConfigJsonSection : FigmaWriterProjectConfigJsonSection {
    override fun write(
        context: FigmaWriterProjectConfigJsonContext,
        json: JsonObjectBuilder,
    ) {
        val config = context.config
        json.put(
            "schemaVersion",
            4,
        )
        json.put(
            "METADATA_PAGE_ID",
            config.metadataPageId,
        )
        json.put(
            "METADATA_NAMESPACE",
            config.metadataNamespace,
        )
        json.put(
            "FIGMA_FILE_KEY",
            config.figmaFileKey,
        )
        json.put(
            "PROJECT_DISPLAY_NAME",
            config.projectDisplayName,
        )
        json.put(
            "MCP_CLIENT_NAME",
            config.mcpClientName,
        )
        json.put(
            "CANONICAL_STAGING_NAMESPACE",
            "${config.metadataNamespace}_staging",
        )
        json.put(
            "PREVIEW_STAGING_NAMESPACE",
            "${config.metadataNamespace}_preview",
        )
    }
}
