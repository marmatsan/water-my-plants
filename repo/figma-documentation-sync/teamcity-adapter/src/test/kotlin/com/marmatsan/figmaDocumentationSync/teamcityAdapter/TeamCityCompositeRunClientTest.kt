package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class TeamCityCompositeRunClientTest : FunSpec(
    {
    test("uses the independent starter for mutating requests") {
        val readClient = object : TeamCityRunClient {
            override fun listRuns(
                buildTypeId: String,
                branch: String,
                status: String,
                limit: Int
            ): List<TeamCityRun> = emptyList()

            override fun startRun(
                buildTypeId: String,
                branch: String
            ): TeamCityRun =
                error("The read client must not queue a run")

            override fun watchRun(
                buildId: Long,
                pollIntervalSeconds: Int,
                timeoutMinutes: Int
            ): TeamCityRun = error("Not used")

            override fun readRun(
                buildId: Long
            ): TeamCityRun = error("Not used")
        }
        val queued = TeamCityRun(
            id = 1681,
            state = "queued",
            status = null,
            statusText = null,
            branchName = "main",
            webUrl = "https://teamcity.example/build/1681"
        )
        val client = TeamCityCompositeRunClient(
            readClient = readClient,
            runStarter = TeamCityRunStarter { buildTypeId, branch ->
                buildTypeId shouldBe "WaterMyPlants_WaterMyPlantsFigmaSync"
                branch shouldBe "main"
                queued
            }
        )

        client.startRun(
            buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync",
            branch = "main"
        ) shouldBe queued
    }
}
)
