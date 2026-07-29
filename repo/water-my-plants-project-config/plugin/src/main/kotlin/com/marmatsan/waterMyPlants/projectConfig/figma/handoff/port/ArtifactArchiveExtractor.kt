package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port

import java.io.File

/** Extracts one artifact archive into a caller-owned directory. */
internal fun interface ArtifactArchiveExtractor {
    /** Extracts [archive] beneath [destination] without allowing path escape. */
    fun extract(
        archive: File,
        destination: File
    )
}
