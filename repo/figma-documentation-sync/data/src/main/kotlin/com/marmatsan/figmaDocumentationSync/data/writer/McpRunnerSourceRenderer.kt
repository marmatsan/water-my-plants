package com.marmatsan.figmaDocumentationSync.data.writer

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import java.nio.charset.StandardCharsets
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Renders the JavaScript boundary evaluated by Figma from packaged templates. */
class McpRunnerSourceRenderer {
    fun clearStaging(metadataPageId: String, namespace: String): String = render(
        CLEAR_STAGING_TEMPLATE,
        mapOf(
            "METADATA_PAGE_ID" to quote(metadataPageId),
            "NAMESPACE" to quote(namespace)
        )
    )

    fun appendChunk(
        metadataPageId: String,
        namespace: String,
        key: String,
        chunk: String,
        chunkIndex: Int,
        chunkCount: Int,
        previousLength: Int
    ): String = render(
        APPEND_CHUNK_TEMPLATE,
        mapOf(
            "METADATA_PAGE_ID" to quote(metadataPageId),
            "NAMESPACE" to quote(namespace),
            "KEY" to quote(key),
            "CHUNK" to quote(chunk),
            "CHUNK_LENGTH" to chunk.length.toString(),
            "PREVIOUS_LENGTH" to previousLength.toString(),
            "CHUNK_INDEX" to chunkIndex.toString(),
            "CHUNK_COUNT" to chunkCount.toString()
        )
    )

    fun stagePayloadFromPng(
        metadataPageId: String,
        namespace: String,
        payloadFileName: String,
        identity: JsonObject
    ): String = render(
        STAGE_PAYLOAD_TEMPLATE,
        mapOf(
            "METADATA_PAGE_ID" to quote(metadataPageId),
            "NAMESPACE" to quote(namespace),
            "PAYLOAD_KEYWORD" to quote(PayloadPngEncoder.TEXT_KEYWORD),
            "PAYLOAD_FILE_NAME" to quote(payloadFileName),
            "EXPECTED_IDENTITY" to CanonicalJson.stringify(identity)
        )
    )

    fun finalizeStaging(
        metadataPageId: String,
        namespace: String,
        identity: JsonObject
    ): String = render(
        FINALIZE_STAGING_TEMPLATE,
        mapOf(
            "METADATA_PAGE_ID" to quote(metadataPageId),
            "NAMESPACE" to quote(namespace),
            "EXPECTED_IDENTITY" to CanonicalJson.stringify(identity)
        )
    )

    fun runTarget(
        metadataPageId: String,
        namespace: String,
        syncOptions: JsonObject,
        executionScope: String,
        modelTarget: String
    ): String = render(
        RUN_TARGET_TEMPLATE,
        mapOf(
            "METADATA_PAGE_ID" to quote(metadataPageId),
            "NAMESPACE" to quote(namespace),
            "SYNC_OPTIONS" to CanonicalJson.stringify(syncOptions),
            "EXECUTION_SCOPE" to quote(executionScope),
            "MODEL_TARGET" to quote(modelTarget)
        )
    )

    fun expectedIdentity(
        modelHash: String,
        gitSha: String,
        modelLength: Int,
        scriptLength: Int,
        writerHash: String,
        transportHash: String
    ): JsonObject = buildJsonObject {
        put("payloadSchemaVersion", PayloadPngEncoder.PAYLOAD_SCHEMA_VERSION)
        put("designModelHash", modelHash)
        put("designModelGitSha", gitSha)
        put("designModelLength", modelLength.toString())
        put("scriptLength", scriptLength.toString())
        put("writerHash", writerHash)
        put("transportHash", transportHash)
    }

    fun templateHashes(): Map<String, String> = TEMPLATE_NAMES.associateWith { templateName ->
        Sha256Hash.of(template(templateName).toByteArray(StandardCharsets.UTF_8))
    }

    private fun render(templateName: String, replacements: Map<String, String>): String {
        val rendered = replacements.entries.fold(template(templateName)) { source, (name, value) ->
            source.replace("@@$name@@", value)
        }
        val unresolved = PLACEHOLDER.find(rendered)?.value
        require(unresolved == null) { "Runner template '$templateName' contains unresolved placeholder $unresolved." }
        return rendered.trimEnd() + "\n"
    }

    private fun template(name: String): String = javaClass.getResourceAsStream("/figma-mcp/$name")
        ?.bufferedReader()
        ?.use { reader -> reader.readText() }
        ?: error("Missing packaged MCP runner template '$name'.")

    private fun quote(value: String): String = JsonPrimitive(value).toString()

    private companion object {
        const val CLEAR_STAGING_TEMPLATE = "clear-staging.mcp.js.template"
        const val APPEND_CHUNK_TEMPLATE = "append-chunk.mcp.js.template"
        const val STAGE_PAYLOAD_TEMPLATE = "stage-payload-from-png.mcp.js.template"
        const val FINALIZE_STAGING_TEMPLATE = "finalize-staging.mcp.js.template"
        const val RUN_TARGET_TEMPLATE = "run-target.mcp.js.template"

        val TEMPLATE_NAMES = listOf(
            CLEAR_STAGING_TEMPLATE,
            APPEND_CHUNK_TEMPLATE,
            STAGE_PAYLOAD_TEMPLATE,
            FINALIZE_STAGING_TEMPLATE,
            RUN_TARGET_TEMPLATE
        )
        val PLACEHOLDER = Regex("@@[A-Z_]+@@")
    }
}
