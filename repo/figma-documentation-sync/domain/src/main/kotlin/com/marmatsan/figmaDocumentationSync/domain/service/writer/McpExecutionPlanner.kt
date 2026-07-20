package com.marmatsan.figmaDocumentationSync.domain.service.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpCapabilities
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpCompletedFile
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionFailure
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionIdentity
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionOptions
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionState
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlan

/** Selects atomic runner files and evolves compatible execution checkpoints. */
class McpExecutionPlanner {
    fun capabilities(
        toolNames: List<String>,
    ): McpCapabilities {
        val sorted = toolNames.distinct().sorted()
        return McpCapabilities(
            toolNames = sorted,
            canUseFigma = REQUIRED_WRITE_TOOL in sorted,
            canUploadAssets = REQUIRED_UPLOAD_TOOL in sorted,
        )
    }

    fun requireWriteCapabilities(
        capabilities: McpCapabilities,
        manifest: ExecutableRunnerManifest,
        executionFiles: List<String>,
    ) {
        val missing =
            buildList {
                if (!capabilities.canUseFigma) {
                    add(
                        element = REQUIRED_WRITE_TOOL,
                    )
                }
                if (needsPayloadUpload(
                        manifest = manifest,
                        executionFiles = executionFiles,
                    ) && !capabilities.canUploadAssets
                ) {
                    add(
                        element = REQUIRED_UPLOAD_TOOL,
                    )
                }
            }
        require(missing.isEmpty()) {
            "MCP endpoint is read-only for this runner; missing tool(s): ${missing.joinToString(", ")}. " +
                "Keep the Codex-operated Figma write path until the local endpoint advertises them."
        }
    }

    fun selectExecutionFiles(
        manifest: ExecutableRunnerManifest,
        options: McpExecutionOptions,
        existingState: McpExecutionState?,
        visualState: McpExecutionState?,
        syncPlan: VisualSyncPlan? = null,
    ): List<String> {
        var files = manifest.files.filter { file -> file.endsWith(".mcp.js") }

        if (syncPlan != null && !manifest.writeMetadata) {
            require(syncPlan.body.manifestHash == manifest.manifestHash) {
                "Visual sync plan manifestHash does not match the runner manifest."
            }
            files =
                when (syncPlan.body.decision.wireValue) {
                    "none" -> {
                        emptyList()
                    }

                    "partial" -> {
                        val scopes = syncPlan.body.executionScopes.toSet()
                        files.filter { file ->
                            !file.startsWith(
                                prefix = "99-",
                            ) || manifest.executionScopes[file] in scopes
                        }
                    }

                    "full" -> {
                        files
                    }

                    else -> {
                        error("Unknown visual sync plan decision '${syncPlan.body.decision.wireValue}'.")
                    }
                }
        }

        if (options.reuseStaging) {
            require(manifest.writeMetadata && manifest.targets == listOf("metadata")) {
                "--reuse-staging is only valid for a metadata-only manifest."
            }
            assertCompletedVisualState(
                metadataManifest = manifest,
                visualState = visualState,
            )
            files =
                files.filter { file ->
                    file.startsWith(
                        prefix = "99-",
                    )
                }
        }

        options.from?.let { firstFile ->
            val index = files.indexOf(firstFile)
            require(index >= 0) { "--from file '$firstFile' is not in the execution plan." }
            files = files.drop(index)
        }

        if (options.retryFailed) {
            val failedFile =
                requireNotNull(existingState?.failedFile) {
                    "--retry-failed requires a checkpoint with failedFile."
                }
            require(failedFile in files) { "Checkpoint failed file '$failedFile' is not in this manifest." }
            return listOf(failedFile)
        }

        if (options.resume && existingState != null) {
            assertStateIdentity(
                manifest = manifest,
                state = existingState,
            )
            val completed =
                existingState.completedFiles
                    .map(
                        transform = McpCompletedFile::file,
                    ).toSet()
            files = files.filterNot(completed::contains)
        }
        return files
    }

    fun createOrResumeState(
        manifest: ExecutableRunnerManifest,
        existingState: McpExecutionState?,
        options: McpExecutionOptions,
        executionFiles: List<String>,
        now: String,
    ): McpExecutionState {
        if ((options.resume || options.retryFailed) && existingState != null) {
            assertStateIdentity(
                manifest = manifest,
                state = existingState,
            )
            return existingState.copy(
                failedFile = null,
                failure = null,
                updatedAt = now,
            )
        }
        return McpExecutionState(
            schemaVersion = STATE_SCHEMA_VERSION,
            identity =
                executionIdentity(
                    manifest = manifest,
                ),
            startedAt = now,
            updatedAt = now,
            completedFiles = emptyList(),
            plannedFiles = executionFiles,
            failedFile = null,
        )
    }

