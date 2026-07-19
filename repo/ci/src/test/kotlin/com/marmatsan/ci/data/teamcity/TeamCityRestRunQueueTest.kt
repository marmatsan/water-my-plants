package com.marmatsan.ci.data.teamcity

import com.marmatsan.ci.domain.model.TeamCityQueuedRun
import com.marmatsan.ci.domain.model.TeamCityRunRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import java.net.URI

internal class TeamCityRestRunQueueTest : FunSpec({
    test("queues an infrastructure run through the loopback TeamCity origin") {
        var requestedUri: URI? = null
        var requestedHeaders: Map<String, String>? = null
        var requestedBody: String? = null
        val queue = TeamCityRestRunQueue(
            serverUrl = "http://127.0.0.1:8111/",
            teamCityToken = "teamcity-token"
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
                    """.trimIndent()
            )
        }

        queue.queue(
            TeamCityRunRequest(
                buildTypeId = "WaterMyPlants_WaterMyPlantsInfrastructureHealth",
                branch = "main"
            )
        ) shouldBe TeamCityQueuedRun(
            id = 1800,
            state = "queued",
            branch = "main",
            webUrl = "http://127.0.0.1:8111/build/1800"
        )
        requestedUri shouldBe URI.create("http://127.0.0.1:8111/app/rest/buildQueue")
        requestedHeaders shouldBe mapOf(
            "Accept" to "application/json",
            "Authorization" to "Bearer teamcity-token",
            "Content-Type" to "application/json"
        )
        requestedHeaders.orEmpty().shouldNotContainKey("Cookie")
        requestedBody shouldBe
            "{\"buildType\":{\"id\":\"WaterMyPlants_WaterMyPlantsInfrastructureHealth\"}," +
                "\"branchName\":\"main\"}"
    }

    test("rejects plain HTTP for a non-loopback TeamCity origin") {
        val failure = shouldThrow<IllegalArgumentException> {
            TeamCityRestRunQueue("http://teamcity.example", "teamcity-token")
        }

        failure.message shouldBe "TeamCity automation requires HTTPS or an HTTP loopback origin."
    }

    test("rejects redirects instead of forwarding the bearer token") {
        val queue = TeamCityRestRunQueue(
            serverUrl = "https://teamcity.example",
            teamCityToken = "teamcity-token"
        ) { _, _, _ ->
            TeamCityRestRunQueue.Response(statusCode = 302, body = "redirect")
        }

        val failure = shouldThrow<IllegalArgumentException> {
            queue.queue(TeamCityRunRequest("InfrastructureHealth", "main"))
        }

        failure.message shouldBe "TeamCity REST queue request failed with HTTP 302: redirect"
    }
})
