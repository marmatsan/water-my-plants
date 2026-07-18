package com.marmatsan.figmaDesignSync.data.json.writer

import com.marmatsan.figmaDesignSync.domain.model.writer.FigmaWriterRuntimeConfig
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Reads the small project-config projection required by Kotlin runner infrastructure. */
object FigmaWriterRuntimeConfigJson {
    fun read(path: String): FigmaWriterRuntimeConfig = decode(Files.readString(Path.of(path)))

    fun decode(source: String): FigmaWriterRuntimeConfig {
        val json = Json.parseToJsonElement(source.removePrefix(UTF8_BOM)).jsonObject
        require(json.requiredInt("schemaVersion") == SUPPORTED_SCHEMA_VERSION) {
            "Unsupported Figma writer project config schema ${json.requiredInt("schemaVersion")}; " +
                "expected $SUPPORTED_SCHEMA_VERSION."
        }
        return FigmaWriterRuntimeConfig(
            metadataPageId = json.requiredString("METADATA_PAGE_ID"),
            metadataNamespace = json.requiredString("METADATA_NAMESPACE"),
            figmaFileKey = json.requiredString("FIGMA_FILE_KEY"),
            projectDisplayName = json.requiredString("PROJECT_DISPLAY_NAME"),
            mcpClientName = json.requiredString("MCP_CLIENT_NAME"),
            repositoryRootRelativeToTools = json.requiredString("REPOSITORY_ROOT_RELATIVE_TO_TOOLS"),
            changeImpactPolicyRelativeToRepository =
                json.requiredString("CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY"),
            writerTargetNames = json.requiredStringList("WRITER_TARGET_NAMES"),
            catalogTargetNames = json.requiredStringList("CATALOG_TARGET_NAMES")
        )
    }

    private fun JsonObject.requiredString(name: String): String =
        this[name]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Figma writer project config is missing '$name'.")

    private fun JsonObject.requiredInt(name: String): Int =
        this[name]?.jsonPrimitive?.int
            ?: throw IllegalArgumentException("Figma writer project config is missing '$name'.")

    private fun JsonObject.requiredStringList(name: String): List<String> =
        this[name]?.jsonArray?.map { value -> value.jsonPrimitive.content }
            ?: throw IllegalArgumentException("Figma writer project config is missing '$name'.")

    private const val SUPPORTED_SCHEMA_VERSION = 1
    private const val UTF8_BOM = "\uFEFF"
}
