package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.marmatsan.unitTest.dsl.given
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import java.net.URI

internal class TeamCityRestRunStarterTest :
    FunSpec(
        {
            test("queues a run with Bearer and Cloudflare headers but no cookies") {
                var requestedUri: URI? = null
                var requestedHeaders: Map<String, String>? = null
                var requestedBody: String? = null
                given {
                    TeamCityRestRunStarter(
                        serverUrl = "https://teamcity.example/",
                        teamCityToken = "teamcity-token",
                        cloudflareAccessToken = "cloudflare-token"
                    ) { uri, headers, body ->
                        requestedUri = uri
                        requestedHeaders = headers
                        requestedBody = body
                        TeamCityRestRunStarter.Response(
                            statusCode = 200,
                            body =
                                """
                                {
                                  "id": 1680,
                                  "state": "queued",
                                  "branchName": "main",
                                  "webUrl": "https://teamcity.example/build/1680"
                                }
                                """.trimIndent()
                        )
                    }
                }.whenever { starter ->
                    starter.startRun(
                        buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync",
                        branch = "main"
                    )
                }.then { result ->
                    result shouldBe
                        Ok(
                            TeamCityRun(
                                id = 1680,
                                state = "queued",
                                status = null,
                                statusText = null,
                                branchName = "main",
                                webUrl = "https://teamcity.example/build/1680"
                            )
                        )
                    requestedUri shouldBe URI.create("https://teamcity.example/app/rest/buildQueue")
                    requestedHeaders shouldBe
                        mapOf(
                            "Accept" to "application/json",
                            "Authorization" to "Bearer teamcity-token",
                            "CF-Access-Token" to "cloudflare-token",
                            "Content-Type" to "application/json"
                        )
                    requestedHeaders.orEmpty().shouldNotContainKey("Cookie")
                    requestedBody shouldBe
                        "{\"buildType\":{\"id\":\"WaterMyPlants_WaterMyPlantsFigmaSync\"}," +
                        "\"branchName\":\"main\"}"
                }
            }

            test("returns rejected requests instead of following authorization redirects") {
                given {
                    TeamCityRestRunStarter(
                        serverUrl = "https://teamcity.example",
                        teamCityToken = "teamcity-token",
                        cloudflareAccessToken = "cloudflare-token"
                    ) { _, _, _ ->
                        TeamCityRestRunStarter.Response(
                            statusCode = 302,
                            body = "redirect"
                        )
                    }
                }.whenever { starter ->
                    starter.startRun(
                        buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync",
                        branch = "main"
                    )
                }.then { result ->
                    result shouldBe
                        Err(
                            TeamCityRunStartError.RequestRejected(
                                statusCode = 302,
                                responseBody = "redirect"
                            )
                        )
                }
            }

            test("returns an invalid response for malformed successful JSON") {
                given {
                    TeamCityRestRunStarter(
                        serverUrl = "https://teamcity.example",
                        teamCityToken = "teamcity-token",
                        cloudflareAccessToken = "cloudflare-token"
                    ) { _, _, _ ->
                        TeamCityRestRunStarter.Response(
                            statusCode = 200,
                            body = "not-json"
                        )
                    }
                }.whenever { starter ->
                    starter.startRun(
                        buildTypeId = "WaterMyPlants_WaterMyPlantsFigmaSync",
                        branch = "main"
                    )
                }.then { result ->
                    result shouldBe Err(TeamCityRunStartError.InvalidResponse)
                }
            }

            test("requires an HTTPS TeamCity origin") {
                given {
                    "http://teamcity.example"
                }.whenever { serverUrl ->
                    shouldThrow<IllegalArgumentException> {
                        TeamCityRestRunStarter(
                            serverUrl = serverUrl,
                            teamCityToken = "teamcity-token",
                            cloudflareAccessToken = "cloudflare-token"
                        )
                    }
                }.then { exception ->
                    exception.message shouldBe "The public TeamCity automation endpoint must use HTTPS."
                }
            }
        }
    )
