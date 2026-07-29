package com.marmatsan.figmaDocumentationSync.data.writer.generation

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import com.marmatsan.figmaDocumentationSync.data.writer.CanonicalMcpRunnerGenerator
import com.marmatsan.figmaDocumentationSync.data.writer.McpRunnerSourceRenderer
import com.marmatsan.figmaDocumentationSync.domain.model.writer.CanonicalSyncPayload
import com.marmatsan.figmaDocumentationSync.domain.model.writer.RunnerPayloadImage
import kotlinx.serialization.json.JsonObject
import java.nio.file.Files
import java.nio.file.Path

/** Plans and materializes the selected canonical payload staging transport. */
internal class CanonicalMcpRunnerStagingPlanner(
    private val renderer: McpRunnerSourceRenderer,
    private val payloadEncoder: PayloadPngEncoder
) {
    /** Adds ordered staging sources and returns PNG identity when PNG transport is selected. */
    fun stage(
        context: CanonicalMcpRunnerGenerationContext,
        outputDirectory: Path,
        sources: MutableMap<String, String>
    ): RunnerPayloadImage? {
        val namespace = context.request.config.canonicalStagingNamespace
        sources[CanonicalMcpRunnerGenerationContract.CLEAR_STAGING_FILE] =
            renderer.clearStaging(
                metadataPageId = context.request.config.metadataPageId,
                namespace = namespace
            )

        val payloadImage =
            if (context.request.transport == CanonicalMcpRunnerGenerator.TRANSPORT_PNG) {
                stagePng(
                    context = context,
                    outputDirectory = outputDirectory,
                    sources = sources,
                    namespace = namespace
                )
            } else {
                addChunkSources(
                    sources = sources,
                    context = context,
                    namespace = namespace
                )
                null
            }

        sources[CanonicalMcpRunnerGenerationContract.FINALIZE_STAGING_FILE] =
            renderer.finalizeStaging(
                metadataPageId = context.request.config.metadataPageId,
                namespace = namespace,
                identity =
                    expectedIdentity(
                        context = context
                    )
            )
        return payloadImage
    }

    private fun stagePng(
        context: CanonicalMcpRunnerGenerationContext,
        outputDirectory: Path,
        sources: MutableMap<String, String>,
        namespace: String
    ): RunnerPayloadImage {
        val payload =
            CanonicalSyncPayload(
                payloadSchemaVersion = PayloadPngEncoder.PAYLOAD_SCHEMA_VERSION,
                designModelJson = context.modelJson,
                designModelHash = context.modelHash,
                designModelGitSha = context.gitSha,
                designModelLength = context.modelJson.length,
                script = context.script,
                scriptLength = context.script.length,
                writerHash = context.writerHash,
                transportHash = context.transportHash
            )
        val bytes = payloadEncoder.encode(payloadEncoder.payloadJson(payload))
        require(bytes.size <= PayloadPngEncoder.MAX_FIGMA_UPLOAD_ASSET_BYTES) {
            "Canonical payload PNG is ${bytes.size} bytes and exceeds " +
                "${PayloadPngEncoder.MAX_FIGMA_UPLOAD_ASSET_BYTES} bytes. Use chunk transport."
        }
        Files.write(
            outputDirectory.resolve(CanonicalMcpRunnerGenerationContract.PAYLOAD_PNG_FILE),
            bytes
        )
        val image =
            RunnerPayloadImage(
                fileName = CanonicalMcpRunnerGenerationContract.PAYLOAD_PNG_FILE,
                byteLength = bytes.size,
                sha256 = Sha256Hash.of(bytes),
                textKeyword = PayloadPngEncoder.TEXT_KEYWORD
            )
        sources[CanonicalMcpRunnerGenerationContract.STAGE_PAYLOAD_FILE] =
            renderer.stagePayloadFromPng(
                metadataPageId = context.request.config.metadataPageId,
                namespace = namespace,
                payloadFileName = CanonicalMcpRunnerGenerationContract.PAYLOAD_PNG_FILE,
                identity =
                    expectedIdentity(
                        context = context
                    )
            )
        return image
    }

    private fun addChunkSources(
        sources: MutableMap<String, String>,
        context: CanonicalMcpRunnerGenerationContext,
        namespace: String
    ) {
        listOf(
            "designModelJson" to context.modelJson,
            "script" to context.script
        ).forEach { (key, value) ->
            val chunks = value.chunked(context.request.chunkSize).ifEmpty { listOf("") }
            var previousLength = 0
            chunks.forEachIndexed { index, chunk ->
                val prefix = if (key == "designModelJson") "10" else "20"
                val fileName =
                    "$prefix-$key-${(index + 1).toString().padStart(
                        3,
                        '0'
                    )}.mcp.js"
                sources[fileName] =
                    renderer.appendChunk(
                        metadataPageId = context.request.config.metadataPageId,
                        namespace = namespace,
                        key = key,
                        chunk = chunk,
                        chunkIndex = index + 1,
                        chunkCount = chunks.size,
                        previousLength = previousLength
                    )
                previousLength += chunk.length
            }
        }
    }

    private fun expectedIdentity(
        context: CanonicalMcpRunnerGenerationContext
    ): JsonObject =
        renderer.expectedIdentity(
            modelHash = context.modelHash,
            gitSha = context.gitSha,
            modelLength = context.modelJson.length,
            scriptLength = context.script.length,
            writerHash = context.writerHash,
            transportHash = context.transportHash
        )
}
