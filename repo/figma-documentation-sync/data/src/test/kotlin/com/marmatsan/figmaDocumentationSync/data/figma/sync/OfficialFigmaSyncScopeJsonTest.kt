package com.marmatsan.figmaDocumentationSync.data.figma.sync

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaImpact
import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDocumentationSync.domain.model.sync.OfficialFigmaSyncScope
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class OfficialFigmaSyncScopeJsonTest : FunSpec(
    {
    test("round trips a model-neutral official scope") {
        val directory = Files.createTempDirectory("figma-sync-scope").toFile()
        try {
            val path = directory.resolve("sync-scope.json")
            val expected = OfficialFigmaSyncScope(
                scope = FigmaVerificationScope.MODEL_NEUTRAL,
                figmaImpact = FigmaImpact.MODEL_NEUTRAL,
                affectedVisualTargets = emptyList(),
                comparisonBase = "abc123",
                gitSha = "def456",
                modelHash = null,
                writerHash = null,
                transportHash = null,
                targetFingerprints = null,
                writerScopeFingerprints = null,
                writerScopeFingerprintSchemaVersion = null,
                visualRunnerManifestHash = null,
                metadataRunnerManifestHash = null,
                visualSyncDecision = null,
                visualSyncPlanHash = null
            )

            val adapter = OfficialFigmaSyncScopeJson()
            adapter.write(
                expected,
                path.absolutePath
            )

            adapter.read(path.absolutePath) shouldBe expected
        } finally {
            directory.deleteRecursively()
        }
    }

    test("reads visual and metadata runner identities recursively") {
        val directory = Files.createTempDirectory("figma-runner-manifests").toFile()
        try {
            val visual = directory.resolve("visual/manifest.json").apply {
                parentFile.mkdirs()
                writeText(
                    manifest(
                        fullVisualSync = true,
                        writeMetadata = false,
                        hash = "visual-hash"
                    )
                )
            }
            directory.resolve("metadata/manifest.json").apply {
                parentFile.mkdirs()
                writeText(
                    manifest(
                        fullVisualSync = false,
                        writeMetadata = true,
                        hash = "metadata-hash"
                    )
                )
            }

            val manifests = OfficialFigmaSyncScopeJson().readRunnerManifests(directory.absolutePath)

            manifests.single { it.fullVisualSync }.path shouldBe visual.toPath().toAbsolutePath().normalize().toString()
            manifests.single { it.writeMetadata }.manifestHash shouldBe "metadata-hash"
        } finally {
            directory.deleteRecursively()
        }
    }
}
)

private fun manifest(
    fullVisualSync: Boolean,
    writeMetadata: Boolean,
    hash: String
) =
    """
    {
      "fullVisualSync": $fullVisualSync,
      "writeMetadata": $writeMetadata,
      "modelHash": "model-hash",
      "writerHash": "writer-hash",
      "transportHash": "transport-hash",
      "targetFingerprints": {"versions": "target-hash"},
      "writerScopeFingerprints": {"versions": "scope-hash"},
      "writerScopeFingerprintSchemaVersion": 1,
      "executionScopes": {"99-run-target.mcp.js": "versions"},
      "manifestHash": "$hash"
    }
    """.trimIndent()
