package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import java.net.URI

internal class TeamCityRestRunStarterTest : FunSpec(
    {
    test("queues a run with Bearer and Cloudflare headers but no cookies") {
        var requestedUri: URI? = null
        var requestedHeaders: Map<String, String>? = null
        var requestedBody: String? = null
        val starter = TeamCityRestRunStarter(
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

        starter.startRun(
            "WaterMyPlants_WaterMyPlantsFigmaSync",
            "main"
        ) shouldBe
            TeamCityRun(
                id = 1680,
                state = "queued",
                status = null,
                statusText = null,
                branchName = "main",
                webUrl = "https://teamcity.example/build/1680"
            )
        requestedUri shouldBe URI.create("https://teamcity.example/app/rest/buildQueue")
        requestedHeaders shouldBe mapOf(
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

    test("rejects redirects instead of following them with authorization headers") {
        val starter = TeamCityRestRunStarter(
            serverUrl = "https://teamcity.example",
            teamCityToken = "teamcity-token",
            cloudflareAccessToken = "cloudflare-token"
        ) { _, _, _ ->
            TeamCityRestRunStarter.Response(
                statusCode = 302,
                body = "redirect"
            )
        }

        val exception = shouldThrow<IllegalArgumentException> {
            starter.startRun(
                "WaterMyPlants_WaterMyPlantsFigmaSync",
                "main"
            )
        }

        exception.message shouldBe
            "TeamCity REST queue request failed with HTTP 302: redirect"
    }

    test("rejects a malformed successful response") {
        val starter = TeamCityRestRunStarter(
            serverUrl = "https://teamcity.example",
            teamCityToken = "teamcity-token",
            cloudflareAccessToken = "cloudflare-token"
        ) { _, _, _ ->
            TeamCityRestRunStarter.Response(
                statusCode = 200,
                body = "not-json"
            )
        }

        val exception = shouldThrow<IllegalArgumentException> {
            starter.startRun(
                "WaterMyPlants_WaterMyPlantsFigmaSync",
                "main"
            )
        }

        exception.message shouldBe "TeamCity REST returned invalid JSON."
    }

    test("requires an HTTPS TeamCity origin") {
        val exception = shouldThrow<IllegalArgumentException> {
            TeamCityRestRunStarter(
                serverUrl = "http://teamcity.example",
                teamCityToken = "teamcity-token",
                cloudflareAccessToken = "cloudflare-token"
            )
        }

        exception.message shouldBe "The public TeamCity automation endpoint must use HTTPS."
    }
}
)
