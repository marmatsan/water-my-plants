package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.CanonicalFigmaArtifactContract

internal data class ValidatedCanonicalFigmaArtifact(
    val gitSha: String,
    val modelHash: String,
    val decision: CanonicalFigmaArtifactContract.Decision,
)
