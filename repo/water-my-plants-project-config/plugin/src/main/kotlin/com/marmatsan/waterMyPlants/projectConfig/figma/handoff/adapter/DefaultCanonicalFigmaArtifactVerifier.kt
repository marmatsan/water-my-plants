package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.adapter

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.CanonicalFigmaArtifactContract
import com.marmatsan.figmaDocumentationSync.domain.service.artifact.CanonicalFigmaArtifactContractValidator
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model.ValidatedCanonicalFigmaArtifact
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port.CanonicalFigmaArtifactVerifier

internal class DefaultCanonicalFigmaArtifactVerifier(
    private val validator: CanonicalFigmaArtifactContractValidator = CanonicalFigmaArtifactContractValidator(),
) : CanonicalFigmaArtifactVerifier {
    override fun verify(
        contract: CanonicalFigmaArtifactContract,
        expectedGitSha: String?,
    ): ValidatedCanonicalFigmaArtifact {
        val result =
            validator.validate(
                contract,
                expectedGitSha,
            )
        return ValidatedCanonicalFigmaArtifact(
            gitSha = result.gitSha,
            modelHash = result.modelHash,
            decision = result.decision,
        )
    }
}
