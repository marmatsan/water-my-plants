package com.marmatsan.figmaDesignSync.teamcityAdapter

/** Composes read-only TeamCity operations with an independently secured run starter. */
class TeamCityCompositeRunClient(
    private val readClient: TeamCityRunClient,
    private val runStarter: TeamCityRunStarter
) : TeamCityRunClient {
    override fun listRuns(
        buildTypeId: String,
        branch: String,
        status: String,
        limit: Int
    ): List<TeamCityRun> = readClient.listRuns(buildTypeId, branch, status, limit)

    override fun startRun(buildTypeId: String, branch: String): TeamCityRun =
        runStarter.startRun(buildTypeId, branch)

    override fun watchRun(
        buildId: Long,
        pollIntervalSeconds: Int,
        timeoutMinutes: Int
    ): TeamCityRun = readClient.watchRun(buildId, pollIntervalSeconds, timeoutMinutes)

    override fun readRun(buildId: Long): TeamCityRun = readClient.readRun(buildId)
}
