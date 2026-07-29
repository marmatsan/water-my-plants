package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model

import com.marmatsan.figmaDocumentationSync.domain.model.artifact.CanonicalFigmaArtifactContract
import java.nio.file.Path

/**
 * Paths and parsed identity discovered in one canonical Figma artifact directory.
 *
 * @property artifactDirectory normalized root of the discovered artifact set.
 * @property planPath required visual synchronization plan.
 * @property visualManifestPath optional visual runner manifest discovered in the set.
 * @property metadataManifestPath optional metadata runner manifest discovered in the set.
 * @property contract parsed cross-artifact identity contract.
 */
internal data class CanonicalFigmaArtifactSet(
    val artifactDirectory: Path,
    val planPath: Path,
    val visualManifestPath: Path?,
    val metadataManifestPath: Path?,
    val contract: CanonicalFigmaArtifactContract
)
