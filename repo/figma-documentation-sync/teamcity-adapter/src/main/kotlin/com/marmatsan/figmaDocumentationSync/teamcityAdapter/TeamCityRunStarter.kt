package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import com.github.michaelbull.result.Result

/** Mutating boundary for queueing one TeamCity run. */
fun interface TeamCityRunStarter {
    /** Queues [buildTypeId] on [branch] or returns an expected provider failure. */
    fun startRun(
        buildTypeId: String,
        branch: String,
    ): Result<TeamCityRun, TeamCityRunStartError>
}
