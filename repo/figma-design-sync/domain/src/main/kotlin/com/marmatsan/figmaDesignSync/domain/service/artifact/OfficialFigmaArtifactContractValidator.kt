package com.marmatsan.figmaDesignSync.domain.service.artifact

import com.marmatsan.figmaDesignSync.domain.model.artifact.OfficialFigmaArtifactContract
import me.tatarka.inject.annotations.Inject

/** Validates that downloaded artifacts share one official main-branch identity. */
@Inject
class OfficialFigmaArtifactContractValidator {
    fun validate(
        contract: OfficialFigmaArtifactContract,
        expectedGitSha: String? = null
    ): Result {
        val model = contract.model
        require(model.branch == MAIN_BRANCH) {
            "Official Figma artifacts require model branch 'main'; found '${model.branch}'."
        }
        require(model.gitSha.isNotBlank() && model.modelHash.isNotBlank()) {
            "design-model.json requires non-empty gitSha and modelHash values."
        }
        expectedGitSha?.takeIf(String::isNotBlank)?.let { expected ->
            requireEqual(model.gitSha, expected, "TeamCity revision")
        }

        val scope = contract.scope
        requireEqual(scope.scope, FULL_VERIFICATION, "Figma change scope")
        requireEqual(scope.gitSha, model.gitSha, "Scope gitSha")
        requireEqual(scope.modelHash, model.modelHash, "Scope modelHash")

        val visualManifests = contract.manifests.filter { it.fullVisualSync }
        val metadataManifests = contract.manifests.filter { it.writeMetadata }
        require(visualManifests.size == 1 && metadataManifests.size == 1) {
            "Expected one full visual and one metadata manifest; found " +
                "${visualManifests.size} visual and ${metadataManifests.size} metadata."
        }

        val visual = visualManifests.single()
        val metadata = metadataManifests.single()
        validateManifest("visual", visual, model)
        validateManifest("metadata", metadata, model)
        requireEqual(visual.writeMetadata, false, "Visual manifest metadata flag")
        requireEqual(metadata.writeMetadata, true, "Metadata manifest metadata flag")

        validateSharedIdentity(contract, visual, metadata)
        val decision = OfficialFigmaArtifactContract.Decision.fromWireValue(contract.plan.decision)
            ?: throw IllegalArgumentException(
                "Unsupported visual sync decision '${contract.plan.decision}'."
            )

        return Result(
            gitSha = model.gitSha,
            modelHash = model.modelHash,
            decision = decision
        )
    }

    private fun validateManifest(
        name: String,
        manifest: OfficialFigmaArtifactContract.Manifest,
        model: OfficialFigmaArtifactContract.Model
    ) {
        requireEqual(manifest.mode, "official", "$name manifest mode")
        requireEqual(manifest.gitSha, model.gitSha, "$name manifest gitSha")
        requireEqual(manifest.modelHash, model.modelHash, "$name manifest modelHash")
        require(manifest.manifestHash.isNotBlank()) { "$name manifest requires a manifestHash." }
    }

    private fun validateSharedIdentity(
        contract: OfficialFigmaArtifactContract,
        visual: OfficialFigmaArtifactContract.Manifest,
        metadata: OfficialFigmaArtifactContract.Manifest
    ) {
        val scope = contract.scope
        val plan = contract.plan
        requireEqual(metadata.writerHash, visual.writerHash, "Metadata manifest writerHash")
        requireEqual(scope.writerHash, visual.writerHash, "Scope writerHash")
        requireEqual(plan.identity.writerHash, visual.writerHash, "Visual plan writerHash")
        requireEqual(metadata.transportHash, visual.transportHash, "Metadata manifest transportHash")
        requireEqual(scope.transportHash, visual.transportHash, "Scope transportHash")
        requireEqual(plan.identity.transportHash, visual.transportHash, "Visual plan transportHash")
        requireEqual(plan.identity.modelHash, contract.model.modelHash, "Visual plan modelHash")
        requireEqual(plan.manifestHash, visual.manifestHash, "Visual plan manifestHash")
        requireEqual(scope.visualRunnerManifestHash, visual.manifestHash, "Scope visual manifestHash")
        requireEqual(scope.metadataRunnerManifestHash, metadata.manifestHash, "Scope metadata manifestHash")
        requireEqual(scope.visualSyncDecision, plan.decision, "Scope visual decision")
    }

    private fun requireEqual(actual: Any?, expected: Any?, description: String) {
        require(actual == expected) {
            "$description mismatch: expected '$expected', found '$actual'."
        }
    }

    data class Result(
        val gitSha: String,
        val modelHash: String,
        val decision: OfficialFigmaArtifactContract.Decision
    )

    private companion object {
        const val MAIN_BRANCH = "main"
        const val FULL_VERIFICATION = "full-verification"
    }
}
