package com.marmatsan.figmaDocumentationSync.domain.service.artifact

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.OfficialFigmaArtifactContract
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class OfficialFigmaArtifactContractValidatorTest : FunSpec({
    val validator = OfficialFigmaArtifactContractValidator()

    test("accepts one consistent official main artifact set") {
        val result = validator.validate(validContract(), expectedGitSha = "abc123")

        result.gitSha shouldBe "abc123"
        result.modelHash shouldBe "model-hash"
        result.decision shouldBe OfficialFigmaArtifactContract.Decision.PARTIAL
    }

    test("rejects a branch-local design model") {
        val contract = validContract().copy(
            model = validContract().model.copy(branch = "feature/not-main")
        )

        val exception = shouldThrow<IllegalArgumentException> {
            validator.validate(contract)
        }

        exception.message shouldBe
            "Official Figma artifacts require model branch 'main'; found 'feature/not-main'."
    }

    test("rejects a mismatched writer identity") {
        val contract = validContract().copy(
            scope = validContract().scope.copy(writerHash = "other-writer")
        )

        val exception = shouldThrow<IllegalArgumentException> {
            validator.validate(contract)
        }

        exception.message shouldBe
            "Scope writerHash mismatch: expected 'writer-hash', found 'other-writer'."
    }

    test("rejects an unsupported visual decision") {
        val contract = validContract().copy(
            scope = validContract().scope.copy(visualSyncDecision = "targeted"),
            plan = validContract().plan.copy(decision = "targeted")
        )

        val exception = shouldThrow<IllegalArgumentException> {
            validator.validate(contract)
        }

        exception.message shouldBe "Unsupported visual sync decision 'targeted'."
    }
})

private fun validContract() = OfficialFigmaArtifactContract(
    model = OfficialFigmaArtifactContract.Model(
        branch = "main",
        gitSha = "abc123",
        modelHash = "model-hash"
    ),
    scope = OfficialFigmaArtifactContract.Scope(
        scope = "full-verification",
        gitSha = "abc123",
        modelHash = "model-hash",
        writerHash = "writer-hash",
        transportHash = "transport-hash",
        visualRunnerManifestHash = "visual-hash",
        metadataRunnerManifestHash = "metadata-hash",
        visualSyncDecision = "partial"
    ),
    plan = OfficialFigmaArtifactContract.Plan(
        decision = "partial",
        manifestHash = "visual-hash",
        identity = OfficialFigmaArtifactContract.Identity(
            modelHash = "model-hash",
            writerHash = "writer-hash",
            transportHash = "transport-hash"
        )
    ),
    manifests = listOf(
        OfficialFigmaArtifactContract.Manifest(
            mode = "official",
            gitSha = "abc123",
            modelHash = "model-hash",
            manifestHash = "visual-hash",
            writerHash = "writer-hash",
            transportHash = "transport-hash",
            fullVisualSync = true,
            writeMetadata = false
        ),
        OfficialFigmaArtifactContract.Manifest(
            mode = "official",
            gitSha = "abc123",
            modelHash = "model-hash",
            manifestHash = "metadata-hash",
            writerHash = "writer-hash",
            transportHash = "transport-hash",
            fullVisualSync = false,
            writeMetadata = true
        )
    )
)
