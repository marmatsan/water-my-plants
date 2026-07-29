package com.marmatsan.figmaDocumentationSync.domain.service.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionOptions
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncDecision
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncIdentity
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlan
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlanBody
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

internal class McpExecutionPlannerTest :
    FunSpec(
        {
            val planner = McpExecutionPlanner()

            test("capability probe distinguishes read-only and write-capable endpoints") {
                planner
                    .capabilities(
                        toolNames =
                            listOf(
                                "get_metadata",
                                "get_screenshot"
                            )
                    ).writeCapable shouldBe false
                val writable =
                    planner.capabilities(
                        toolNames =
                            listOf(
                                "upload_assets",
                                "use_figma",
                                "get_metadata"
                            )
                    )
                writable.writeCapable shouldBe true
                planner.requireWriteCapabilities(
                    capabilities = writable,
                    manifest = manifest,
                    executionFiles = manifest.files
                )

                shouldThrow<IllegalArgumentException> {
                    planner.requireWriteCapabilities(
                        capabilities =
                            planner.capabilities(
                                toolNames = listOf("get_metadata")
                            ),
                        manifest = manifest,
                        executionFiles = manifest.files
                    )
                }.message shouldBe
                    "MCP endpoint is read-only for this runner; missing tool(s): use_figma, upload_assets. " +
                    "Keep the Codex-operated Figma write path until the local endpoint advertises them."
            }

            test("resume skips completed files with the same execution identity") {
                var state =
                    planner.createOrResumeState(
                        manifest = manifest,
                        existingState = null,
                        options = McpExecutionOptions(),
                        executionFiles = manifest.files,
                        now = "2026-07-18T18:00:00Z"
                    )
                state =
                    planner.recordSuccess(
                        state = state,
                        manifest = manifest,
                        file = "00-clear-staging.mcp.js",
                        durationMs = 10,
                        summary = "ok",
                        now = "2026-07-18T18:00:01Z"
                    )

                planner.selectExecutionFiles(
                    manifest = manifest,
                    options =
                        McpExecutionOptions(
                            resume = true
                        ),
                    existingState = state,
                    visualState = null
                ) shouldContainExactly manifest.files.drop(1)
            }

            test("retry failed selects only the atomic failed unit") {
                val initial =
                    planner.createOrResumeState(
                        manifest = manifest,
                        existingState = null,
                        options = McpExecutionOptions(),
                        executionFiles = manifest.files,
                        now = "2026-07-18T18:00:00Z"
                    )
                val failed =
                    planner.recordFailure(
                        state = initial,
                        file = "99-00-preflight.mcp.js",
                        durationMs = 25,
                        message = "timeout",
                        now = "2026-07-18T18:00:01Z"
                    )

                planner.selectExecutionFiles(
                    manifest = manifest,
                    options =
                        McpExecutionOptions(
                            resume = true,
                            retryFailed = true
                        ),
                    existingState = failed,
                    visualState = null
                ) shouldContainExactly listOf("99-00-preflight.mcp.js")
            }

            test("partial visual plan keeps staging and selected execution scopes") {
                val plan =
                    VisualSyncPlan(
                        body =
                            VisualSyncPlanBody(
                                schemaVersion = 1,
                                decision = VisualSyncDecision.PARTIAL,
                                reason = "target-model-fingerprints-changed",
                                requiresVisualWrite = true,
                                requiresMetadataWrite = true,
                                executionScopes = listOf("preflight"),
                                identity =
                                    VisualSyncIdentity(
                                        modelHash = manifest.modelHash,
                                        writerHash = manifest.writerHash,
                                        transportHash = manifest.transportHash,
                                        writerScopeFingerprintSchemaVersion = 1
                                    ),
                                manifestHash = manifest.manifestHash
                            ),
                        planHash = "sha256:plan"
                    )

                planner.selectExecutionFiles(
                    manifest = manifest,
                    options = McpExecutionOptions(),
                    existingState = null,
                    visualState = null,
                    syncPlan = plan
                ) shouldContainExactly manifest.files.dropLast(1)
            }

            test("checkpoint identity invalidates resume when writer changes") {
                val state =
                    planner.createOrResumeState(
                        manifest = manifest,
                        existingState = null,
                        options = McpExecutionOptions(),
                        executionFiles = manifest.files,
                        now = "2026-07-18T18:00:00Z"
                    )
                shouldThrow<IllegalArgumentException> {
                    planner.assertStateIdentity(
                        manifest =
                            manifest.copy(
                                writerHash = "sha256:new-writer"
                            ),
                        state = state
                    )
                }.message shouldBe "Checkpoint writerHash mismatch: sha256:writer != sha256:new-writer."
            }

            test("checkpoint rejects a completed file whose hash was altered") {
                val initial =
                    planner.createOrResumeState(
                        manifest = manifest,
                        existingState = null,
                        options = McpExecutionOptions(),
                        executionFiles = manifest.files,
                        now = "2026-07-18T18:00:00Z"
                    )
                val completed =
                    planner.recordSuccess(
                        state = initial,
                        manifest = manifest,
                        file = "00-clear-staging.mcp.js",
                        durationMs = 10,
                        summary = "ok",
                        now = "2026-07-18T18:00:01Z"
                    )
                val altered =
                    completed.copy(
                        completedFiles =
                            listOf(
                                completed.completedFiles.single().copy(
                                    fileHash = "sha256:altered"
                                )
                            )
                    )

                shouldThrow<IllegalArgumentException> {
                    planner.assertStateIdentity(
                        manifest = manifest,
                        state = altered
                    )
                }.message shouldBe
                    "Checkpoint file hash mismatch for '00-clear-staging.mcp.js': " +
                    "sha256:altered != sha256:00."
            }
        }
    )

