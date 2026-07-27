package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port

import java.io.File

/** Extracts one artifact archive into a caller-owned directory. */
internal fun interface ArtifactArchiveExtractor {
    fun extract(
        archive: File,
        destination: File,
    )
}
