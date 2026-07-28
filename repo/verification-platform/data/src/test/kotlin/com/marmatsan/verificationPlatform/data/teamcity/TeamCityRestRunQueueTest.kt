package com.marmatsan.verificationPlatform.data.teamcity

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.marmatsan.unitTest.dsl.given
import com.marmatsan.verificationPlatform.domain.model.teamcity.QueueTeamCityRunError
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityQueuedRun
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityRunRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import java.io.IOException
import java.net.URI

internal class TeamCityRestRunQueueTest :
    FunSpec(
        {
            test("queues an infrastructure run through the loopback TeamCity origin") {
                var requestedUri: URI? = null
                var requestedHeaders: Map<String, String>? = null
                var requestedBody: String? = null
                given {
                    TeamCityRestRunQueue(
                        serverUrl = "http://127.0.0.1:8111/",
                        teamCityToken = "teamcity-token",
                    ) { uri, headers, body ->
                        requestedUri = uri
                        requestedHeaders = headers
                        requestedBody = body
                        TeamCityRestRunQueue.Response(
                            statusCode = 200,
                            body =
                                """
                                {
                                  "id": 1800,
                                  "state": "queued",
                                  "branchName": "main",
                                  "webUrl": "http://127.0.0.1:8111/build/1800"
                                }
                                """.trimIndent(),
                        )
                    }
                }.whenever { queue ->
                    queue.queue(
                        request =
                            TeamCityRunRequest(
                                buildTypeId = "WaterMyPlants_WaterMyPlantsInfrastructureHealth",
                                branch = "main",
                            ),
                    )
                }.then { result ->
                    result shouldBe
                        Ok(
                            TeamCityQueuedRun(
                                id = 1800,
                                state = "queued",
                                branch = "main",
                                webUrl = "http://127.0.0.1:8111/build/1800",
                            ),
                        )
                    requestedUri shouldBe URI.create("http://127.0.0.1:8111/app/rest/buildQueue")
                    requestedHeaders shouldBe
                        mapOf(
                            "Accept" to "application/json",
                            "Authorization" to "Bearer teamcity-token",
                            "Content-Type" to "application/json",
                        )
                    requestedHeaders.orEmpty().shouldNotContainKey("Cookie")
                    requestedBody shouldBe
                        "{\"buildType\":{\"id\":\"WaterMyPlants_WaterMyPlantsInfrastructureHealth\"}," +
                        "\"branchName\":\"main\"}"
                }
            }

            test("rejects plain HTTP for a non-loopback TeamCity origin") {
                given {
                    "http://teamcity.example"
                }.whenever { serverUrl ->
                    shouldThrow<IllegalArgumentException> {
                        TeamCityRestRunQueue(
                            serverUrl = serverUrl,
                            teamCityToken = "teamcity-token",
                        )
                    }
                }.then { failure ->
                    failure.message shouldBe
                        "TeamCity automation requires HTTPS or an HTTP loopback origin."
                }
            }

            test("returns a rejected request without forwarding the bearer token") {
                given {
                    TeamCityRestRunQueue(
                        serverUrl = "https://teamcity.example",
                        teamCityToken = "teamcity-token",
                    ) { _, _, _ ->
                        TeamCityRestRunQueue.Response(
                            statusCode = 302,
                            body = "redirect",
                        )
                    }
                }.whenever { queue ->
                    queue.queue(
                        request =
                            TeamCityRunRequest(
                                buildTypeId = "InfrastructureHealth",
                                branch = "main",
                            ),
                    )
                }.then { result ->
                    result shouldBe
                        Err(
                            QueueTeamCityRunError.RequestRejected(
                                statusCode = 302,
                                responseBody = "redirect",
                            ),
                        )
                }
            }

            test("returns an invalid response for malformed TeamCity JSON") {
                given {
                    TeamCityRestRunQueue(
                        serverUrl = "https://teamcity.example",
                        teamCityToken = "teamcity-token",
                    ) { _, _, _ ->
                        TeamCityRestRunQueue.Response(
                            statusCode = 200,
                            body = "{}",
                        )
                    }
                }.whenever { queue ->
                    queue.queue(
                        TeamCityRunRequest(
                            buildTypeId = "InfrastructureHealth",
                            branch = "main",
                        ),
                    )
                }.then { result ->
                    result shouldBe Err(QueueTeamCityRunError.InvalidResponse)
                }
            }

            test("returns unavailable when the TeamCity transport fails") {
                given {
                    TeamCityRestRunQueue(
                        serverUrl = "https://teamcity.example",
                        teamCityToken = "teamcity-token",
                    ) { _, _, _ ->
                        throw IOException("connection refused")
                    }
                }.whenever { queue ->
                    queue.queue(
                        TeamCityRunRequest(
                            buildTypeId = "InfrastructureHealth",
                            branch = "main",
                        ),
                    )
                }.then { result ->
                    result shouldBe
                        Err(
                            QueueTeamCityRunError.Unavailable(
                                detail = "connection refused",
                            ),
                        )
                }
            }
        },
    )
