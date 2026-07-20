package com.marmatsan.figmaDocumentationSync.domain.service.writer

import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaSyncMetadata
import com.marmatsan.figmaDocumentationSync.domain.model.writer.RunnerManifest
import com.marmatsan.figmaDocumentationSync.domain.model.writer.VisualSyncDecision
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

internal class VisualSyncPlannerTest : FunSpec(
    {
    val planner = VisualSyncPlanner { body -> "sha256:${body.reason}" }

    test("missing Figma metadata fails closed to a complete visual sync") {
        val plan = planner.create(
            manifest,
            null
        )

        plan.body.decision shouldBe VisualSyncDecision.FULL
        plan.body.reason shouldBe "figma-metadata-unavailable"
        plan.body.executionScopes.shouldContainExactly(manifest.executionScopes.values)
    }

    test("unchanged visual inputs skip Figma even when transport identity changed") {
        val plan = planner.create(
            manifest.copy(
                modelHash = "sha256:model-same"
            ),
            previousMetadata(
                modelHash = "sha256:model-same"
            )
        )

        plan.body.decision shouldBe VisualSyncDecision.NONE
        plan.body.requiresVisualWrite shouldBe false
        plan.planHash shouldBe "sha256:visual-input-unchanged"
    }

    test("model changes select changed target fingerprints plus preflight") {
        val plan = planner.create(
            manifest,
            previousMetadata(
                modelHash = "sha256:model-old"
            )
        )

        plan.body.decision shouldBe VisualSyncDecision.PARTIAL
        plan.body.reason shouldBe "target-model-fingerprints-changed"
        plan.body.executionScopes.shouldContainExactly(
            "preflight",
            "waterMyPlants.libraries.androidx"
        )
    }

    test("scoped writer changes select only their execution family") {
        val previous = previousMetadata().copy(
            writerHash = "sha256:writer-old",
            writerScopeFingerprints = previousMetadata().writerScopeFingerprints?.plus(
                pair = "waterMyPlants.libraries.androidx" to "sha256:catalog-writer-old"
            )
        )

        val plan = planner.create(
            manifest,
            previous
        )

        plan.body.decision shouldBe VisualSyncDecision.PARTIAL
        plan.body.reason shouldBe "writer-scope-fingerprints-changed"
        plan.body.executionScopes.shouldContainExactly(
            "preflight",
            "waterMyPlants.libraries.androidx"
        )
    }

    test("Kotlin planner changes select their scope even when the compiled TypeScript hash is unchanged") {
        val previous = previousMetadata().copy(
            writerScopeFingerprints = previousMetadata().writerScopeFingerprints?.plus(
                pair = "waterMyPlants.libraries.androidx" to "sha256:catalog-writer-old"
            )
        )

        val plan = planner.create(
            manifest,
            previous
        )

        plan.body.decision shouldBe VisualSyncDecision.PARTIAL
        plan.body.reason shouldBe "writer-scope-fingerprints-changed"
        plan.body.executionScopes.shouldContainExactly(
            "preflight",
            "waterMyPlants.libraries.androidx"
        )
    }

    test("metadata-only writer changes still create a partial metadata plan") {
        val previous = previousMetadata().copy(
            writerHash = "sha256:writer-old",
            writerScopeFingerprints = previousMetadata().writerScopeFingerprints?.plus(
                pair = "metadata" to "sha256:metadata-writer-old"
            )
        )

        val plan = planner.create(
            manifest,
            previous
        )

        plan.body.decision shouldBe VisualSyncDecision.PARTIAL
        plan.body.reason shouldBe "metadata-writer-fingerprint-changed"
        plan.body.executionScopes.shouldContainExactly("preflight")
    }

    test("fingerprint schema changes fail closed to a complete visual sync") {
        val plan = planner.create(
            manifest,
            previousMetadata().copy(
                writerScopeFingerprintSchemaVersion = 2
            )
        )

        plan.body.decision shouldBe VisualSyncDecision.FULL
        plan.body.reason shouldBe "writer-scope-fingerprint-schema-changed"
    }
}
)

private val manifest = RunnerManifest(
    path = "manifest.json",
    fullVisualSync = true,
    writeMetadata = false,
    modelHash = "sha256:model-new",
    writerHash = "sha256:writer-new",
    transportHash = "sha256:transport-new",
    manifestHash = "sha256:manifest",
    writerScopeFingerprintSchemaVersion = 1,
    executionScopes = linkedMapOf(
        "99-00-preflight.mcp.js" to "preflight",
        "99-01-versions.mcp.js" to "versions",
        "99-02-00-libraries-androidx.mcp.js" to "waterMyPlants.libraries.androidx",
        "99-02-99-libraries-cleanup.mcp.js" to "waterMyPlants.libraries.cleanup"
    ),
    targetFingerprints = mapOf(
        "preflight" to "sha256:preflight-new",
        "versions" to "sha256:versions-same",
        "waterMyPlants.libraries.androidx" to "sha256:androidx-new",
        "waterMyPlants.libraries.cleanup" to "sha256:cleanup-same"
    ),
    writerScopeFingerprints = mapOf(
        "preflight" to "sha256:preflight-writer-same",
        "versions" to "sha256:versions-writer-same",
        "waterMyPlants.libraries.androidx" to "sha256:catalog-writer-same",
        "waterMyPlants.libraries.cleanup" to "sha256:catalog-writer-same",
        "metadata" to "sha256:metadata-writer-same"
    )
)

private fun previousMetadata(
    modelHash: String = manifest.modelHash
) = FigmaSyncMetadata(
    modelHash = modelHash,
    writerHash = manifest.writerHash,
    targetFingerprints = manifest.targetFingerprints +
        ("waterMyPlants.libraries.androidx" to "sha256:androidx-old"),
    writerScopeFingerprints = manifest.writerScopeFingerprints,
    writerScopeFingerprintSchemaVersion = manifest.writerScopeFingerprintSchemaVersion
)
