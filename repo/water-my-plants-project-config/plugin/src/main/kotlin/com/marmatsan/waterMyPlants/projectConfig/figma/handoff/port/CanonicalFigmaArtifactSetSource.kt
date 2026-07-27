package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port

import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model.CanonicalFigmaArtifactSet
import java.io.File

/** Supplies the canonical artifact data required by the handoff use case. */
internal fun interface CanonicalFigmaArtifactSetSource {
    /** Discovers and parses the canonical set rooted at [artifactDirectory]. */
    fun read(
        artifactDirectory: File,
    ): CanonicalFigmaArtifactSet
}
