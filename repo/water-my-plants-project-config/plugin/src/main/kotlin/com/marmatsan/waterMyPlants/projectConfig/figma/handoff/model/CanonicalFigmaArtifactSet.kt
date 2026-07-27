package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.CanonicalFigmaArtifactContract
import java.nio.file.Path

internal data class CanonicalFigmaArtifactSet(
    val artifactDirectory: Path,
    val planPath: Path,
    val visualManifestPath: Path?,
    val metadataManifestPath: Path?,
    val contract: CanonicalFigmaArtifactContract,
)