    fun recordSuccess(
        state: McpExecutionState,
        manifest: ExecutableRunnerManifest,
        file: String,
        durationMs: Long,
        summary: String?,
        now: String,
    ): McpExecutionState {
        val completed =
            state.completedFiles.filterNot { entry -> entry.file == file } +
                McpCompletedFile(
                    file = file,
                    fileHash = manifest.fileHashes.getValue(file),
                    durationMs = durationMs,
                    completedAt = now,
                    summary = summary?.take(MAX_SUMMARY_LENGTH),
                )
        return state.copy(
            completedFiles = completed,
            failedFile = null,
            failure = null,
            updatedAt = now,
        )
    }

    fun recordFailure(
        state: McpExecutionState,
        file: String,
        durationMs: Long,
        message: String,
        now: String,
    ): McpExecutionState =
        state.copy(
            failedFile = file,
            failure =
                McpExecutionFailure(
                    message = message,
                    durationMs = durationMs,
                    failedAt = now,
                ),
            updatedAt = now,
        )

    fun executionIdentity(
        manifest: ExecutableRunnerManifest,
    ): McpExecutionIdentity =
        McpExecutionIdentity(
            modelHash = manifest.modelHash,
            gitSha = manifest.gitSha,
            writerHash = manifest.writerHash,
            transportHash = manifest.transportHash,
            manifestHash = manifest.manifestHash,
        )

    fun assertStateIdentity(
        manifest: ExecutableRunnerManifest,
        state: McpExecutionState,
    ) {
        val expected =
            executionIdentity(
                manifest = manifest,
            )
        val values =
            listOf(
                "modelHash" to (state.identity.modelHash to expected.modelHash),
                "gitSha" to (state.identity.gitSha to expected.gitSha),
                "writerHash" to (state.identity.writerHash to expected.writerHash),
                "transportHash" to (state.identity.transportHash to expected.transportHash),
                "manifestHash" to (state.identity.manifestHash to expected.manifestHash),
            )
        values.forEach { (key, valuesForKey) ->
            require(valuesForKey.first == valuesForKey.second) {
                "Checkpoint $key mismatch: ${valuesForKey.first} != ${valuesForKey.second}."
            }
        }
        state.plannedFiles.forEach { file ->
            require(file in manifest.files) { "Checkpoint planned file '$file' is not in the runner manifest." }
        }
        state.completedFiles.forEach { completed ->
            val expectedHash = manifest.fileHashes[completed.file]
            require(expectedHash == completed.fileHash) {
                "Checkpoint file hash mismatch for '${completed.file}': " +
                    "${completed.fileHash} != ${expectedHash ?: "<missing>"}."
            }
        }
        state.failedFile?.let { file ->
            require(file in manifest.files) { "Checkpoint failed file '$file' is not in the runner manifest." }
        }
    }

    fun assertCompletedVisualState(
        metadataManifest: ExecutableRunnerManifest,
        visualState: McpExecutionState?,
    ) {
        val state =
            requireNotNull(visualState) {
                "--reuse-staging requires --visual-state from the completed visual runner."
            }
        val expected =
            executionIdentity(
                manifest = metadataManifest,
            )
        val compatible =
            listOf(
                "modelHash" to (state.identity.modelHash to expected.modelHash),
                "gitSha" to (state.identity.gitSha to expected.gitSha),
                "writerHash" to (state.identity.writerHash to expected.writerHash),
                "transportHash" to (state.identity.transportHash to expected.transportHash),
            )
        compatible.forEach { (key, valuesForKey) ->
            require(valuesForKey.first == valuesForKey.second) {
                "Visual checkpoint $key does not match the metadata manifest."
            }
        }
        val completedFiles =
            state.completedFiles
                .map(
                    transform = McpCompletedFile::file,
                ).filter {
                    it.startsWith(
                        prefix = "99-",
                    )
                }.toSet()
        val plannedFiles =
            state.plannedFiles.filter {
                it.startsWith(
                    prefix = "99-",
                )
            }
        require(
            plannedFiles.isNotEmpty() &&
                plannedFiles.all(completedFiles::contains) &&
                state.failedFile == null,
        ) {
            "Visual checkpoint is not complete enough to authorize staging reuse."
        }
    }

    private fun needsPayloadUpload(
        manifest: ExecutableRunnerManifest,
        executionFiles: List<String>,
    ): Boolean = manifest.transport == "png" && PAYLOAD_STAGE_FILE in executionFiles

    companion object {
        const val REQUIRED_WRITE_TOOL = "use_figma"
        const val REQUIRED_UPLOAD_TOOL = "upload_assets"
        const val FIGMA_USE_SKILL_URI = "skill://figma/figma-use/SKILL.md"
        const val PAYLOAD_STAGE_FILE = "10-stage-payload-from-png.mcp.js"

        private const val STATE_SCHEMA_VERSION = 1
        private const val MAX_SUMMARY_LENGTH = 500
    }
}
