package com.marmatsan.figmaDocumentationSync.data.json.writer.config

import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

/** Writes version-variable, color-variable, and dependency-version component fields. */
internal object WriterVersionsConfigJsonSection : FigmaWriterProjectConfigJsonSection {
    override fun write(
        context: FigmaWriterProjectConfigJsonContext,
        json: JsonObjectBuilder,
    ) {
        val config = context.config
        json.put(
            "VERSIONS_COLLECTION_NAME",
            config.versionsCollectionName,
        )
        json.put(
            "VERSIONS_COLLECTION_NAMES",
            listOf(config.versionsCollectionName).toJsonArray(),
        )
        json.put(
            "VERSION_ALIAS_MODE_NAME",
            config.versionAliasModeName,
        )
        json.put(
            "VERSION_NUMBER_MODE_NAME",
            config.versionNumberModeName,
        )
        json.put(
            "OUTLINE_COLOR_VARIABLE_NAME",
            config.outlineColorVariableName,
        )
        json.put(
            "SURFACE_COLOR_VARIABLE_NAME",
            config.surfaceColorVariableName,
        )
        json.put(
            "DEPENDENCY_VERSION_COMPONENT_ID",
            config.dependencyVersionComponentId,
        )
        json.put(
            "PROJECT_VERSION_COMPONENT_ID",
            config.dependencyVersionComponentId,
        )
        json.put(
            "DEPENDENCY_VERSION_INSTANCE_NAMES",
            config.dependencyVersionInstanceNames.toJsonArray(),
        )
        json.put(
            "DEPENDENCY_VERSION_PROPS",
            config.dependencyVersionProps.toJsonObject(),
        )
    }
}
