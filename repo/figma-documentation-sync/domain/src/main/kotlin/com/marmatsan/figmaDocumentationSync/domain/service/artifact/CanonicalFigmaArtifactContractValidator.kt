package com.marmatsan.figmaDocumentationSync.domain.service.artifact

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.CanonicalFigmaArtifactContract
import me.tatarka.inject.annotations.Inject

/** Validates that downloaded artifacts share one canonical main-branch identity. */
@Inject
class CanonicalFigmaArtifactContractValidator {
    fun validate(
        contract: CanonicalFigmaArtifactContract,
        expectedGitSha: String? = null,
    ): Result {
        val model = contract.model
        require(model.branch == MAIN_BRANCH) {
            "Canonical Figma artifacts require model branch 'main'; found '${model.branch}'."
        }
        require(model.gitSha.isNotBlank() && model.modelHash.isNotBlank()) {
            "design-model.json requires non-empty gitSha and modelHash values."
        }
        expectedGitSha?.takeIf(String::isNotBlank)?.let { expected ->
            requireEqual(
                actual = model.gitSha,
                expected = expected,
                description = "CI revision",
            )
        }

        val scope = contract.scope
        requireEqual(
            actual = scope.scope,
            expected = FULL_VERIFICATION,
            description = "Figma change scope",
        )
        requireEqual(
            actual = scope.gitSha,
            expected = model.gitSha,
            description = "Scope gitSha",
        )
        requireEqual(
            actual = scope.modelHash,
            expected = model.modelHash,
            description = "Scope modelHash",
        )

        val visualManifests = contract.manifests.filter { it.fullVisualSync }
        val metadataManifests = contract.manifests.filter { it.writeMetadata }
        require(visualManifests.size == 1 && metadataManifests.size == 1) {
            "Expected one full visual and one metadata manifest; found " +
                "${visualManifests.size} visual and ${metadataManifests.size} metadata."
        }

        val visual = visualManifests.single()
        val metadata = metadataManifests.single()
        validateManifest(
            name = "visual",
            manifest = visual,
            model = model,
        )
        validateManifest(
            name = "metadata",
            manifest = metadata,
            model = model,
        )
        requireEqual(
            actual = visual.writeMetadata,
            expected = false,
            description = "Visual manifest metadata flag",
        )
        requireEqual(
            actual = metadata.writeMetadata,
            expected = true,
            description = "Metadata manifest metadata flag",
        )

        validateSharedIdentity(
            contract = contract,
            visual = visual,
            metadata = metadata,
        )
        val decision =
            CanonicalFigmaArtifactContract.Decision.fromWireValue(
                value = contract.plan.decision,
            )
                ?: throw IllegalArgumentException(
                    "Unsupported visual sync decision '${contract.plan.decision}'.",
                )

        return Result(
            gitSha = model.gitSha,
            modelHash = model.modelHash,
            decision = decision,
        )
    }

    private fun validateManifest(
        name: String,
        manifest: CanonicalFigmaArtifactContract.Manifest,
        model: CanonicalFigmaArtifactContract.Model,
    ) {
        requireEqual(
            actual = manifest.mode,
            expected = "canonical",
            description = "$name manifest mode",
        )
        requireEqual(
            actual = manifest.gitSha,
            expected = model.gitSha,
            description = "$name manifest gitSha",
        )
        requireEqual(
            actual = manifest.modelHash,
            expected = model.modelHash,
            description = "$name manifest modelHash",
        )
        require(manifest.manifestHash.isNotBlank()) { "$name manifest requires a manifestHash." }
    }

    private fun validateSharedIdentity(
        contract: CanonicalFigmaArtifactContract,
        visual: CanonicalFigmaArtifactContract.Manifest,
        metadata: CanonicalFigmaArtifactContract.Manifest,
    ) {
        val scope = contract.scope
        val plan = contract.plan
        requireEqual(
            actual = metadata.writerHash,
            expected = visual.writerHash,
            description = "Metadata manifest writerHash",
        )
        requireEqual(
            actual = scope.writerHash,
            expected = visual.writerHash,
            description = "Scope writerHash",
        )
        requireEqual(
            actual = plan.identity.writerHash,
            expected = visual.writerHash,
            description = "Visual plan writerHash",
        )
        requireEqual(
            actual = metadata.transportHash,
            expected = visual.transportHash,
            description = "Metadata manifest transportHash",
        )
        requireEqual(
            actual = scope.transportHash,
            expected = visual.transportHash,
            description = "Scope transportHash",
        )
        requireEqual(
            actual = plan.identity.transportHash,
            expected = visual.transportHash,
            description = "Visual plan transportHash",
        )
        requireEqual(
            actual = plan.identity.modelHash,
            expected = contract.model.modelHash,
            description = "Visual plan modelHash",
        )
        requireEqual(
            actual = plan.manifestHash,
            expected = visual.manifestHash,
            description = "Visual plan manifestHash",
        )
        requireEqual(
            actual = scope.visualRunnerManifestHash,
            expected = visual.manifestHash,
            description = "Scope visual manifestHash",
        )
        requireEqual(
            actual = scope.metadataRunnerManifestHash,
            expected = metadata.manifestHash,
            description = "Scope metadata manifestHash",
        )
        requireEqual(
            actual = scope.visualSyncDecision,
            expected = plan.decision,
            description = "Scope visual decision",
        )
    }

    private fun requireEqual(
        actual: Any?,
        expected: Any?,
        description: String,
    ) {
        require(actual == expected) {
            "$description mismatch: expected '$expected', found '$actual'."
        }
    }

    data class Result(
        val gitSha: String,
        val modelHash: String,
        val decision: CanonicalFigmaArtifactContract.Decision,
    )

    private companion object {
        const val MAIN_BRANCH = "main"
        const val FULL_VERIFICATION = "full-verification"
    }
}
