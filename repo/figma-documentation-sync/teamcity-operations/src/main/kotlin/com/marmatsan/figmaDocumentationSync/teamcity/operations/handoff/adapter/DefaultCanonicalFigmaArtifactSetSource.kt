package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.adapter

import com.marmatsan.figmaDocumentationSync.data.figma.artifact.CanonicalFigmaArtifactSetReader
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.model.CanonicalFigmaArtifactSet
import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port.CanonicalFigmaArtifactSetSource
import java.io.File

/** Adapts reusable artifact discovery to the project-config handoff model. */
internal class DefaultCanonicalFigmaArtifactSetSource(
    private val reader: CanonicalFigmaArtifactSetReader = CanonicalFigmaArtifactSetReader()
) : CanonicalFigmaArtifactSetSource {
    /** Discovers and projects the canonical set rooted at [artifactDirectory]. */
    override fun read(
        artifactDirectory: File
    ): CanonicalFigmaArtifactSet {
        val result = reader.read(artifactDirectory.absolutePath)
        return CanonicalFigmaArtifactSet(
            artifactDirectory = result.artifactDirectory,
            planPath = result.planPath,
            visualManifestPath = result.visualManifestPath,
            metadataManifestPath = result.metadataManifestPath,
            contract = result.contract
        )
    }
}
