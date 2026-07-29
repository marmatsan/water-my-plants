package com.marmatsan.figmaDocumentationSync.teamcityAdapter

/** Typed operations required to validate, queue, and wait for TeamCity runs. */
interface TeamCityRunClient : TeamCityRunStarter {
    /** Lists up to [limit] runs matching build type, branch, and status. */
    fun listRuns(
        buildTypeId: String,
        branch: String,
        status: String,
        limit: Int = 1
    ): List<TeamCityRun>

    /** Polls [buildId] until completion or the bounded timeout is reached. */
    fun watchRun(
        buildId: Long,
        pollIntervalSeconds: Int,
        timeoutMinutes: Int
    ): TeamCityRun

    /** Reads the current typed state of [buildId]. */
    fun readRun(
        buildId: Long
    ): TeamCityRun
}
