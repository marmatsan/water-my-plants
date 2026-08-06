package com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.adapter

import com.marmatsan.figmaDocumentationSync.teamcity.operations.handoff.port.TeamCityFigmaHandoffSummaryWriter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.File

/** Writes the handoff JSON beside the canonical artifact set. */
internal class JsonTeamCityFigmaHandoffSummaryWriter : TeamCityFigmaHandoffSummaryWriter {
    /** Writes pretty, deterministic [summary] JSON into [artifactDirectory]. */
    override fun write(
        artifactDirectory: File,
        summary: JsonObject
    ): File {
        val summaryFile = artifactDirectory.resolve("figma-sync-handoff.json")
        summaryFile.writeText(
            prettyJson.encodeToString(
                JsonObject.serializer(),
                summary
            ) + System.lineSeparator()
        )
        return summaryFile
    }

    private companion object {
        val prettyJson = Json { prettyPrint = true }
    }
}
