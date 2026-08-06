package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port

import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.model.CanonicalFigmaArtifactSet
import java.io.File

/** Supplies the canonical artifact data required by the handoff use case. */
internal fun interface CanonicalFigmaArtifactSetSource {
    /** Discovers and parses the canonical set rooted at [artifactDirectory]. */
    fun read(
        artifactDirectory: File
    ): CanonicalFigmaArtifactSet
}
