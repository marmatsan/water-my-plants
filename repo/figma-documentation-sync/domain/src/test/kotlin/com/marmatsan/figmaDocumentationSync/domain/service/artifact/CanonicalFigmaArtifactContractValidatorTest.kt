package com.marmatsan.figmaDocumentationSync.domain.service.artifact

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.CanonicalFigmaArtifactContract
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class CanonicalFigmaArtifactContractValidatorTest :
    FunSpec(
        {
            val validator = CanonicalFigmaArtifactContractValidator()

            test("accepts one consistent canonical main artifact set") {
                val result =
                    validator.validate(
                        contract = validContract(),
                        expectedGitSha = "abc123"
                    )

                result.gitSha shouldBe "abc123"
                result.modelHash shouldBe "model-hash"
                result.decision shouldBe CanonicalFigmaArtifactContract.Decision.PARTIAL
            }

            test("rejects a branch-local design model") {
                val contract =
                    validContract().copy(
                        model =
                            validContract().model.copy(
                                branch = "feature/not-main"
                            )
                    )

                val exception =
                    shouldThrow<IllegalArgumentException> {
                        validator.validate(contract)
                    }

                exception.message shouldBe
                    "Canonical Figma artifacts require model branch 'main'; found 'feature/not-main'."
            }

            test("rejects a mismatched writer identity") {
                val contract =
                    validContract().copy(
                        scope =
                            validContract().scope.copy(
                                writerHash = "other-writer"
                            )
                    )

                val exception =
                    shouldThrow<IllegalArgumentException> {
                        validator.validate(contract)
                    }

                exception.message shouldBe
                    "Scope writerHash mismatch: expected 'writer-hash', found 'other-writer'."
            }

            test("rejects an unsupported visual decision") {
                val contract =
                    validContract().copy(
                        scope =
                            validContract().scope.copy(
                                visualSyncDecision = "targeted"
                            ),
                        plan =
                            validContract().plan.copy(
                                decision = "targeted"
                            )
                    )

                val exception =
                    shouldThrow<IllegalArgumentException> {
                        validator.validate(contract)
                    }

                exception.message shouldBe "Unsupported visual sync decision 'targeted'."
            }
        }
    )

private fun validContract() =
    CanonicalFigmaArtifactContract(
        model =
            CanonicalFigmaArtifactContract.Model(
                branch = "main",
                gitSha = "abc123",
                modelHash = "model-hash"
            ),
        scope =
            CanonicalFigmaArtifactContract.Scope(
                scope = "full-verification",
                gitSha = "abc123",
                modelHash = "model-hash",
                writerHash = "writer-hash",
                transportHash = "transport-hash",
                visualRunnerManifestHash = "visual-hash",
                metadataRunnerManifestHash = "metadata-hash",
                visualSyncDecision = "partial"
            ),
        plan =
            CanonicalFigmaArtifactContract.Plan(
                decision = "partial",
                manifestHash = "visual-hash",
                identity =
                    CanonicalFigmaArtifactContract.Identity(
                        modelHash = "model-hash",
                        writerHash = "writer-hash",
                        transportHash = "transport-hash"
                    )
            ),
        manifests =
            listOf(
                CanonicalFigmaArtifactContract.Manifest(
                    mode = "canonical",
                    gitSha = "abc123",
                    modelHash = "model-hash",
                    manifestHash = "visual-hash",
                    writerHash = "writer-hash",
                    transportHash = "transport-hash",
                    fullVisualSync = true,
                    writeMetadata = false
                ),
                CanonicalFigmaArtifactContract.Manifest(
                    mode = "canonical",
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
