package com.marmatsan.figmaDocumentationSync.projectConfig

import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRun
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRunClient

/** Coordinates one idempotent rerun of the official TeamCity Figma Sync pipeline. */
class TeamCityFigmaSyncRerunner(
    private val teamCityClient: TeamCityRunClient,
    private val buildTypeId: String = "WaterMyPlants_WaterMyPlantsFigmaSync",
    private val branch: String = "main"
) {
    fun rerun(
        request: Request = Request()
    ): TeamCityFigmaSyncRerunResult {
        require(request.pollIntervalSeconds in 1..300) {
            "TeamCity polling interval must be between 1 and 300 seconds."
        }
        require(request.timeoutMinutes in 1..1440) {
            "TeamCity timeout must be between 1 and 1440 minutes."
        }
        val activeRun = findActiveRun()
        if (request.validateOnly) {
            return result(
                run = null,
                state = "Validated",
                reused = false
            )
        }
        if (activeRun != null) {
            val run = if (request.waitForCompletion) waitForSuccess(
                run = activeRun,
                request = request
            ) else activeRun
            return result(
                run = run,
                state = run.state,
                reused = true
            )
        }

        val queuedRun = try {
            teamCityClient.startRun(
                buildTypeId,
                branch
            )
        } catch (
            startError: RuntimeException
        ) {
            findActiveRun() ?: throw startError
        }
        val run = if (request.waitForCompletion) waitForSuccess(
            run = queuedRun,
            request = request
        ) else queuedRun
        return result(
            run = run,
            state = run.state,
            reused = false
        )
    }

    private fun findActiveRun(): TeamCityRun? =
        listOf(
            "running",
            "queued"
        ).firstNotNullOfOrNull { status ->
            teamCityClient.listRuns(
                buildTypeId = buildTypeId,
                branch = branch,
                status = status,
                limit = 1
            ).firstOrNull()
        }

    private fun waitForSuccess(
        run: TeamCityRun,
        request: Request
    ): TeamCityRun {
        val finished = try {
            teamCityClient.watchRun(
                buildId = run.id,
                pollIntervalSeconds = request.pollIntervalSeconds,
                timeoutMinutes = request.timeoutMinutes
            )
        } catch (
            watchError: RuntimeException
        ) {
            val current = runCatching { teamCityClient.readRun(run.id) }
                .getOrElse { throw watchError }
            if (current.state == "finished" && current.status != "SUCCESS") {
                throw failedRun(
                    run = current
                )
            }
            throw watchError
        }
        if (finished.status != "SUCCESS") {
            throw failedRun(
                run = finished
            )
        }
        return finished
    }

    private fun failedRun(
        run: TeamCityRun
    ): IllegalStateException =
        IllegalStateException(
            "TeamCity Figma Sync run ${run.id} finished with status '${run.status}': " +
                (run.statusText ?: "no status text")
        )

    private fun result(
        run: TeamCityRun?,
        state: String,
        reused: Boolean
    ): TeamCityFigmaSyncRerunResult =
        TeamCityFigmaSyncRerunResult(
            runId = run?.id,
            webUrl = run?.webUrl,
            branch = branch,
            state = state,
            reused = reused
        )

    data class Request(
        val validateOnly: Boolean = false,
        val waitForCompletion: Boolean = false,
        val pollIntervalSeconds: Int = 10,
        val timeoutMinutes: Int = 60
    )
}
