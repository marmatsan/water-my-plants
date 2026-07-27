package com.marmatsan.waterMyPlants.projectConfig

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityBuild
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityBuildArtifactClient
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.TeamCityFigmaSyncHandoffPreparer
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.writeArtifactFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

internal class TeamCityCanonicalFigmaPayloadUploaderTest :
    FunSpec(
        {
            val uploadUrl =
                "https://mcp.figma.com/mcp/upload/0d188fd2-0c70-46f5-b30a-6dd4f3904998/submit?scaleMode=FILL"
            val temporaryDirectory =
                tempdir(
                    prefix = "teamcity-figma-payload-uploader",
                )

            test("uploads the hash-verified PNG from a successful main TeamCity build") {
                val root = temporaryDirectory.resolve("upload").apply { mkdirs() }
                val payload = PayloadPngEncoder().encode("{\"canonical\":true}")
                val client =
                    fixtureClient(
                        payload = payload,
                    )
                val handoffPreparer =
                    TeamCityFigmaSyncHandoffPreparer(
                        teamCityClient = client,
                        clock =
                            Clock.fixed(
                                Instant.parse(
                                    "2026-07-19T08:00:00Z",
                                ),
                                ZoneOffset.UTC,
                            ),
                    )
                var uploadedUrl = ""
                var uploadedBytes = byteArrayOf()
                val uploader =
                    TeamCityCanonicalFigmaPayloadUploader(
                        handoffPreparer = handoffPreparer,
                        uploadPng = { url, bytes ->
                            uploadedUrl = url
                            uploadedBytes = bytes
                        },
                    )

                val result =
                    uploader.upload(
                        TeamCityCanonicalFigmaPayloadUploader.Request(
                            buildId = 1672,
                            artifactDirectory = null,
                            uploadUrl = uploadUrl,
                            destinationRoot = root,
                            expectedGitSha = "abc123",
                        ),
                    )

                uploadedUrl shouldBe uploadUrl
                uploadedBytes shouldBe payload
                result.buildId shouldBe 1672
                result.gitSha shouldBe "abc123"
                result.modelHash shouldBe "model-hash"
                result.payloadByteLength shouldBe payload.size
                result.payloadSha256 shouldBe
                    Sha256Hash.of(
                        value = payload,
                    )
                result.artifactDirectory
                    .resolve(
                        relative = "figma-sync-handoff.json",
                    ).shouldExist()
            }

            test("rejects a PNG whose bytes no longer match the canonical manifest") {
                val root = temporaryDirectory.resolve("tamper").apply { mkdirs() }
                val payload = PayloadPngEncoder().encode("{\"canonical\":true}")
                val client =
                    fixtureClient(
                        payload = payload,
                    ) { outputDirectory ->
                        val path =
                            outputDirectory.resolve(
                                relative = "mcp-runners/visual/10-canonical-sync-payload.png",
                            )
                        val tampered = path.readBytes()
                        tampered[tampered.lastIndex] = (tampered.last() + 1).toByte()
                        path.writeBytes(tampered)
                    }
                var uploads = 0
                val uploader =
                    TeamCityCanonicalFigmaPayloadUploader(
                        handoffPreparer =
                            TeamCityFigmaSyncHandoffPreparer(
                                teamCityClient = client,
                            ),
                        uploadPng = { _, _ -> uploads += 1 },
                    )

                val failure =
                    shouldThrow<IllegalArgumentException> {
                        uploader.upload(
                            TeamCityCanonicalFigmaPayloadUploader.Request(
                                buildId = 1672,
                                artifactDirectory = null,
                                uploadUrl = uploadUrl,
                                destinationRoot = root,
                            ),
                        )
                    }

                failure.message?.startsWith(
                    prefix = "Canonical PNG payload hash mismatch:",
                ) shouldBe true
                uploads shouldBe 0
            }

            test("uploads a previously downloaded artifact only with an explicit revision") {
                val root = temporaryDirectory.resolve("artifact-directory").apply { mkdirs() }
                val artifacts =
                    root
                        .resolve(
                            relative = "artifacts",
                        ).apply {
                            mkdirs()
                            writeArtifactFixture(
                                payloadBytes = PayloadPngEncoder().encode("{\"canonical\":true}"),
                            )
                        }
                val client =
                    object : TeamCityBuildArtifactClient {
                        override fun readBuild(
                            buildId: Long,
                        ): TeamCityBuild =
                            error("TeamCity must not be called")

                        override fun downloadArtifacts(
                            buildId: Long,
                            outputDirectory: File,
                        ) =
                            error("TeamCity must not be called")
                    }
                var uploads = 0
                val uploader =
                    TeamCityCanonicalFigmaPayloadUploader(
                        handoffPreparer =
                            TeamCityFigmaSyncHandoffPreparer(
                                teamCityClient = client,
                            ),
                        uploadPng = { _, _ -> uploads += 1 },
                    )

                val missingRevision =
                    shouldThrow<IllegalArgumentException> {
                        uploader.upload(
                            TeamCityCanonicalFigmaPayloadUploader.Request(
                                buildId = null,
                                artifactDirectory = artifacts,
                                uploadUrl = uploadUrl,
                                destinationRoot = root,
                            ),
                        )
                    }
                missingRevision.message shouldBe
                    "figmaExpectedGitSha is required with figmaArtifactDirectory."

                uploader.upload(
                    TeamCityCanonicalFigmaPayloadUploader.Request(
                        buildId = null,
                        artifactDirectory = artifacts,
                        uploadUrl = uploadUrl,
                        destinationRoot = root,
                        expectedGitSha = "abc123",
                    ),
                )

                uploads shouldBe 1
            }
        },
    )

private fun fixtureClient(
    payload: ByteArray,
    afterWrite: (File) -> Unit = {},
): TeamCityBuildArtifactClient =
    object : TeamCityBuildArtifactClient {
        override fun readBuild(
            buildId: Long,
        ): TeamCityBuild =
            TeamCityBuild(
                id = buildId,
                state = "finished",
                status = "SUCCESS",
                branchName = "main",
                buildTypeName = "Generate main design model",
                webUrl = null,
            )

        override fun downloadArtifacts(
            buildId: Long,
            outputDirectory: File,
        ) {
            outputDirectory.writeArtifactFixture(
                payloadBytes = payload,
            )
            afterWrite(outputDirectory)
        }
    }