private val manifest =
    ExecutableRunnerManifest(
        path = "manifest.json",
        schemaVersion = 4,
        mode = "canonical",
        entrypoint = "trunk-sync",
        target = "preflight",
        targets =
            listOf(
                "preflight",
                "versions"
            ),
        writeMetadata = false,
        transport = "png",
        namespace = "sync_staging",
        sectionNodeId = null,
        roots = emptyList(),
        allowCanonicalSections = false,
        fullVisualSync = true,
        allowPartial = false,
        metadataPageId = "1:2",
        modelPath = "design-model.json",
        scriptPath = "writer.mcp.js",
        modelHash = "sha256:model",
        gitSha = "abc123",
        designModelLength = 100,
        scriptLength = 200,
        writerHash = "sha256:writer",
        transportHash = "sha256:transport",
        targetFingerprints =
            mapOf(
                "preflight" to "sha256:model-preflight",
                "versions" to "sha256:model-versions"
            ),
        writerScopeFingerprints =
            mapOf(
                "preflight" to "sha256:writer-preflight",
                "versions" to "sha256:writer-versions",
                "metadata" to "sha256:writer-metadata"
            ),
        writerScopeFingerprintSchemaVersion = 1,
        executionScopes =
            mapOf(
                "99-00-preflight.mcp.js" to "preflight",
                "99-01-versions.mcp.js" to "versions"
            ),
        payloadImage =
            com.marmatsan.figmaDocumentationSync.domain.model.writer.RunnerPayloadImage(
                fileName = "payload.png",
                byteLength = 1,
                sha256 = "sha256:payload",
                textKeyword = "figmaSyncPayload"
            ),
        files =
            listOf(
                "00-clear-staging.mcp.js",
                "10-stage-payload-from-png.mcp.js",
                "90-finalize-staging.mcp.js",
                "99-00-preflight.mcp.js",
                "99-01-versions.mcp.js"
            ),
        fileHashes =
            mapOf(
                "00-clear-staging.mcp.js" to "sha256:00",
                "10-stage-payload-from-png.mcp.js" to "sha256:10",
                "90-finalize-staging.mcp.js" to "sha256:90",
                "99-00-preflight.mcp.js" to "sha256:99-00",
                "99-01-versions.mcp.js" to "sha256:99-01"
            ),
        manifestHash = "sha256:manifest"
    )
