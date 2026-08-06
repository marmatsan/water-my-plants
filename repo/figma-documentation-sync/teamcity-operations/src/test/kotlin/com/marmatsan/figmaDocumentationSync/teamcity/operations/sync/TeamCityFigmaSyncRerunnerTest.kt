package com.marmatsan.figmaDocumentationSync.teamcity.operations.sync

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRun
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRunClient
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRunStartError
import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class TeamCityFigmaSyncRerunnerTest :
    FunSpec(
        {
            test("validates authenticated access without queueing a run") {
                given {
                    val requestedStatuses = mutableListOf<String>()
                    val client =
                        teamCityRunClient(
                            listRunsBehavior = { _, _, status, _ ->
                                requestedStatuses += status
                                emptyList()
                            }
                        )
                    client to requestedStatuses
                }.whenever { (client, requestedStatuses) ->
                    teamCityFigmaSyncRerunner(
                        client = client
                    ).rerun(
                        request =
                            TeamCityFigmaSyncRerunner.Request(
                                validateOnly = true
                            )
                    ) to requestedStatuses
                }.then { (result, requestedStatuses) ->
                    result shouldBe
                        TeamCityFigmaSyncRerunResult(
                            runId = null,
                            webUrl = null,
                            branch = "main",
                            state = "Validated",
                            reused = false
                        )
                    requestedStatuses shouldBe
                        listOf(
                            "running",
                            "queued"
                        )
                }
            }

            test("reuses and waits for an active run") {
                given {
                    val active =
                        teamCityRun(
                            id = 1580,
                            state = "running"
                        )
                    val finished =
                        teamCityRun(
                            id = 1580,
                            state = "finished",
                            status = "SUCCESS"
                        )
                    teamCityRunClient(
                        listRunsBehavior = { _, _, status, _ ->
                            if (status == "running") listOf(active) else emptyList()
                        },
                        watchRunBehavior = { buildId, pollIntervalSeconds, timeoutMinutes ->
                            buildId shouldBe 1580
                            pollIntervalSeconds shouldBe 5
                            timeoutMinutes shouldBe 30
                            finished
                        }
                    )
                }.whenever { client ->
                    teamCityFigmaSyncRerunner(
                        client = client
                    ).rerun(
                        request =
                            TeamCityFigmaSyncRerunner.Request(
                                waitForCompletion = true,
                                pollIntervalSeconds = 5,
                                timeoutMinutes = 30
                            )
                    )
                }.then { result ->
                    result shouldBe
                        TeamCityFigmaSyncRerunResult(
                            runId = 1580,
                            webUrl = "https://teamcity.example/build/1580",
                            branch = "main",
                            state = "finished",
                            reused = true
                        )
                }
            }

            test("accepts a concurrent run when queueing reports an uncertain failure") {
                given {
                    var lookupCount = 0
                    val concurrent =
                        teamCityRun(
                            id = 1581,
                            state = "queued"
                        )
                    teamCityRunClient(
                        listRunsBehavior = { _, _, status, _ ->
                            lookupCount += 1
                            if (lookupCount > 2 && status == "queued") listOf(concurrent) else emptyList()
                        },
                        startRunBehavior = { _, _ ->
                            Err(TeamCityRunStartError.Unavailable("uncertain start"))
                        }
                    )
                }.whenever { client ->
                    teamCityFigmaSyncRerunner(
                        client = client
                    ).rerun()
                }.then { result ->
                    result shouldBe
                        TeamCityFigmaSyncRerunResult(
                            runId = 1581,
                            webUrl = "https://teamcity.example/build/1581",
                            branch = "main",
                            state = "queued",
                            reused = false
                        )
                }
            }

            test("reports the typed queue failure when no concurrent run exists") {
                given {
                    teamCityRunClient(
                        startRunBehavior = { _, _ ->
                            Err(
                                TeamCityRunStartError.RequestRejected(
                                    statusCode = 403,
                                    responseBody = "forbidden"
                                )
                            )
                        }
                    )
                }.whenever { client ->
                    shouldThrow<IllegalStateException> {
                        teamCityFigmaSyncRerunner(
                            client = client
                        ).rerun()
                    }
                }.then { exception ->
                    exception.message shouldBe
                        "TeamCity REST queue request failed with HTTP 403: forbidden"
                }
            }

            test("reports a failed run after watch returns a non successful result") {
                given {
                    val queued =
                        teamCityRun(
                            id = 1582,
                            state = "queued"
                        )
                    val failed =
                        teamCityRun(
                            id = 1582,
                            state = "finished",
                            status = "FAILURE",
                            statusText = "Figma metadata is stale"
                        )
                    teamCityRunClient(
                        startRunBehavior = { _, _ -> Ok(queued) },
                        watchRunBehavior = { _, _, _ -> failed }
                    )
                }.whenever { client ->
                    shouldThrow<IllegalStateException> {
                        teamCityFigmaSyncRerunner(
                            client = client
                        ).rerun(
                            request =
                                TeamCityFigmaSyncRerunner.Request(
                                    waitForCompletion = true
                                )
                        )
                    }
                }.then { exception ->
                    exception.message shouldBe
                        "TeamCity Figma Sync run 1582 finished with status 'FAILURE': Figma metadata is stale"
                }
            }
        }
    )

private fun teamCityFigmaSyncRerunner(
    client: TeamCityRunClient
): TeamCityFigmaSyncRerunner =
    TeamCityFigmaSyncRerunner(
        teamCityClient = client,
        buildTypeId = "Example_FigmaSync",
        branch = "main"
    )

/** Creates a focused TeamCity port double whose unsupported operations fail fast. */
private fun teamCityRunClient(
    listRunsBehavior: (String, String, String, Int) -> List<TeamCityRun> = { _, _, _, _ -> emptyList() },
    startRunBehavior: (String, String) -> Result<TeamCityRun, TeamCityRunStartError> = { _, _ ->
        error("Queueing was not expected")
    },
    watchRunBehavior: (Long, Int, Int) -> TeamCityRun = { _, _, _ -> error("Waiting was not expected") },
    readRunBehavior: (Long) -> TeamCityRun = { error("Reading was not expected") }
): TeamCityRunClient =
    object : TeamCityRunClient {
        override fun listRuns(
            buildTypeId: String,
            branch: String,
            status: String,
            limit: Int
        ): List<TeamCityRun> =
            listRunsBehavior(
                buildTypeId,
                branch,
                status,
                limit
            )

        override fun startRun(
            buildTypeId: String,
            branch: String
        ): Result<TeamCityRun, TeamCityRunStartError> =
            startRunBehavior(
                buildTypeId,
                branch
            )

        override fun watchRun(
            buildId: Long,
            pollIntervalSeconds: Int,
            timeoutMinutes: Int
        ): TeamCityRun =
            watchRunBehavior(
                buildId,
                pollIntervalSeconds,
                timeoutMinutes
            )

        override fun readRun(
            buildId: Long
        ): TeamCityRun = readRunBehavior(buildId)
    }

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
