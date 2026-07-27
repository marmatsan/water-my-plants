package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.adapter

import com.marmatsan.figmaDocumentationSync.data.figma.artifact.CanonicalFigmaArtifactSetReader
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.model.CanonicalFigmaArtifactSet
import com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port.CanonicalFigmaArtifactSetSource
import java.io.File

internal class DefaultCanonicalFigmaArtifactSetSource(
    private val reader: CanonicalFigmaArtifactSetReader = CanonicalFigmaArtifactSetReader(),
) : CanonicalFigmaArtifactSetSource {
    override fun read(
        artifactDirectory: File,
    ): CanonicalFigmaArtifactSet {
        val result = reader.read(artifactDirectory.absolutePath)
        return CanonicalFigmaArtifactSet(
            artifactDirectory = result.artifactDirectory,
            planPath = result.planPath,
            visualManifestPath = result.visualManifestPath,
            metadataManifestPath = result.metadataManifestPath,
            contract = result.contract,
        )
    }
}
