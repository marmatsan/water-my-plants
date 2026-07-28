package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe

internal class TeamCityCliClientTest :
    FunSpec(
        {
            val temporaryDirectory =
                tempdir(
                    prefix = "teamcity-cli-client",
                )

            test("reads build identity through the TeamCity CLI JSON contract") {
                val commands = mutableListOf<List<String>>()
                given {
                    TeamCityCliClient { command, _ ->
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
                            error = "",
                        )
                    }
                }.whenever { client ->
                    client.readBuild(
                        buildId = 1573,
                    )
                }.then { build ->
                    build shouldBe
                        TeamCityBuild(
                            id = 1573,
                            state = "finished",
                            status = "SUCCESS",
                            branchName = "main",
                            buildTypeName = "Generate main design model",
                            webUrl = "https://teamcity.example/build/1573",
                        )
                    commands.single() shouldBe
                        listOf(
                            "teamcity",
                            "--no-color",
                            "--no-input",
                            "build",
                            "view",
                            "1573",
                            "--json",
                        )
                }
            }

            test("downloads one build artifact set into the requested directory") {
                val output =
                    temporaryDirectory.resolve("download/artifacts")
                val commands = mutableListOf<List<String>>()
                given {
                    TeamCityCliClient { command, _ ->
                        commands += command
                        TeamCityCliClient.CommandResult(
                            exitCode = 0,
                            output = "downloaded",
                            error = "",
                        )
                    }
                }.whenever { client ->
                    client.downloadArtifacts(
                        buildId = 1573,
                        outputDirectory = output,
                    )
                }.then {
                    output.isDirectory shouldBe true
                    commands.single() shouldBe
                        listOf(
                            "teamcity",
                            "--no-color",
                            "--no-input",
                            "build",
                            "download",
                            "1573",
                            "--output",
                            output.absolutePath,
                        )
                }
            }

            test("reports the canonical command when a read operation fails") {
                given {
                    TeamCityCliClient { _, _ ->
                        TeamCityCliClient.CommandResult(
                            exitCode = 1,
                            output = "",
                            error = "authentication response was HTML",
                        )
                    }
                }.whenever { client ->
                    runCatching {
                        client.readBuild(
                            buildId = 1573,
                        )
                    }.exceptionOrNull()
                }.then { failure ->
                    failure?.message shouldBe
                        "TeamCity CLI command 'build view 1573 --json' failed with exit code 1: " +
                        "authentication response was HTML"
                }
            }

            test("lists active runs with the authenticated command environment") {
                val commands = mutableListOf<List<String>>()
                val environments = mutableListOf<Map<String, String?>>()
                val expectedEnvironment =
                    mapOf(
                        "TEAMCITY_URL" to "https://teamcity.example",
                        "TEAMCITY_TOKEN" to "teamcity-token",
                        "TEAMCITY_HEADER_CF_ACCESS_TOKEN" to "cloudflare-token",
                        "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID" to null,
                        "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET" to null,
                    )
                given {
                    TeamCityCliClient(
                        environment = expectedEnvironment,
                    ) { command, environment ->
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
                            error = "",
                        )
                    }
                }.whenever { client ->
                    client.listRuns(
                        buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync",
                        branch = "main",
                        status = "running",
                    )
                }.then { runs ->
                    runs shouldBe
                        listOf(
                            TeamCityRun(
                                id = 1580,
                                state = "running",
                                status = null,
                                statusText = null,
                                branchName = "main",
                                webUrl = "https://teamcity.example/build/1580",
                            ),
                        )
                    commands.single() shouldBe
                        listOf(
                            "teamcity",
                            "--no-color",
                            "--no-input",
                            "build",
                            "list",
                            "--job",
                            "WaterMyPlants_WaterMyPlantsFigmaSync",
                            "--branch",
                            "main",
                            "--status",
                            "running",
                            "--limit",
                            "1",
                            "--json",
                        )
                    environments.single() shouldBe expectedEnvironment
                }
            }

            test("queues and waits for a run through typed commands") {
                val commands = mutableListOf<List<String>>()
                given {
                    TeamCityCliClient { command, _ ->
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
                            error = "",
                        )
                    }
                }.whenever { client ->
                    client.startRun(
                        buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync",
                        branch = "main",
                    ) to
                        client.watchRun(
                            buildId = 1581,
                            pollIntervalSeconds = 10,
                            timeoutMinutes = 60,
                        )
                }.then { (started, finished) ->
                    started shouldBe
                        Ok(
                            TeamCityRun(
                                id = 1581,
                                state = "queued",
                                status = "UNKNOWN",
                                statusText = null,
                                branchName = "main",
                                webUrl = "https://teamcity.example/build/1581",
                            ),
                        )
                    finished.status shouldBe "SUCCESS"
                    commands shouldBe
                        listOf(
                            listOf(
                                "teamcity",
                                "--no-color",
                                "--no-input",
                                "build",
                                "start",
                                "WaterMyPlants_WaterMyPlantsFigmaSync",
                                "--branch",
                                "main",
                                "--json",
                            ),
                            listOf(
                                "teamcity",
                                "--no-color",
                                "--no-input",
                                "build",
                                "watch",
                                "1581",
                                "--interval",
                                "10",
                                "--timeout",
                                "60m",
                                "--json",
                            ),
                        )
                }
            }

            test("returns a typed failure when the queue command exits unsuccessfully") {
                given {
                    TeamCityCliClient { _, _ ->
                        TeamCityCliClient.CommandResult(
                            exitCode = 1,
                            output = "",
                            error = "not authorized",
                        )
                    }
                }.whenever { client ->
                    client.startRun(
                        buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync",
                        branch = "main",
                    )
                }.then { result ->
                    result shouldBe
                        Err(
                            TeamCityRunStartError.CommandFailed(
                                exitCode = 1,
                                detail = "not authorized",
                            ),
                        )
                }
            }
        },
    )
