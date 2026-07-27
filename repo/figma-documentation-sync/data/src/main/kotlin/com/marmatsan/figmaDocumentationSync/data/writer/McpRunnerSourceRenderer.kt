package com.marmatsan.figmaDocumentationSync.data.writer

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.nio.charset.StandardCharsets

/** Renders the JavaScript boundary evaluated by Figma from packaged templates. */
class McpRunnerSourceRenderer {
    /** Renders the operation that removes stale canonical staging data. */
    fun clearStaging(
        metadataPageId: String,
        namespace: String,
    ): String =
        render(
            templateName = CLEAR_STAGING_TEMPLATE,
            replacements =
                mapOf(
                    "METADATA_PAGE_ID" to
                        quote(
                            value = metadataPageId,
                        ),
                    "NAMESPACE" to
                        quote(
                            value = namespace,
                        ),
                ),
        )

    /** Renders one validated append operation for a chunked staging value. */
    fun appendChunk(
        metadataPageId: String,
        namespace: String,
        key: String,
        chunk: String,
        chunkIndex: Int,
        chunkCount: Int,
        previousLength: Int,
    ): String =
        render(
            templateName = APPEND_CHUNK_TEMPLATE,
            replacements =
                mapOf(
                    "METADATA_PAGE_ID" to
                        quote(
                            value = metadataPageId,
                        ),
                    "NAMESPACE" to
                        quote(
                            value = namespace,
                        ),
                    "KEY" to
                        quote(
                            value = key,
                        ),
                    "CHUNK" to
                        quote(
                            value = chunk,
                        ),
                    "CHUNK_LENGTH" to chunk.length.toString(),
                    "PREVIOUS_LENGTH" to previousLength.toString(),
                    "CHUNK_INDEX" to chunkIndex.toString(),
                    "CHUNK_COUNT" to chunkCount.toString(),
                ),
        )

    /** Renders the operation that decodes and stages a previously uploaded PNG payload. */
    fun stagePayloadFromPng(
        metadataPageId: String,
        namespace: String,
        payloadFileName: String,
        identity: JsonObject,
    ): String =
        render(
            templateName = STAGE_PAYLOAD_TEMPLATE,
            replacements =
                mapOf(
                    "METADATA_PAGE_ID" to
                        quote(
                            value = metadataPageId,
                        ),
                    "NAMESPACE" to
                        quote(
                            value = namespace,
                        ),
                    "PAYLOAD_KEYWORD" to
                        quote(
                            value = PayloadPngEncoder.TEXT_KEYWORD,
                        ),
                    "PAYLOAD_FILE_NAME" to
                        quote(
                            value = payloadFileName,
                        ),
                    "EXPECTED_IDENTITY" to
                        CanonicalJson.stringify(
                            value = identity,
                        ),
                ),
        )

    /** Renders the operation that validates and commits the staged canonical identity. */
    fun finalizeStaging(
        metadataPageId: String,
        namespace: String,
        identity: JsonObject,
    ): String =
        render(
            templateName = FINALIZE_STAGING_TEMPLATE,
            replacements =
                mapOf(
                    "METADATA_PAGE_ID" to
                        quote(
                            value = metadataPageId,
                        ),
                    "NAMESPACE" to
                        quote(
                            value = namespace,
                        ),
                    "EXPECTED_IDENTITY" to
                        CanonicalJson.stringify(
                            value = identity,
                        ),
                ),
        )

    /** Renders the operation that invokes one model [modelTarget] for [executionScope]. */
    fun runTarget(
        metadataPageId: String,
        namespace: String,
        syncOptions: JsonObject,
        executionScope: String,
        modelTarget: String,
    ): String =
        render(
            templateName = RUN_TARGET_TEMPLATE,
            replacements =
                mapOf(
                    "METADATA_PAGE_ID" to
                        quote(
                            value = metadataPageId,
                        ),
                    "NAMESPACE" to
                        quote(
                            value = namespace,
                        ),
                    "SYNC_OPTIONS" to
                        CanonicalJson.stringify(
                            value = syncOptions,
                        ),
                    "EXECUTION_SCOPE" to
                        quote(
                            value = executionScope,
                        ),
                    "MODEL_TARGET" to
                        quote(
                            value = modelTarget,
                        ),
                ),
        )

    /** Builds the canonical identity that generated staging code must verify. */
    fun expectedIdentity(
        modelHash: String,
        gitSha: String,
        modelLength: Int,
        scriptLength: Int,
        writerHash: String,
        transportHash: String,
    ): JsonObject =
        buildJsonObject {
            put(
                "payloadSchemaVersion",
                PayloadPngEncoder.PAYLOAD_SCHEMA_VERSION,
            )
            put(
                "designModelHash",
                modelHash,
            )
            put(
                "designModelGitSha",
                gitSha,
            )
            put(
                "designModelLength",
                modelLength.toString(),
            )
            put(
                "scriptLength",
                scriptLength.toString(),
            )
            put(
                "writerHash",
                writerHash,
            )
            put(
                "transportHash",
                transportHash,
            )
        }

    /** Returns deterministic hashes for every packaged JavaScript template. */
    fun templateHashes(): Map<String, String> =
        TEMPLATE_NAMES.associateWith { templateName ->
            Sha256Hash.of(
                value =
                    template(
                        name = templateName,
                    ).toByteArray(StandardCharsets.UTF_8),
            )
        }

    private fun render(
        templateName: String,
        replacements: Map<String, String>,
    ): String {
        val rendered =
            replacements.entries.fold(
                template(
                    name = templateName,
                ),
            ) { source, (name, value) ->
                source.replace(
                    "@@$name@@",
                    value,
                )
            }
        val unresolved = PLACEHOLDER.find(rendered)?.value
        require(unresolved == null) { "Runner template '$templateName' contains unresolved placeholder $unresolved." }
        return rendered.trimEnd() + "\n"
    }

    private fun template(
        name: String,
    ): String =
        javaClass
            .getResourceAsStream("/figma-mcp/$name")
            ?.bufferedReader()
            ?.use { reader -> reader.readText() }
            ?: error("Missing packaged MCP runner template '$name'.")

    private fun quote(
        value: String,
    ): String = JsonPrimitive(value).toString()

    private companion object {
        const val CLEAR_STAGING_TEMPLATE = "clear-staging.mcp.js.template"
        const val APPEND_CHUNK_TEMPLATE = "append-chunk.mcp.js.template"
        const val STAGE_PAYLOAD_TEMPLATE = "stage-payload-from-png.mcp.js.template"
        const val FINALIZE_STAGING_TEMPLATE = "finalize-staging.mcp.js.template"
        const val RUN_TARGET_TEMPLATE = "run-target.mcp.js.template"

        val TEMPLATE_NAMES =
            listOf(
                CLEAR_STAGING_TEMPLATE,
                APPEND_CHUNK_TEMPLATE,
                STAGE_PAYLOAD_TEMPLATE,
                FINALIZE_STAGING_TEMPLATE,
                RUN_TARGET_TEMPLATE,
            )
        val PLACEHOLDER = Regex("@@[A-Z_]+@@")
    }
}
