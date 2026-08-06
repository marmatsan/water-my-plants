package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.CanonicalFigmaArtifactContract
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.model.ValidatedCanonicalFigmaArtifact

/** Verifies the canonical identity without exposing a concrete validator to the use case. */
internal fun interface CanonicalFigmaArtifactVerifier {
    /** Validates [contract] and optionally requires [expectedGitSha]. */
    fun verify(
        contract: CanonicalFigmaArtifactContract,
        expectedGitSha: String?
    ): ValidatedCanonicalFigmaArtifact
}
