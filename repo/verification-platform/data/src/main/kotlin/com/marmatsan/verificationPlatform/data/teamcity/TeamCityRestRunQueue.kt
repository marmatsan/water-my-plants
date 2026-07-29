package com.marmatsan.verificationPlatform.data.teamcity

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.marmatsan.verificationPlatform.domain.model.teamcity.QueueTeamCityRunError
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityQueuedRun
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityRunRequest
import com.marmatsan.verificationPlatform.domain.port.teamcity.TeamCityRunQueue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

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
     * Expected transport, rejection, and response failures are translated to
     * [QueueTeamCityRunError]. Thread interruption is preserved and rethrown.
     */
    override fun queue(
        request: TeamCityRunRequest
    ): Result<TeamCityQueuedRun, QueueTeamCityRunError> {
        val uri =
            serverUri.resolve(
                "/app/rest/buildQueue"
            )
        val headers =
            mapOf(
                "Accept" to "application/json",
                "Authorization" to "Bearer $teamCityToken",
                "Content-Type" to "application/json"
            )
        val body =
            buildJsonObject {
                put(
                    "buildType",
                    buildJsonObject {
                        put(
                            "id",
                            request.buildTypeId
                        )
                    }
                )
                put(
                    "branchName",
                    request.branch
                )
            }.toString()
        val response =
            try {
                post(
                    uri,
                    headers,
                    body
                )
            } catch (
                exception: InterruptedException
            ) {
                Thread.currentThread().interrupt()
                throw exception
            } catch (
                exception: IOException
            ) {
                return Err(
                    QueueTeamCityRunError.Unavailable(
                        detail = exception.message.orEmpty()
                    )
                )
            }
        if (response.statusCode !in 200..299) {
            return Err(
                QueueTeamCityRunError.RequestRejected(
                    statusCode = response.statusCode,
                    responseBody = response.body.take(MAX_ERROR_BODY_LENGTH)
                )
            )
        }
        return try {
            val json = Json.parseToJsonElement(response.body).jsonObject
            Ok(
                TeamCityQueuedRun(
                    id = json.getValue("id").jsonPrimitive.long,
                    state = json.getValue("state").jsonPrimitive.content,
                    branch = json.getValue("branchName").jsonPrimitive.content,
                    webUrl = json["webUrl"]?.jsonPrimitive?.content
                )
            )
        } catch (
            _: RuntimeException
        ) {
            Err(QueueTeamCityRunError.InvalidResponse)
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
        const val MAX_ERROR_BODY_LENGTH = 500

        val HTTP_CLIENT: HttpClient =
            HttpClient
                .newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(30))
                .build()

        fun requireTrustedOrigin(
            uri: URI
        ) {
            val trusted =
                uri.scheme.equals(
                    "https",
                    ignoreCase = true
                ) ||
                    (
                        uri.scheme.equals(
                            "http",
                            ignoreCase = true
                        ) && uri.host in LOOPBACK_HOSTS
                    )
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
            val request =
                HttpRequest
                    .newBuilder(uri)
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .apply { headers.forEach(::header) }
                    .build()
            val response =
                HTTP_CLIENT.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                )
            return Response(
                statusCode = response.statusCode(),
                body = response.body()
            )
        }

        val LOOPBACK_HOSTS =
            setOf(
                "localhost",
                "127.0.0.1",
                "::1"
            )
    }
}
