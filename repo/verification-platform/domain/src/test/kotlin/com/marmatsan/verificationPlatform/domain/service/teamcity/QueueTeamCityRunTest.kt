package com.marmatsan.verificationPlatform.domain.service.teamcity

import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityQueuedRun
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityRunRequest
import io.kotest.assertions.throwables.shouldThrow
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
                val service = QueueTeamCityRun { expected }

                service.execute(
                    TeamCityRunRequest(
                        buildTypeId = "WaterMyPlants_InfrastructureHealth",
                        branch = "main",
                    ),
                ) shouldBe expected
            }

            test("rejects shell syntax in the build type id") {
                val service =
                    QueueTeamCityRun {
                        TeamCityQueuedRun(
                            id = 1800,
                            state = "queued",
                            branch = "main",
                            webUrl = null,
                        )
                    }

                val failure =
                    shouldThrow<IllegalArgumentException> {
                        service.execute(
                            TeamCityRunRequest(
                                buildTypeId = "Health && publish",
                                branch = "main",
                            ),
                        )
                    }

                failure.message shouldBe "TeamCity build type id contains unsupported characters."
            }
        },
    )
