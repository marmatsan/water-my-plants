package com.marmatsan.figmaDocumentationSync.data.mcp

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.McpExecutionStateJson
import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpToolResult
import com.marmatsan.figmaDocumentationSync.domain.port.writer.McpClientPort
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

internal class McpRunnerExecutorTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "mcp-runner-executor"
                )

            test("executes manifest files through the Kotlin MCP port and checkpoints each result") {
                val root = temporaryDirectory.toPath()
                val sources =
                    linkedMapOf(
                        "00-clear-staging.mcp.js" to "return { cleared: true };\n",
                        "99-00-preflight.mcp.js" to "return { preflight: true };\n"
                    )
                sources.forEach { (file, source) ->
                    Files.writeString(
                        root.resolve(
                            file
                        ),
                        source
                    )
                }
                val draft =
                    executableManifest(
                        root = root.toString(),
                        files = sources.keys.toList(),
                        hashes =
                            sources.mapValues { (_, source) ->
                                Sha256Hash.of(
                                    value = source.toByteArray(StandardCharsets.UTF_8)
                                )
                            }
                    )
                val manifest =
                    ExecutableRunnerManifestJson().finalizeAndWrite(
                        draft = draft,
                        outputPath =
                            root
                                .resolve(
                                    "manifest.json"
                                ).toString()
                    )
                val client = RecordingMcpClient()
                val executor =
                    McpRunnerExecutor(
                        clock =
                            Clock.fixed(
                                Instant.parse(
                                    "2026-07-18T18:00:00Z"
                                ),
                                ZoneOffset.UTC
                            ),
                        clientFactory = { _, _ -> client }
                    )

                val result =
                    executor.execute(
                        McpRunnerExecutor.Request(
                            manifestPath = manifest.path,
                            fileKey = "file-key",
                            clientName = "test-client",
                            projectDisplayName = "Test Project"
                        )
                    )

                client.executedCode shouldContainExactly sources.values.toList()
                result.executionFiles shouldContainExactly sources.keys.toList()
                result.state.completedFiles.map { entry -> entry.file } shouldContainExactly sources.keys.toList()
                McpExecutionStateJson()
                    .readOptional(
                        path =
                            root
                                .resolve(
                                    "execution-state.json"
                                ).toString()
                    )?.completedFiles
                    ?.size shouldBe 2
                client.closed shouldBe true
            }
        }
    ) {
    private class RecordingMcpClient : McpClientPort {
        val executedCode = mutableListOf<String>()
        var closed = false

        override suspend fun listToolNames(): List<String> = listOf("use_figma")

        override suspend fun readTextResource(
            uri: String
        ): String = "Use Figma safely."

        override suspend fun useFigma(
            fileKey: String,
            code: String,
            description: String,
            skillNames: String
        ): McpToolResult {
            executedCode += code
            return McpToolResult(
                isError = false,
                text = "ok"
            )
        }

        override suspend fun requestAssetUpload(
            fileKey: String,
            count: Int
        ): McpToolResult =
            error("Chunk transport must not upload assets")

        override suspend fun uploadAsset(
            url: String,
            bytes: ByteArray
        ) =
            error("Chunk transport must not upload assets")

        override fun close() {
            closed = true
        }
    }
}

private fun executableManifest(
    root: String,
    files: List<String>,
    hashes: Map<String, String>
) = ExecutableRunnerManifest(
    path = "$root/manifest.json",
    schemaVersion = 4,
    mode = "canonical",
    entrypoint = "trunk-sync",
    target = "preflight",
    targets = listOf("preflight"),
    writeMetadata = false,
    transport = "chunks",
    namespace = "test_staging",
    sectionNodeId = null,
    roots = emptyList(),
    allowCanonicalSections = false,
    fullVisualSync = false,
    allowPartial = true,
    metadataPageId = "1:2",
    modelPath = "design-model.json",
    scriptPath = "writer.mcp.js",
    modelHash = "sha256:model",
    gitSha = "abc123",
    designModelLength = 10,
    scriptLength = 20,
    writerHash = "sha256:writer",
    transportHash = "sha256:transport",
    targetFingerprints = mapOf("preflight" to "sha256:model-preflight"),
    writerScopeFingerprints =
        mapOf(
            "preflight" to "sha256:writer-preflight",
            "metadata" to "sha256:writer-metadata"
        ),
    writerScopeFingerprintSchemaVersion = 1,
    executionScopes = mapOf("99-00-preflight.mcp.js" to "preflight"),
    payloadImage = null,
    files = files,
    fileHashes = hashes,
    manifestHash = ""
)
