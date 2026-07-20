package com.marmatsan.figmaDocumentationSync.projectConfig

import com.marmatsan.figmaDocumentationSync.data.hash.Sha256Hash
import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityBuild
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityBuildArtifactClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

internal class TeamCityOfficialFigmaPayloadUploaderTest : FunSpec(
    {
    val uploadUrl =
        "https://mcp.figma.com/mcp/upload/0d188fd2-0c70-46f5-b30a-6dd4f3904998/submit?scaleMode=FILL"

    test("uploads the hash-verified PNG from a successful main TeamCity build") {
        val root = Files.createTempDirectory("figma-payload-upload").toFile()
        val payload = PayloadPngEncoder().encode("{\"official\":true}")
        val client = fixtureClient(
            payload = payload
        )
        val handoffPreparer = TeamCityFigmaSyncHandoffPreparer(
            teamCityClient = client,
            clock = Clock.fixed(
                Instant.parse("2026-07-19T08:00:00Z"),
                ZoneOffset.UTC
            )
        )
        var uploadedUrl = ""
        var uploadedBytes = byteArrayOf()
        val uploader = TeamCityOfficialFigmaPayloadUploader(
            handoffPreparer = handoffPreparer,
            uploadPng = { url, bytes ->
                uploadedUrl = url
                uploadedBytes = bytes
            }
        )

        val result = uploader.upload(
            TeamCityOfficialFigmaPayloadUploader.Request(
                buildId = 1672,
                artifactDirectory = null,
                uploadUrl = uploadUrl,
                destinationRoot = root,
                expectedGitSha = "abc123"
            )
        )

        uploadedUrl shouldBe uploadUrl
        uploadedBytes shouldBe payload
        result.buildId shouldBe 1672
        result.gitSha shouldBe "abc123"
        result.modelHash shouldBe "model-hash"
        result.payloadByteLength shouldBe payload.size
        result.payloadSha256 shouldBe Sha256Hash.of(payload)
        result.artifactDirectory.resolve("figma-sync-handoff.json").shouldExist()
        root.deleteRecursively()
    }

    test("rejects a PNG whose bytes no longer match the official manifest") {
        val root = Files.createTempDirectory("figma-payload-tamper").toFile()
        val payload = PayloadPngEncoder().encode("{\"official\":true}")
        val client = fixtureClient(
            payload = payload
        ) { outputDirectory ->
            val path = outputDirectory.resolve(
                "mcp-runners/visual/10-official-sync-payload.png"
            )
            val tampered = path.readBytes()
            tampered[tampered.lastIndex] = (tampered.last() + 1).toByte()
            path.writeBytes(tampered)
        }
        var uploads = 0
        val uploader = TeamCityOfficialFigmaPayloadUploader(
            handoffPreparer = TeamCityFigmaSyncHandoffPreparer(
                teamCityClient = client
            ),
            uploadPng = { _, _ -> uploads += 1 }
        )

        val failure = shouldThrow<IllegalArgumentException> {
            uploader.upload(
                TeamCityOfficialFigmaPayloadUploader.Request(
                    buildId = 1672,
                    artifactDirectory = null,
                    uploadUrl = uploadUrl,
                    destinationRoot = root
                )
            )
        }

        failure.message?.startsWith("Official PNG payload hash mismatch:") shouldBe true
        uploads shouldBe 0
        root.deleteRecursively()
    }

    test("uploads a previously downloaded artifact only with an explicit revision") {
        val root = Files.createTempDirectory("figma-payload-directory").toFile()
        val artifacts = root.resolve("artifacts").apply {
            mkdirs()
            writeArtifactFixture(PayloadPngEncoder().encode("{\"official\":true}"))
        }
        val client = object : TeamCityBuildArtifactClient {
            override fun readBuild(
                buildId: Long
            ): TeamCityBuild =
                error("TeamCity must not be called")

            override fun downloadArtifacts(
                buildId: Long,
                outputDirectory: File
            ) =
                error("TeamCity must not be called")
        }
        var uploads = 0
        val uploader = TeamCityOfficialFigmaPayloadUploader(
            handoffPreparer = TeamCityFigmaSyncHandoffPreparer(
                teamCityClient = client
            ),
            uploadPng = { _, _ -> uploads += 1 }
        )

        val missingRevision = shouldThrow<IllegalArgumentException> {
            uploader.upload(
                TeamCityOfficialFigmaPayloadUploader.Request(
                    buildId = null,
                    artifactDirectory = artifacts,
                    uploadUrl = uploadUrl,
                    destinationRoot = root
                )
            )
        }
        missingRevision.message shouldBe
            "figmaExpectedGitSha is required with figmaArtifactDirectory."

        uploader.upload(
            TeamCityOfficialFigmaPayloadUploader.Request(
                buildId = null,
                artifactDirectory = artifacts,
                uploadUrl = uploadUrl,
                destinationRoot = root,
                expectedGitSha = "abc123"
            )
        )

        uploads shouldBe 1
        root.deleteRecursively()
    }
}
)

private fun fixtureClient(
    payload: ByteArray,
    afterWrite: (File) -> Unit = {}
): TeamCityBuildArtifactClient = object : TeamCityBuildArtifactClient {
    override fun readBuild(
        buildId: Long
    ): TeamCityBuild = TeamCityBuild(
        id = buildId,
        state = "finished",
        status = "SUCCESS",
        branchName = "main",
        buildTypeName = "Generate main design model",
        webUrl = null
    )

    override fun downloadArtifacts(
        buildId: Long,
        outputDirectory: File
    ) {
        outputDirectory.writeArtifactFixture(payload)
        afterWrite(outputDirectory)
    }
}
