package com.marmatsan.figmaDocumentationSync.data.json.writer

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterRuntimeConfig
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
    fun read(
        path: String
    ): FigmaWriterRuntimeConfig = decode(
        source = Files.readString(
            Path.of(
                path
            )
        )
    )

    fun decode(
        source: String
    ): FigmaWriterRuntimeConfig {
        val json = Json.parseToJsonElement(source.removePrefix(UTF8_BOM)).jsonObject
        require(json.requiredInt("schemaVersion") == SUPPORTED_SCHEMA_VERSION) {
            "Unsupported Figma writer project config schema ${json.requiredInt("schemaVersion")}; " +
                "expected $SUPPORTED_SCHEMA_VERSION."
        }
        val ciTargets = json.requiredStringList(
            name = "CI_VISUAL_TARGET_NAMES"
        )
        return FigmaWriterRuntimeConfig(
            metadataPageId = json.requiredString("METADATA_PAGE_ID"),
            metadataNamespace = json.requiredString("METADATA_NAMESPACE"),
            figmaFileKey = json.requiredString("FIGMA_FILE_KEY"),
            projectDisplayName = json.requiredString("PROJECT_DISPLAY_NAME"),
            mcpClientName = json.requiredString("MCP_CLIENT_NAME"),
            repositoryRootRelativeToTools = json.requiredString("REPOSITORY_ROOT_RELATIVE_TO_TOOLS"),
            changeImpactPolicyRelativeToRepository =
                json.requiredString("CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY"),
            writerTargetNames = json.requiredStringList(
                name = "WRITER_TARGET_NAMES"
            ),
            catalogTargetNames = json.requiredStringList(
                name = "CATALOG_TARGET_NAMES"
            ),
            ciVisualPlanConfig = if (ciTargets.isEmpty()) null else CiVisualPlanConfig(
                configurationModelName = json.requiredString("CI_CONFIGURATION_MODEL_NAME"),
                ciPipelineName = json.requiredString("CI_PIPELINE_NAME"),
                figmaPipelineName = json.requiredString("FIGMA_PIPELINE_NAME"),
                githubMainBlobUrl = json.requiredString("GITHUB_MAIN_BLOB_URL"),
                teamCitySource = json.requiredString("TEAMCITY_SOURCE"),
                topologySource = json.requiredString("TOPOLOGY_SOURCE"),
                windowsRuntimeSource = json.requiredString("WINDOWS_RUNTIME_SOURCE"),
                windowsRuntimeRunbookSource = json.requiredString("WINDOWS_RUNTIME_RUNBOOK_SOURCE"),
                visualContractSource = json.requiredString("VISUAL_CONTRACT_SOURCE"),
                branchProtectionSource = json.requiredString("BRANCH_PROTECTION_SOURCE"),
                officialSyncSource = json.requiredString("OFFICIAL_SYNC_SOURCE"),
                officialDesignModelPath = json.requiredString("OFFICIAL_DESIGN_MODEL_PATH")
            )
        )
    }

    private fun JsonObject.requiredString(
        name: String
    ): String =
        this[name]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Figma writer project config is missing '$name'.")

    private fun JsonObject.requiredInt(
        name: String
    ): Int =
        this[name]?.jsonPrimitive?.int
            ?: throw IllegalArgumentException("Figma writer project config is missing '$name'.")

    private fun JsonObject.requiredStringList(
        name: String
    ): List<String> =
        this[name]?.jsonArray?.map { value -> value.jsonPrimitive.content }
            ?: throw IllegalArgumentException("Figma writer project config is missing '$name'.")

    private const val SUPPORTED_SCHEMA_VERSION = 1
    private const val UTF8_BOM = "\uFEFF"
}
