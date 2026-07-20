package com.marmatsan.verificationPlatform.data.teamcity

import com.marmatsan.verificationPlatform.domain.model.TeamCityQueuedRun
import com.marmatsan.verificationPlatform.domain.model.TeamCityRunRequest
import com.marmatsan.verificationPlatform.domain.port.TeamCityRunQueue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

/**
 * Bearer-authenticated TeamCity REST adapter that never follows redirects.
 *
 * @param serverUrl trusted TeamCity origin. HTTPS is required except for HTTP
 * loopback addresses used by the local server.
 * @param teamCityToken bearer token sent only to the validated origin.
 * @param post injectable HTTP boundary used by focused adapter tests.
 */
class TeamCityRestRunQueue(
    serverUrl: String,
    private val teamCityToken: String,
    private val post: (URI, Map<String, String>, String) -> Response = ::post
) : TeamCityRunQueue {
    private val serverUri = URI.create(serverUrl.trimEnd('/')).also(::requireTrustedOrigin)

    init {
        require(teamCityToken.isNotBlank()) { "TeamCity automation token must not be blank." }
    }

    /**
     * Queues [request] through TeamCity's `buildQueue` REST resource.
     *
     * @throws IllegalArgumentException when TeamCity rejects the request or
     * returns a malformed response.
     */
    override fun queue(
        request: TeamCityRunRequest
    ): TeamCityQueuedRun {
        val uri = serverUri.resolve(
            "/app/rest/buildQueue"
        )
        val headers = mapOf(
            "Accept" to "application/json",
            "Authorization" to "Bearer $teamCityToken",
            "Content-Type" to "application/json"
        )
        val body = buildJsonObject {
            put(
                "buildType",
                buildJsonObject { put(
                    "id",
                    request.buildTypeId
                ) }
            )
            put(
                "branchName",
                request.branch
            )
        }.toString()
        val response = post(
            uri,
            headers,
            body
        )
        require(response.statusCode in 200..299) {
            "TeamCity REST queue request failed with HTTP ${response.statusCode}: ${response.body.take(500)}"
        }
        return runCatching {
            val json = Json.parseToJsonElement(response.body).jsonObject
            TeamCityQueuedRun(
                id = json.getValue("id").jsonPrimitive.long,
                state = json.getValue("state").jsonPrimitive.content,
                branch = json.getValue("branchName").jsonPrimitive.content,
                webUrl = json["webUrl"]?.jsonPrimitive?.content
            )
        }.getOrElse {
            throw IllegalArgumentException(
                "TeamCity REST returned invalid JSON.",
                it
            )
        }
    }

    /**
     * HTTP result required by the queue adapter.
     *
     * @property statusCode numeric HTTP response status.
     * @property body response body used for success parsing or bounded error
     * reporting.
     */
    data class Response(
        val statusCode: Int,
        val body: String
    )

    private companion object {
        val HTTP_CLIENT: HttpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(30))
            .build()

        fun requireTrustedOrigin(
            uri: URI
        ) {
            val trusted = uri.scheme.equals(
                "https",
                ignoreCase = true
            ) ||
                uri.scheme.equals(
                    "http",
                    ignoreCase = true
                ) && uri.host in LOOPBACK_HOSTS
            require(trusted) {
                "TeamCity automation requires HTTPS or an HTTP loopback origin."
            }
            require(uri.userInfo == null && uri.query == null && uri.fragment == null) {
                "TeamCity automation origin must not contain credentials, a query, or a fragment."
            }
        }

        fun post(
            uri: URI,
            headers: Map<String, String>,
            body: String
        ): Response {
            val request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .apply { headers.forEach(::header) }
                .build()
            val response = HTTP_CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            )
            return Response(
                statusCode = response.statusCode(),
                body = response.body()
            )
        }

        val LOOPBACK_HOSTS = setOf(
            "localhost",
            "127.0.0.1",
            "::1"
        )
    }
}
