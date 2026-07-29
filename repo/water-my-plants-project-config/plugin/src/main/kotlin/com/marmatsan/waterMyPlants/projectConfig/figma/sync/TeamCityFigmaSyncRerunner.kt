package com.marmatsan.waterMyPlants.projectConfig.figma.sync

import com.github.michaelbull.result.getOrElse
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRun
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRunClient
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRunStartError

/** Coordinates one idempotent rerun of the canonical TeamCity Figma Sync pipeline. */
class TeamCityFigmaSyncRerunner(
    private val teamCityClient: TeamCityRunClient,
    private val buildTypeId: String = "WaterMyPlants_WaterMyPlantsFigmaSync",
    private val branch: String = "main"
) {
    /** Validates access, reuses an active run, or queues exactly one run according to [request]. */
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
            val run =
                if (request.waitForCompletion) {
                    waitForSuccess(
                        run = activeRun,
                        request = request
                    )
                } else {
                    activeRun
                }
            return result(
                run = run,
                state = run.state,
                reused = true
            )
        }

        val queuedRun =
            teamCityClient
                .startRun(
                    buildTypeId = buildTypeId,
                    branch = branch
                ).getOrElse { startError ->
                    findActiveRun() ?: throw IllegalStateException(startError.operatorMessage())
                }
        val run =
            if (request.waitForCompletion) {
                waitForSuccess(
                    run = queuedRun,
                    request = request
                )
            } else {
                queuedRun
            }
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
            teamCityClient
                .listRuns(
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
        val finished =
            try {
                teamCityClient.watchRun(
                    buildId = run.id,
                    pollIntervalSeconds = request.pollIntervalSeconds,
                    timeoutMinutes = request.timeoutMinutes
                )
            } catch (
                watchError: RuntimeException
            ) {
                val current =
                    runCatching {
                        teamCityClient.readRun(
                            buildId = run.id
                        )
                    }.getOrElse { throw watchError }
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

    /**
     * Operator controls for a bounded, idempotent Figma Sync rerun.
     *
     * @property validateOnly verifies credentials and read access without queueing a run.
     * @property waitForCompletion waits for and validates a successful terminal status.
     * @property pollIntervalSeconds bounded interval passed to TeamCity polling.
     * @property timeoutMinutes bounded maximum wait for run completion.
     */
    data class Request(
        val validateOnly: Boolean = false,
        val waitForCompletion: Boolean = false,
        val pollIntervalSeconds: Int = 10,
        val timeoutMinutes: Int = 60
    )
}

private fun TeamCityRunStartError.operatorMessage(): String =
    when (this) {
        is TeamCityRunStartError.RequestRejected -> {
            "TeamCity REST queue request failed with HTTP $statusCode: $responseBody"
        }

        is TeamCityRunStartError.CommandFailed -> {
            "TeamCity CLI queue command failed with exit code $exitCode: $detail"
        }

        TeamCityRunStartError.InvalidResponse -> {
            "TeamCity returned an invalid queue response."
        }

        is TeamCityRunStartError.Unavailable -> {
            "TeamCity queue transport is unavailable: $detail"
        }
    }
