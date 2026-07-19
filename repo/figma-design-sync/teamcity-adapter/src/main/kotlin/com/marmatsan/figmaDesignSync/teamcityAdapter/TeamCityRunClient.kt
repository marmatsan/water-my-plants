package com.marmatsan.figmaDesignSync.teamcityAdapter

/** Typed operations required to validate, queue, and wait for TeamCity runs. */
interface TeamCityRunClient : TeamCityRunStarter {
    fun listRuns(
        buildTypeId: String,
        branch: String,
        status: String,
        limit: Int = 1
    ): List<TeamCityRun>

    fun watchRun(
        buildId: Long,
        pollIntervalSeconds: Int,
        timeoutMinutes: Int
    ): TeamCityRun

    fun readRun(buildId: Long): TeamCityRun
}
