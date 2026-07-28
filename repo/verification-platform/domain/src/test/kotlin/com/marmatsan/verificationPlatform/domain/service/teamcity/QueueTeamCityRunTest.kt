package com.marmatsan.verificationPlatform.domain.service.teamcity

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.marmatsan.unitTest.dsl.given
import com.marmatsan.verificationPlatform.domain.model.teamcity.QueueTeamCityRunError
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityQueuedRun
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityRunRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class QueueTeamCityRunTest :
    FunSpec(
        {
            test("queues a validated build type and branch") {
                val expected =
                    TeamCityQueuedRun(
                        id = 1800,
                        state = "queued",
                        branch = "main",
                        webUrl = null,
                    )
                given {
                    QueueTeamCityRun { Ok(expected) }
                }.whenever { service ->
                    service.execute(
                        TeamCityRunRequest(
                            buildTypeId = "WaterMyPlants_InfrastructureHealth",
                            branch = "main",
                        ),
                    )
                }.then { result ->
                    result shouldBe Ok(expected)
                }
            }

            test("rejects shell syntax in the build type id") {
                given {
                    QueueTeamCityRun {
                        error("The queue must not receive an invalid request")
                    }
                }.whenever { service ->
                    service.execute(
                        TeamCityRunRequest(
                            buildTypeId = "Health && publish",
                            branch = "main",
                        ),
                    )
                }.then { result ->
                    result shouldBe Err(QueueTeamCityRunError.UnsupportedBuildType)
                }
            }

            test("rejects shell syntax in the branch") {
                given {
                    QueueTeamCityRun {
                        error("The queue must not receive an invalid request")
                    }
                }.whenever { service ->
                    service.execute(
                        TeamCityRunRequest(
                            buildTypeId = "InfrastructureHealth",
                            branch = "main && publish",
                        ),
                    )
                }.then { result ->
                    result shouldBe Err(QueueTeamCityRunError.UnsupportedBranch)
                }
            }
        },
    )
