package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port

import kotlinx.serialization.json.JsonObject
import java.io.File

/** Persists the generated handoff summary. */
internal fun interface TeamCityFigmaHandoffSummaryWriter {
    /** Writes [summary] beside the canonical set in [artifactDirectory]. */
    fun write(
        artifactDirectory: File,
        summary: JsonObject
    ): File
}
