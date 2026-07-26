package com.marmatsan.waterMyPlants.projectConfig

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.json.writer.ExecutableRunnerManifestJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.VisualSyncPlanJson
import com.marmatsan.figmaDocumentationSync.domain.model.writer.ExecutableRunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.RunnerPayloadImage
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncDecision
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncIdentity
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlan
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncPlanBody
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityBuild
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityBuildArtifactClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.nio.file.Files
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

internal class TeamCityFigmaSyncHandoffPreparerTest :
    FunSpec(
        {
            test("prepares a validated handoff from an existing artifact directory") {
                val root = Files.createTempDirectory("figma-handoff").toFile()
                val artifacts =
                    root
                        .resolve(
                            relative = "artifacts",
                        ).apply {
                            mkdirs()
                            writeArtifactFixture()
                        }
                val client =
                    object : TeamCityBuildArtifactClient {
                        override fun readBuild(
                            buildId: Long,
                        ): TeamCityBuild = error("TeamCity must not be called")

                        override fun downloadArtifacts(
                            buildId: Long,
                            outputDirectory: File,
                        ) =
                            error("TeamCity must not be called")
                    }
                val preparer =
                    TeamCityFigmaSyncHandoffPreparer(
                        teamCityClient = client,
                        clock =
                            Clock.fixed(
                                Instant.parse(
                                    "2026-07-18T18:00:00Z",
                                ),
                                ZoneOffset.UTC,
                            ),
                    )

                val result =
                    preparer.prepare(
                        request =
                            TeamCityFigmaSyncHandoffPreparer.Request(
                                buildId = null,
                                artifactDirectory = artifacts,
                                destinationRoot =
                                    root.resolve(
                                        relative = "downloads",
                                    ),
                                expectedGitSha = "abc123",
                            ),
                    )

                result.summaryFile.shouldExist()
                result.summary["schemaVersion"]?.jsonPrimitive?.content shouldBe "1"
                result.summary["preparedAt"]?.jsonPrimitive?.content shouldBe "2026-07-18T18:00:00Z"
                result.summary["gitSha"]?.jsonPrimitive?.content shouldBe "abc123"
                result.summary["modelHash"]?.jsonPrimitive?.content shouldBe "model-hash"
                result.summary["decision"]?.jsonPrimitive?.content shouldBe "partial"
                result.summary["teamCityBuildId"]?.toString() shouldBe "null"
                result.summary["dryRun"]
                    ?.jsonObject
                    ?.get(
                        key = "decision",
                    )?.jsonPrimitive
                    ?.content shouldBe "partial"
                result.summary["nextUnit"]?.jsonPrimitive?.content shouldBe "00-clear-staging.mcp.js"
                result.summary["commands"]
                    ?.jsonObject
                    ?.get(
                        key = "uploadPayload",
                    )?.jsonPrimitive
                    ?.content shouldBe
                    ".\\gradlew.bat uploadCanonicalFigmaPayload " +
                    "-PfigmaArtifactDirectory=\"${artifacts.toPath().toAbsolutePath().normalize()}\" " +
                    "-PfigmaExpectedGitSha=abc123 " +
                    "-PfigmaMcpUploadUrl=\"SINGLE_USE_UPLOAD_URL\""
                root.deleteRecursively()
            }

            test("rejects a TeamCity build that is not successful") {
                val root = Files.createTempDirectory("figma-handoff-build").toFile()
                val client =
                    object : TeamCityBuildArtifactClient {
                        override fun readBuild(
                            buildId: Long,
                        ): TeamCityBuild =
                            TeamCityBuild(
                                id = buildId,
                                state = "finished",
                                status = "FAILURE",
                                branchName = "main",
                                buildTypeName = "Generate main design model",
                                webUrl = null,
                            )

                        override fun downloadArtifacts(
                            buildId: Long,
                            outputDirectory: File,
                        ) = Unit
                    }
                val preparer =
                    TeamCityFigmaSyncHandoffPreparer(
                        teamCityClient = client,
                    )

                val exception =
                    shouldThrow<IllegalArgumentException> {
                        preparer.prepare(
                            request =
                                TeamCityFigmaSyncHandoffPreparer.Request(
                                    buildId = 1573,
                                    artifactDirectory = null,
                                    destinationRoot =
                                        root.resolve(
                                            relative = "downloads",
                                        ),
                                ),
                        )
                    }

                exception.message shouldBe
                    "Build 1573 must be finished and successful; found state 'finished' and status 'FAILURE'."
                root.deleteRecursively()
            }
        },
    )

