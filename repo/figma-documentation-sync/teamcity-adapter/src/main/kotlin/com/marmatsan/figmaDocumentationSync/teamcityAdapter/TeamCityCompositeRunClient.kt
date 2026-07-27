package com.marmatsan.figmaDocumentationSync.teamcityAdapter

/** Composes read-only TeamCity operations with an independently secured run starter. */
class TeamCityCompositeRunClient(
    private val readClient: TeamCityRunClient,
    private val runStarter: TeamCityRunStarter,
) : TeamCityRunClient {
    /** Delegates read-only listing to the configured read client. */
    override fun listRuns(
        buildTypeId: String,
        branch: String,
        status: String,
        limit: Int,
    ): List<TeamCityRun> =
        readClient.listRuns(
            buildTypeId = buildTypeId,
            branch = branch,
            status = status,
            limit = limit,
        )

    /** Delegates queueing to the independently secured mutating boundary. */
    override fun startRun(
        buildTypeId: String,
        branch: String,
    ): TeamCityRun =
        runStarter.startRun(
            buildTypeId = buildTypeId,
            branch = branch,
        )

    /** Delegates bounded polling to the configured read client. */
    override fun watchRun(
        buildId: Long,
        pollIntervalSeconds: Int,
        timeoutMinutes: Int,
    ): TeamCityRun =
        readClient.watchRun(
            buildId = buildId,
            pollIntervalSeconds = pollIntervalSeconds,
            timeoutMinutes = timeoutMinutes,
        )

    /** Delegates current-state lookup to the configured read client. */
    override fun readRun(
        buildId: Long,
    ): TeamCityRun =
        readClient.readRun(
            buildId = buildId,
        )
}
