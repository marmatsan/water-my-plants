package com.marmatsan.verificationPlatform.domain.service

import com.marmatsan.verificationPlatform.domain.model.TeamCityQueuedRun
import com.marmatsan.verificationPlatform.domain.model.TeamCityRunRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class QueueTeamCityRunTest : FunSpec(
    {
    test("queues a validated build type and branch") {
        val expected = TeamCityQueuedRun(
            1800,
            "queued",
            "main",
            null
        )
        val service = QueueTeamCityRun { expected }

        service.execute(
            TeamCityRunRequest(
                "WaterMyPlants_InfrastructureHealth",
                "main"
            )
        ) shouldBe expected
    }

    test("rejects shell syntax in the build type id") {
        val service = QueueTeamCityRun { TeamCityQueuedRun(
            1800,
            "queued",
            "main",
            null
        ) }

        val failure = shouldThrow<IllegalArgumentException> {
            service.execute(
                TeamCityRunRequest(
                    "Health && publish",
                    "main"
                )
            )
        }

        failure.message shouldBe "TeamCity build type id contains unsupported characters."
    }
}
)
