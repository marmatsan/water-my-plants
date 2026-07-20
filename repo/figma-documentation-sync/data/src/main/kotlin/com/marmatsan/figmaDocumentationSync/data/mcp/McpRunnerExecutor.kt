package com.marmatsan.figmaDocumentationSync.data.mcp

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.McpExecutionStateJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.VisualSyncPlanJson
import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpCapabilities
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionOptions
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionState
import com.marmatsan.figmaDocumentationSync.domain.port.writer.McpClientPort
import com.marmatsan.figmaDocumentationSync.domain.service.writer.McpExecutionPlanner
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import kotlinx.coroutines.runBlocking

/** Filesystem and MCP orchestration for one deterministic, checkpointed runner execution. */
class McpRunnerExecutor(
    private val manifestJson: ExecutableRunnerManifestJson = ExecutableRunnerManifestJson(),
    private val stateJson: McpExecutionStateJson = McpExecutionStateJson(),
    private val planJson: VisualSyncPlanJson = VisualSyncPlanJson(),
    private val planner: McpExecutionPlanner = McpExecutionPlanner(),
    private val clock: Clock = Clock.systemUTC(),
    private val clientFactory: (String, String) -> McpClientPort = KotlinSdkMcpClient::connect
) {
    fun probe(
        endpoint: String,
        clientName: String
    ): McpCapabilities {
        val client = clientFactory(
            endpoint,
            clientName
        )
        return client.use { connected -> runBlocking { planner.capabilities(connected.listToolNames()) } }
    }

    fun inspect(
        request: Request
    ): Inspection {
        val loaded = load(
            request = request
        )
        return Inspection(
            manifestHash = loaded.manifest.manifestHash,
            statePath = loaded.statePath.toString(),
            reuseStaging = request.options.reuseStaging,
            decision = loaded.plan?.body?.decision?.wireValue,
            executionScopes = loaded.plan?.body?.executionScopes,
            executionFiles = loaded.executionFiles
        )
    }

    fun record(
        request: Request,
        file: String,
        success: Boolean,
        summary: String
    ): McpExecutionState {
        val loaded = load(
            request = request
        )
        require(file in loaded.manifest.files) { "Recorded file '$file' is not in the runner manifest." }
        val now = clock.instant().toString()
        val initial = planner.createOrResumeState(
            manifest = loaded.manifest,
            existingState = loaded.existingState,
            options = request.options.copy(
                resume = loaded.existingState != null
            ),
            executionFiles = loaded.executionFiles.ifEmpty {
                loaded.existingState?.plannedFiles ?: loaded.manifest.files
            },
            now = now
        )
        val state = if (success) {
            planner.recordSuccess(
                initial,
                loaded.manifest,
                file,
                0,
                summary,
                now
            )
        } else {
            planner.recordFailure(
                initial,
                file,
                0,
                summary,
                now
            )
        }
        stateJson.writeAtomic(
            state,
            loaded.statePath.toString()
        )
        return state
    }

    fun execute(
        request: Request
    ): Result {
        val loaded = load(
            request = request
        )
        var state = planner.createOrResumeState(
            manifest = loaded.manifest,
            existingState = loaded.existingState,
            options = request.options,
            executionFiles = loaded.executionFiles,
            now = clock.instant().toString()
        )
        if (loaded.executionFiles.isEmpty()) {
            stateJson.writeAtomic(
                state,
                loaded.statePath.toString()
            )
            return Result(
                state = state,
                executionFiles = emptyList(),
                toolNames = emptyList()
            )
        }

        val client = clientFactory(
            request.endpoint,
            request.clientName
        )
        val toolNames = client.use { connected ->
            runBlocking {
                val capabilities = planner.capabilities(connected.listToolNames())
                planner.requireWriteCapabilities(
                    capabilities,
                    loaded.manifest,
                    loaded.executionFiles
                )
                val guidance = connected.readTextResource(McpExecutionPlanner.FIGMA_USE_SKILL_URI)
                require(guidance.isNotBlank()) {
                    "Write-capable Figma MCP endpoint did not provide required " +
                        McpExecutionPlanner.FIGMA_USE_SKILL_URI + "."
                }

                loaded.executionFiles.forEach { file ->
                    val startedAt = System.nanoTime()
                    try {
                        if (file == McpExecutionPlanner.PAYLOAD_STAGE_FILE) {
                            uploadPayload(
                                client = connected,
                                manifest = loaded.manifest,
                                runnerDirectory = loaded.runnerDirectory,
                                fileKey = request.fileKey
                            )
                        }
                        val source = Files.readString(loaded.runnerDirectory.resolve(file))
                        assertContentHash(
                            expectedHash = loaded.manifest.fileHashes.getValue(file),
                            content = source,
                            label = file
                        )
                        val toolResult = connected.useFigma(
                            fileKey = request.fileKey,
                            code = source,
                            description = "${request.projectDisplayName} Figma Sync: $file",
                            skillNames = "resource:figma-use"
                        )
                        require(!toolResult.isError) {
                            toolResult.text.ifBlank { "Figma MCP reported an error for $file." }
                        }
                        state = planner.recordSuccess(
                            state = state,
                            manifest = loaded.manifest,
                            file = file,
                            durationMs = elapsedMilliseconds(
                                startedAt = startedAt
                            ),
                            summary = toolResult.text,
                            now = clock.instant().toString()
                        )
                        stateJson.writeAtomic(
                            state,
                            loaded.statePath.toString()
                        )
                    } catch (
                        failure: Exception
                    ) {
                        state = planner.recordFailure(
                            state = state,
                            file = file,
                            durationMs = elapsedMilliseconds(
                                startedAt = startedAt
                            ),
                            message = failure.message ?: failure::class.simpleName.orEmpty(),
                            now = clock.instant().toString()
                        )
                        stateJson.writeAtomic(
                            state,
                            loaded.statePath.toString()
                        )
                        throw failure
                    }
                }
                capabilities.toolNames
            }
        }
        return Result(
            state = state,
            executionFiles = loaded.executionFiles,
            toolNames = toolNames
        )
    }

    private fun load(
        request: Request
    ): Loaded {
        val manifestPath = Path.of(request.manifestPath).toAbsolutePath().normalize()
        val runnerDirectory = requireNotNull(manifestPath.parent) { "Manifest has no parent directory." }
        val manifest = manifestJson.read(manifestPath.toString())
        val statePath = request.statePath?.let(Path::of)?.toAbsolutePath()?.normalize()
            ?: runnerDirectory.resolve(DEFAULT_STATE_FILE)
        val existingState = stateJson.readOptional(statePath.toString())
        val visualState = request.visualStatePath?.let(stateJson::readOptional)
        val plan = request.planPath?.let(planJson::read)
        val executionFiles = planner.selectExecutionFiles(
            manifest = manifest,
            options = request.options,
            existingState = existingState,
            visualState = visualState,
            syncPlan = plan
        )
        return Loaded(
            manifest = manifest,
            runnerDirectory = runnerDirectory,
            statePath = statePath,
            existingState = existingState,
            plan = plan,
            executionFiles = executionFiles
        )
    }

    private suspend fun uploadPayload(
        client: McpClientPort,
        manifest: ExecutableRunnerManifest,
        runnerDirectory: Path,
        fileKey: String
    ) {
        val payload = requireNotNull(manifest.payloadImage) {
            "PNG runner manifest does not declare payloadImage."
        }
        val response = client.requestAssetUpload(
            fileKey,
            1
        )
        require(!response.isError) { response.text.ifBlank { "upload_assets failed." } }
        val uploadUrl = URL_PATTERN.find(response.text)?.value
            ?: error("upload_assets did not return an upload URL.")
        val bytes = Files.readAllBytes(runnerDirectory.resolve(payload.fileName))
        val actualHash = Sha256Hash.of(bytes)
        require(actualHash == payload.sha256) {
            "MCP content hash mismatch for '${payload.fileName}': $actualHash != ${payload.sha256}."
        }
        client.uploadAsset(
            uploadUrl,
            bytes
        )
    }

    private fun assertContentHash(
        expectedHash: String,
        content: String,
        label: String
    ) {
        val actualHash = Sha256Hash.of(content.toByteArray(StandardCharsets.UTF_8))
        require(actualHash == expectedHash) {
            "MCP content hash mismatch for '$label': $actualHash != $expectedHash."
        }
    }

    private fun elapsedMilliseconds(
        startedAt: Long
    ): Long = (System.nanoTime() - startedAt) / 1_000_000

    data class Request(
        val manifestPath: String,
        val endpoint: String = "http://127.0.0.1:3845/mcp",
        val fileKey: String = "",
        val clientName: String = "figma-documentation-sync",
        val projectDisplayName: String = "Figma Documentation Sync",
        val statePath: String? = null,
        val visualStatePath: String? = null,
        val planPath: String? = null,
        val options: McpExecutionOptions = McpExecutionOptions()
    )

    data class Inspection(
        val manifestHash: String,
        val statePath: String,
        val reuseStaging: Boolean,
        val decision: String?,
        val executionScopes: List<String>?,
        val executionFiles: List<String>
    )

    data class Result(
        val state: McpExecutionState,
        val executionFiles: List<String>,
        val toolNames: List<String>
    )

    private data class Loaded(
        val manifest: ExecutableRunnerManifest,
        val runnerDirectory: Path,
        val statePath: Path,
        val existingState: McpExecutionState?,
        val plan: com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlan?,
        val executionFiles: List<String>
    )

    private companion object {
        const val DEFAULT_STATE_FILE = "execution-state.json"
        val URL_PATTERN = Regex("https://[^\\s\\\"']+")
    }
}