internal fun File.writeArtifactFixture(
    payloadBytes: ByteArray? = null,
) {
    resolve(
        relative = "design-model.json",
    ).writeText(
        """{"branch":"main","gitSha":"abc123","modelHash":"model-hash"}""",
    )
    val visual =
        resolve(
            relative = "mcp-runners/visual",
        ).apply { mkdirs() }
    val metadata =
        resolve(
            relative = "mcp-runners/metadata",
        ).apply { mkdirs() }
    val visualManifest =
        visual.writeManifest(
            targets = listOf("preflight"),
            fullVisualSync = true,
            writeMetadata = false,
            payloadBytes = payloadBytes,
        )
    val metadataManifest =
        metadata.writeManifest(
            targets = listOf("metadata"),
            fullVisualSync = false,
            writeMetadata = true,
            payloadBytes = payloadBytes,
        )
    VisualSyncPlanJson().run {
        val body =
            VisualSyncPlanBody(
                schemaVersion = 1,
                decision = VisualSyncDecision.PARTIAL,
                reason = "target-model-fingerprints-changed",
                requiresVisualWrite = true,
                requiresMetadataWrite = true,
                executionScopes = listOf("preflight"),
                identity =
                    VisualSyncIdentity(
                        modelHash = "model-hash",
                        writerHash = "writer-hash",
                        transportHash = "transport-hash",
                        writerScopeFingerprintSchemaVersion = 1,
                    ),
                manifestHash = visualManifest.manifestHash,
            )
        write(
            VisualSyncPlan(
                body = body,
                planHash = hash(body),
            ),
            resolve(
                relative = "visual-sync-plan.json",
            ).absolutePath,
        )
    }
    resolve(
        relative = "sync-scope.json",
    ).writeText(
        """
        {
          "scope":"full-verification",
          "gitSha":"abc123",
          "modelHash":"model-hash",
          "writerHash":"writer-hash",
          "transportHash":"transport-hash",
          "visualRunnerManifestHash":"${visualManifest.manifestHash}",
          "metadataRunnerManifestHash":"${metadataManifest.manifestHash}",
          "visualSyncDecision":"partial"
        }
        """.trimIndent(),
    )
}

private fun File.writeManifest(
    targets: List<String>,
    fullVisualSync: Boolean,
    writeMetadata: Boolean,
    payloadBytes: ByteArray?,
): ExecutableRunnerManifest {
    val fileName = if (writeMetadata) "99-run-target.mcp.js" else "99-00-preflight.mcp.js"
    val source = "return { target: '${targets.single()}' };\n"
    resolve(
        relative = "00-clear-staging.mcp.js",
    ).writeText("return { cleared: true };\n")
    resolve(
        relative = fileName,
    ).writeText(source)
    val payloadImage =
        payloadBytes?.let { bytes ->
            val payloadFileName = "10-canonical-sync-payload.png"
            resolve(
                relative = payloadFileName,
            ).writeBytes(bytes)
            RunnerPayloadImage(
                fileName = payloadFileName,
                byteLength = bytes.size,
                sha256 =
                    Sha256Hash.of(
                        value = bytes,
                    ),
                textKeyword = "figmaSyncPayload",
            )
        }
    val files =
        listOf(
            "00-clear-staging.mcp.js",
            fileName,
        )
    val fileHashes =
        files.associateWith { name ->
            Sha256Hash.of(
                value =
                    resolve(
                        relative = name,
                    ).readBytes(),
            )
        }
    val draft =
        ExecutableRunnerManifest(
            path =
                resolve(
                    relative = "manifest.json",
                ).absolutePath,
            schemaVersion = 4,
            mode = "canonical",
            entrypoint = "trunk-sync",
            target = targets.first(),
            targets = targets,
            writeMetadata = writeMetadata,
            transport = if (payloadImage == null) "chunks" else "png",
            namespace = "test_staging",
            sectionNodeId = null,
            roots = emptyList(),
            allowCanonicalSections = false,
            fullVisualSync = fullVisualSync,
            allowPartial = false,
            metadataPageId = "1:2",
            modelPath = "design-model.json",
            scriptPath = "writer.mcp.js",
            modelHash = "model-hash",
            gitSha = "abc123",
            designModelLength = 10,
            scriptLength = 20,
            writerHash = "writer-hash",
            transportHash = "transport-hash",
            targetFingerprints = mapOf("preflight" to "model-target-hash"),
            writerScopeFingerprints =
                mapOf(
                    "preflight" to "writer-preflight-hash",
                    "metadata" to "writer-metadata-hash",
                ),
            writerScopeFingerprintSchemaVersion = 1,
            executionScopes = mapOf(fileName to targets.single()),
            payloadImage = payloadImage,
            files = files,
            fileHashes = fileHashes,
            manifestHash = "",
        )
    return ExecutableRunnerManifestJson().finalizeAndWrite(
        draft = draft,
        outputPath =
            resolve(
                relative = "manifest.json",
            ).absolutePath,
    )
}
