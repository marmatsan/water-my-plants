package com.marmatsan.figmaDesignSync.teamcityAdapter

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class TeamCityCliClientTest : FunSpec({
    test("reads build identity through the TeamCity CLI JSON contract") {
        val commands = mutableListOf<List<String>>()
        val client = TeamCityCliClient { command, _ ->
            commands += command
            TeamCityCliClient.CommandResult(
                exitCode = 0,
                output =
                    """
                    {
                      "id": "1573",
                      "state": "finished",
                      "status": "SUCCESS",
                      "branchName": "main",
                      "webUrl": "https://teamcity.example/build/1573",
                      "buildType": { "name": "Generate main design model" }
                    }
                    """.trimIndent(),
                error = ""
            )
        }

        val build = client.readBuild(1573)

        build shouldBe TeamCityBuild(
            id = 1573,
            state = "finished",
            status = "SUCCESS",
            branchName = "main",
            buildTypeName = "Generate main design model",
            webUrl = "https://teamcity.example/build/1573"
        )
        commands.single() shouldBe listOf(
            "teamcity",
            "--no-color",
            "--no-input",
            "run",
            "view",
            "1573",
            "--json"
        )
    }

    test("downloads one build artifact set into the requested directory") {
        val output = Files.createTempDirectory("teamcity-cli-download").resolve("artifacts").toFile()
        val commands = mutableListOf<List<String>>()
        val client = TeamCityCliClient { command, _ ->
            commands += command
            TeamCityCliClient.CommandResult(exitCode = 0, output = "downloaded", error = "")
        }

        client.downloadArtifacts(1573, output)

        output.isDirectory shouldBe true
        commands.single() shouldBe listOf(
            "teamcity",
            "--no-color",
            "--no-input",
            "run",
            "download",
            "1573",
            "--output",
            output.absolutePath
        )
        output.parentFile.deleteRecursively()
    }

    test("lists active runs with the authenticated command environment") {
        val commands = mutableListOf<List<String>>()
        val environments = mutableListOf<Map<String, String?>>()
        val expectedEnvironment = mapOf(
            "TEAMCITY_URL" to "https://teamcity.example",
            "TEAMCITY_TOKEN" to "teamcity-token",
            "TEAMCITY_HEADER_CF_ACCESS_TOKEN" to "cloudflare-token",
            "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID" to null,
            "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET" to null
        )
        val client = TeamCityCliClient(environment = expectedEnvironment) { command, environment ->
            commands += command
            environments += environment
            TeamCityCliClient.CommandResult(
                exitCode = 0,
                output =
                    """
                    {
                      "count": 1,
                      "build": [{
                        "id": 1580,
                        "state": "running",
                        "branchName": "main",
                        "webUrl": "https://teamcity.example/build/1580"
                      }]
                    }
                    """.trimIndent(),
                error = ""
            )
        }

        client.listRuns(
            buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync",
            branch = "main",
            status = "running"
        ) shouldBe listOf(
            TeamCityRun(
                id = 1580,
                state = "running",
                status = null,
                statusText = null,
                branchName = "main",
                webUrl = "https://teamcity.example/build/1580"
            )
        )
        commands.single() shouldBe listOf(
            "teamcity",
            "--no-color",
            "--no-input",
            "run",
            "list",
            "--job",
            "WaterMyPlants_WaterMyPlantsFigmaSync",
            "--branch",
            "main",
            "--status",
            "running",
            "--limit",
            "1",
            "--json"
        )
        environments.single() shouldBe expectedEnvironment
    }

    test("queues and waits for a run through typed commands") {
        val commands = mutableListOf<List<String>>()
        val client = TeamCityCliClient { command, _ ->
            commands += command
            val finished = command.contains("watch")
            TeamCityCliClient.CommandResult(
                exitCode = 0,
                output =
                    """
                    {
                      "id": 1581,
                      "state": "${if (finished) "finished" else "queued"}",
                      "status": "${if (finished) "SUCCESS" else "UNKNOWN"}",
                      "branchName": "main",
                      "webUrl": "https://teamcity.example/build/1581"
                    }
                    """.trimIndent(),
                error = ""
            )
        }

        client.startRun("WaterMyPlants_WaterMyPlantsFigmaSync", "main").state shouldBe "queued"
        client.watchRun(1581, pollIntervalSeconds = 10, timeoutMinutes = 60).status shouldBe "SUCCESS"
        commands shouldBe listOf(
            listOf(
                "teamcity", "--no-color", "--no-input", "run", "start",
                "WaterMyPlants_WaterMyPlantsFigmaSync", "--branch", "main", "--json"
            ),
            listOf(
                "teamcity", "--no-color", "--no-input", "run", "watch", "1581",
                "--interval", "10", "--timeout", "60m", "--json"
            )
        )
    }
})
