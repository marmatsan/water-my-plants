package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class TeamCityCompositeRunClientTest :
    FunSpec(
        {
            test("uses the independent starter for mutating requests") {
                val queued =
                    TeamCityRun(
                        id = 1681,
                        state = "queued",
                        status = null,
                        statusText = null,
                        branchName = "main",
                        webUrl = "https://teamcity.example/build/1681",
                    )
                given {
                    val readClient =
                        object : TeamCityRunClient {
                            override fun listRuns(
                                buildTypeId: String,
                                branch: String,
                                status: String,
                                limit: Int,
                            ): List<TeamCityRun> = emptyList()

                            override fun startRun(
                                buildTypeId: String,
                                branch: String,
                            ): Result<TeamCityRun, TeamCityRunStartError> =
                                error("The read client must not queue a run")

                            override fun watchRun(
                                buildId: Long,
                                pollIntervalSeconds: Int,
                                timeoutMinutes: Int,
                            ): TeamCityRun = error("Not used")

                            override fun readRun(
                                buildId: Long,
                            ): TeamCityRun = error("Not used")
                        }
                    TeamCityCompositeRunClient(
                        readClient = readClient,
                        runStarter =
                            TeamCityRunStarter { buildTypeId, branch ->
                                buildTypeId shouldBe "WaterMyPlants_WaterMyPlantsFigmaSync"
                                branch shouldBe "main"
                                Ok(queued)
                            },
                    )
                }.whenever { client ->
                    client.startRun(
                        buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync",
                        branch = "main",
                    )
                }.then { result ->
                    result shouldBe Ok(queued)
                }
            }
        },
    )
