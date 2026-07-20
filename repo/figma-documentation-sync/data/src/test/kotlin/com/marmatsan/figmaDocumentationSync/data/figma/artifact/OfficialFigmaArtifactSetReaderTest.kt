package com.marmatsan.figmaDocumentationSync.data.figma.artifact

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class OfficialFigmaArtifactSetReaderTest : FunSpec(
    {
    test("reads the official artifact files recursively") {
        val root = Files.createTempDirectory("figma-artifact-set").toFile()
        try {
            root.resolve(
                relative = "design-model.json"
            ).writeText(
                """{"branch":"main","gitSha":"abc123","modelHash":"model-hash"}"""
            )
            root.resolve(
                relative = "sync-scope.json"
            ).writeText(
                "\uFEFF" + """
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
            root.resolve(
                relative = "visual-sync-plan.json"
            ).writeText(
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
            val visualDirectory = root.resolve(
                relative = "mcp-runners/visual"
            ).apply { mkdirs() }
            val metadataDirectory = root.resolve(
                relative = "mcp-runners/metadata"
            ).apply { mkdirs() }
            visualDirectory.resolve(
                relative = "manifest.json"
            ).writeText(
                manifestJson(
                    fullVisualSync = true,
                    writeMetadata = false,
                    manifestHash = "visual-hash"
                )
            )
            metadataDirectory.resolve(
                relative = "manifest.json"
            ).writeText(
                manifestJson(
                    fullVisualSync = false,
                    writeMetadata = true,
                    manifestHash = "metadata-hash"
                )
            )

            val result = OfficialFigmaArtifactSetReader().read(root.absolutePath)

            result.contract.model.branch shouldBe "main"
            result.contract.plan.decision shouldBe "partial"
            result.visualManifestPath shouldBe visualDirectory.resolve(
                relative = "manifest.json"
            ).toPath()
            result.metadataManifestPath shouldBe metadataDirectory.resolve(
                relative = "manifest.json"
            ).toPath()
        } finally {
            root.deleteRecursively()
        }
    }
}
)

private fun manifestJson(
    fullVisualSync: Boolean,
    writeMetadata: Boolean,
    manifestHash: String
) =
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
