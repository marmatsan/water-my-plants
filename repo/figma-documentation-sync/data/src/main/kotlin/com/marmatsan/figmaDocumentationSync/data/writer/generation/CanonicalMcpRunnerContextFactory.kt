package com.marmatsan.figmaDocumentationSync.data.writer.generation

import com.marmatsan.figmaDocumentationSync.data.datasource.impact.FigmaChangeImpactPolicyDataSource
import com.marmatsan.figmaDocumentationSync.data.fingerprint.FigmaTargetFingerprintCalculator
import com.marmatsan.figmaDocumentationSync.data.fingerprint.WriterScopeFingerprintCalculator
import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.CanonicalJson
import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import com.marmatsan.figmaDocumentationSync.data.writer.CanonicalMcpRunnerGenerator
import com.marmatsan.figmaDocumentationSync.data.writer.McpRunnerSourceRenderer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/** Validates a public request and creates the immutable canonical generation context. */
internal class CanonicalMcpRunnerContextFactory(
    private val renderer: McpRunnerSourceRenderer,
    private val targetFingerprints: FigmaTargetFingerprintCalculator,
    private val writerFingerprints: WriterScopeFingerprintCalculator,
    private val policySource: FigmaChangeImpactPolicyDataSource,
) {
    /** Loads inputs, validates canonical identity, and calculates every generation fingerprint. */
    fun create(
        request: CanonicalMcpRunnerGenerator.Request,
    ): CanonicalMcpRunnerGenerationContext {
        require(request.transport in CanonicalMcpRunnerGenerationContract.supportedTransports) {
            "Unsupported MCP transport '${request.transport}'. Expected png or chunks."
        }
        require(request.chunkSize >= CanonicalMcpRunnerGenerationContract.MINIMUM_CHUNK_SIZE) {
            "MCP chunk size must be at least ${CanonicalMcpRunnerGenerationContract.MINIMUM_CHUNK_SIZE}."
        }

        val modelElement =
            Json.parseToJsonElement(
                Files
                    .readString(request.modelPath.toNormalizedPath())
                    .removePrefix(CanonicalMcpRunnerGenerationContract.UTF8_BOM),
            )
        val designModel = modelElement.jsonObject
        val modelJson =
            Json.encodeToString(
                JsonElement.serializer(),
                modelElement,
            )
        val script = Files.readString(request.scriptPath.toNormalizedPath())
        validateModel(
            designModel = designModel,
        )
        validateStagingEntry(
            key = "designModelJson",
            value = modelJson,
        )
        validateStagingEntry(
            key = "script",
            value = script,
        )

        val modelHash = designModel.requiredString("modelHash")
        val gitSha = designModel.requiredString("gitSha")
        val writerHash =
            Sha256Hash.of(
                value = script.toByteArray(StandardCharsets.UTF_8),
            )
        val allTargetFingerprints =
            targetFingerprints.create(
                designModel = designModel,
                visualTargets = request.config.visualTargetNames,
                catalogTargets = request.config.catalogTargetNames,
            )
        val allWriterFingerprints =
            writerFingerprints.create(
                sourceRoot = request.writerSourceDirectory.toNormalizedPath(),
                repositoryRoot = request.repositoryRootDirectory.toNormalizedPath(),
                policy = policySource.read(request.changeImpactPolicyPath),
                writerTargets = request.config.writerTargetNames,
                catalogTargets = request.config.catalogTargetNames,
                scopes = allTargetFingerprints.keys.toList(),
            )

        return CanonicalMcpRunnerGenerationContext(
            request = request,
            outputRoot = request.outputDirectory.toNormalizedPath(),
            designModel = designModel,
            modelJson = modelJson,
            script = script,
            modelHash = modelHash,
            gitSha = gitSha,
            writerHash = writerHash,
            transportHash =
                transportHash(
                    request = request,
                ),
            targetFingerprints = allTargetFingerprints,
            writerScopeFingerprints = allWriterFingerprints,
        )
    }

    private fun transportHash(
        request: CanonicalMcpRunnerGenerator.Request,
    ): String {
        val body =
            buildJsonObject {
                put(
                    "contractVersion",
                    CanonicalMcpRunnerGenerationContract.TRANSPORT_CONTRACT_VERSION,
                )
                put(
                    "transport",
                    request.transport,
                )
                put(
                    "chunkSize",
                    if (request.transport == CanonicalMcpRunnerGenerator.TRANSPORT_CHUNKS) {
                        JsonPrimitive(request.chunkSize)
                    } else {
                        JsonNull
                    },
                )
                put(
                    "payloadSchemaVersion",
                    if (request.transport == CanonicalMcpRunnerGenerator.TRANSPORT_PNG) {
                        JsonPrimitive(PayloadPngEncoder.PAYLOAD_SCHEMA_VERSION)
                    } else {
                        JsonNull
                    },
                )
                put(
                    "templates",
                    renderer.templateHashes().toJsonObject(),
                )
            }
        return Sha256Hash.of(
            value = CanonicalJson.stringify(body),
        )
    }

    private fun validateModel(
        designModel: JsonObject,
    ) {
        require(designModel.requiredString("branch") == "main") {
            "MCP runners require a main design model. Found '${designModel.requiredString("branch")}'."
        }
        designModel.requiredString("gitSha")
        designModel.requiredString("modelHash")
    }

    private fun validateStagingEntry(
        key: String,
        value: String,
    ) {
        require(value.length <= CanonicalMcpRunnerGenerationContract.MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH) {
            "$key is ${value.length} characters and exceeds the " +
                "${CanonicalMcpRunnerGenerationContract.MAX_SHARED_PLUGIN_DATA_ENTRY_LENGTH}-character " +
                "sharedPluginData staging limit."
        }
    }

    private fun String.toNormalizedPath(): Path =
        Path
            .of(this)
            .toAbsolutePath()
            .normalize()

    private fun JsonObject.requiredString(
        name: String,
    ): String =
        this[name]?.jsonPrimitive?.content
            ?: throw IllegalArgumentException("Design model is missing '$name'.")

    private fun Map<String, String>.toJsonObject(): JsonObject =
        JsonObject(mapValues { (_, value) -> JsonPrimitive(value) })
}
