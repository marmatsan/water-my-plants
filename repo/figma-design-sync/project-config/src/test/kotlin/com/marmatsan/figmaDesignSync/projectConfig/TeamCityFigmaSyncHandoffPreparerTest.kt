package com.marmatsan.figmaDesignSync.projectConfig

import com.marmatsan.figmaDesignSync.teamcityAdapter.TeamCityBuild
import com.marmatsan.figmaDesignSync.teamcityAdapter.TeamCityBuildArtifactClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonPrimitive

internal class TeamCityFigmaSyncHandoffPreparerTest : FunSpec({
    test("prepares a validated handoff from an existing artifact directory") {
        val root = Files.createTempDirectory("figma-handoff").toFile()
        val artifacts = root.resolve("artifacts").apply {
            mkdirs()
            writeArtifactFixture()
        }
        val tools = root.resolve("tools").apply { mkdirs() }
        val client = object : TeamCityBuildArtifactClient {
            override fun readBuild(buildId: Long): TeamCityBuild = error("TeamCity must not be called")

            override fun downloadArtifacts(buildId: Long, outputDirectory: File) =
                error("TeamCity must not be called")
        }
        val preparer = TeamCityFigmaSyncHandoffPreparer(
            teamCityClient = client,
            clock = Clock.fixed(Instant.parse("2026-07-18T18:00:00Z"), ZoneOffset.UTC),
            execute = { _, _ -> error("Node must not run when the executor build is skipped") }
        )

        val result = preparer.prepare(
            TeamCityFigmaSyncHandoffPreparer.Request(
                buildId = null,
                artifactDirectory = artifacts,
                destinationRoot = root.resolve("downloads"),
                projectRootDirectory = root,
                toolsDirectory = tools,
                skipExecutorBuild = true,
                expectedGitSha = "abc123"
            )
        )

        result.summaryFile.shouldExist()
        result.summary["schemaVersion"]?.jsonPrimitive?.content shouldBe "1"
        result.summary["preparedAt"]?.jsonPrimitive?.content shouldBe "2026-07-18T18:00:00Z"
        result.summary["gitSha"]?.jsonPrimitive?.content shouldBe "abc123"
        result.summary["modelHash"]?.jsonPrimitive?.content shouldBe "model-hash"
        result.summary["decision"]?.jsonPrimitive?.content shouldBe "partial"
        result.summary["teamCityBuildId"] shouldBe JsonNull
        result.summary["dryRun"] shouldBe JsonNull
        root.deleteRecursively()
    }

    test("rejects a TeamCity build that is not successful") {
        val root = Files.createTempDirectory("figma-handoff-build").toFile()
        val client = object : TeamCityBuildArtifactClient {
            override fun readBuild(buildId: Long): TeamCityBuild =
                TeamCityBuild(
                    id = buildId,
                    state = "finished",
                    status = "FAILURE",
                    branchName = "main",
                    buildTypeName = "Generate main design model",
                    webUrl = null
                )

            override fun downloadArtifacts(buildId: Long, outputDirectory: File) = Unit
        }
        val preparer = TeamCityFigmaSyncHandoffPreparer(teamCityClient = client)

        val exception = shouldThrow<IllegalArgumentException> {
            preparer.prepare(
                TeamCityFigmaSyncHandoffPreparer.Request(
                    buildId = 1573,
                    artifactDirectory = null,
                    destinationRoot = root.resolve("downloads"),
                    projectRootDirectory = root,
                    toolsDirectory = root.resolve("tools"),
                    skipExecutorBuild = true
                )
            )
        }

        exception.message shouldBe
            "Build 1573 must be finished and successful; found state 'finished' and status 'FAILURE'."
        root.deleteRecursively()
    }
})

private fun File.writeArtifactFixture() {
    resolve("design-model.json").writeText(
        """{"branch":"main","gitSha":"abc123","modelHash":"model-hash"}"""
    )
    resolve("sync-scope.json").writeText(
        """
        {
          "scope":"full-verification",
          "gitSha":"abc123",
          "modelHash":"model-hash",
          "writerHash":"writer-hash",
          "transportHash":"transport-hash",
          "visualRunnerManifestHash":"visual-hash",
          "metadataRunnerManifestHash":"metadata-hash",
          "visualSyncDecision":"partial"
        }
        """.trimIndent()
    )
    resolve("visual-sync-plan.json").writeText(
        """
        {
          "decision":"partial",
          "manifestHash":"visual-hash",
          "identity":{
            "modelHash":"model-hash",
            "writerHash":"writer-hash",
            "transportHash":"transport-hash"
          }
        }
        """.trimIndent()
    )
    val visual = resolve("mcp-runners/visual").apply { mkdirs() }
    val metadata = resolve("mcp-runners/metadata").apply { mkdirs() }
    visual.resolve("manifest.json").writeText(
        artifactManifest(fullVisualSync = true, writeMetadata = false, manifestHash = "visual-hash")
    )
    metadata.resolve("manifest.json").writeText(
        artifactManifest(fullVisualSync = false, writeMetadata = true, manifestHash = "metadata-hash")
    )
}

private fun artifactManifest(
    fullVisualSync: Boolean,
    writeMetadata: Boolean,
    manifestHash: String
): String =
    """
    {
      "mode":"official",
      "gitSha":"abc123",
      "modelHash":"model-hash",
      "manifestHash":"$manifestHash",
      "writerHash":"writer-hash",
      "transportHash":"transport-hash",
      "fullVisualSync":$fullVisualSync,
      "writeMetadata":$writeMetadata
    }
    """.trimIndent()
