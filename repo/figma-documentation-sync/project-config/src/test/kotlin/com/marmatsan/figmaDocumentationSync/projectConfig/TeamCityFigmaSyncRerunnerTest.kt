package com.marmatsan.figmaDocumentationSync.projectConfig

import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRun
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRunClient
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class TeamCityFigmaSyncRerunnerTest : FunSpec(
    {
    test("validates authenticated access without queueing a run") {
        val requestedStatuses = mutableListOf<String>()
        val client = object : TeamCityRunClient {
            override fun listRuns(
                buildTypeId: String,
                branch: String,
                status: String,
                limit: Int
            ): List<TeamCityRun> {
                requestedStatuses += status
                return emptyList()
            }

            override fun startRun(
                buildTypeId: String,
                branch: String
            ): TeamCityRun =
                error("Validation must not queue a run")

            override fun watchRun(
                buildId: Long,
                pollIntervalSeconds: Int,
                timeoutMinutes: Int
            ): TeamCityRun = error("Validation must not wait")

            override fun readRun(
                buildId: Long
            ): TeamCityRun = error("Validation must not read a run")
        }

        TeamCityFigmaSyncRerunner(
            teamCityClient = client
        ).rerun(
            request = TeamCityFigmaSyncRerunner.Request(
                validateOnly = true
            )
        ) shouldBe TeamCityFigmaSyncRerunResult(
            runId = null,
            webUrl = null,
            branch = "main",
            state = "Validated",
            reused = false
        )
        requestedStatuses shouldBe listOf(
            "running",
            "queued"
        )
    }

    test("reuses and waits for an active run") {
        val active = teamCityRun(
            id = 1580,
            state = "running"
        )
        val finished = teamCityRun(
            id = 1580,
            state = "finished",
            status = "SUCCESS"
        )
        val client = object : TeamCityRunClient {
            override fun listRuns(
                buildTypeId: String,
                branch: String,
                status: String,
                limit: Int
            ): List<TeamCityRun> = if (status == "running") listOf(active) else emptyList()

            override fun startRun(
                buildTypeId: String,
                branch: String
            ): TeamCityRun =
                error("An active run must be reused")

            override fun watchRun(
                buildId: Long,
                pollIntervalSeconds: Int,
                timeoutMinutes: Int
            ): TeamCityRun {
                buildId shouldBe 1580
                pollIntervalSeconds shouldBe 5
                timeoutMinutes shouldBe 30
                return finished
            }

            override fun readRun(
                buildId: Long
            ): TeamCityRun = error("Watch succeeds")
        }

        TeamCityFigmaSyncRerunner(
            teamCityClient = client
        ).rerun(
            request = TeamCityFigmaSyncRerunner.Request(
                waitForCompletion = true,
                pollIntervalSeconds = 5,
                timeoutMinutes = 30
            )
        ) shouldBe TeamCityFigmaSyncRerunResult(
            runId = 1580,
            webUrl = "https://teamcity.example/build/1580",
            branch = "main",
            state = "finished",
            reused = true
        )
    }

    test("accepts a concurrent run when queueing reports an uncertain failure") {
        var lookupCount = 0
        val concurrent = teamCityRun(
            id = 1581,
            state = "queued"
        )
        val client = object : TeamCityRunClient {
            override fun listRuns(
                buildTypeId: String,
                branch: String,
                status: String,
                limit: Int
            ): List<TeamCityRun> {
                lookupCount += 1
                return if (lookupCount > 2 && status == "queued") listOf(concurrent) else emptyList()
            }

            override fun startRun(
                buildTypeId: String,
                branch: String
            ): TeamCityRun =
                throw IllegalArgumentException("uncertain start")

            override fun watchRun(
                buildId: Long,
                pollIntervalSeconds: Int,
                timeoutMinutes: Int
            ): TeamCityRun = error("Waiting was not requested")

            override fun readRun(
                buildId: Long
            ): TeamCityRun = error("Reading was not requested")
        }

        TeamCityFigmaSyncRerunner(
            teamCityClient = client
        ).rerun() shouldBe TeamCityFigmaSyncRerunResult(
            runId = 1581,
            webUrl = "https://teamcity.example/build/1581",
            branch = "main",
            state = "queued",
            reused = false
        )
    }

    test("reports a failed run after watch returns a non successful result") {
        val queued = teamCityRun(
            id = 1582,
            state = "queued"
        )
        val failed = teamCityRun(
            id = 1582,
            state = "finished",
            status = "FAILURE",
            statusText = "Figma metadata is stale"
        )
        val client = object : TeamCityRunClient {
            override fun listRuns(
                buildTypeId: String,
                branch: String,
                status: String,
                limit: Int
            ): List<TeamCityRun> = emptyList()

            override fun startRun(
                buildTypeId: String,
                branch: String
            ): TeamCityRun = queued

            override fun watchRun(
                buildId: Long,
                pollIntervalSeconds: Int,
                timeoutMinutes: Int
            ): TeamCityRun = failed

            override fun readRun(
                buildId: Long
            ): TeamCityRun = error("Watch returned a result")
        }

        val exception = shouldThrow<IllegalStateException> {
            TeamCityFigmaSyncRerunner(
                teamCityClient = client
            ).rerun(
                request = TeamCityFigmaSyncRerunner.Request(
                    waitForCompletion = true
                )
            )
        }

        exception.message shouldBe
            "TeamCity Figma Sync run 1582 finished with status 'FAILURE': Figma metadata is stale"
    }
}
)

private fun teamCityRun(
    id: Long,
    state: String,
    status: String? = null,
    statusText: String? = null
): TeamCityRun =
    TeamCityRun(
        id = id,
        state = state,
        status = status,
        statusText = statusText,
        branchName = "main",
        webUrl = "https://teamcity.example/build/$id"
    )
