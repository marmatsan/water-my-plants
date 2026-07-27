package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.CanonicalFigmaArtifactContract

/**
 * Canonical identity accepted after cross-artifact validation.
 *
 * @property gitSha repository revision shared by every validated artifact.
 * @property modelHash canonical design-model hash shared by every artifact.
 * @property decision validated visual synchronization decision.
 */
internal data class ValidatedCanonicalFigmaArtifact(
    val gitSha: String,
    val modelHash: String,
    val decision: CanonicalFigmaArtifactContract.Decision,
)
