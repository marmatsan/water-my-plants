package com.marmatsan.waterMyPlants.projectConfig.figma.handoff.port

import kotlinx.serialization.json.JsonObject
import java.io.File

/** Persists the generated handoff summary. */
internal fun interface TeamCityFigmaHandoffSummaryWriter {
    fun write(
        artifactDirectory: File,
        summary: JsonObject,
    ): File
}
