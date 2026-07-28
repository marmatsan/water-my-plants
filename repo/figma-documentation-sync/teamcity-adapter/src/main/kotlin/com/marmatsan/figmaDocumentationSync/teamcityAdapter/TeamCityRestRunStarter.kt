package com.marmatsan.figmaDocumentationSync.teamcityAdapter

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration

/** Cookie-free REST adapter for queueing a TeamCity run with Bearer authentication. */
class TeamCityRestRunStarter(
    serverUrl: String,
    private val teamCityToken: String,
    private val cloudflareAccessToken: String,
    private val send: (URI, Map<String, String>, String) -> Response = ::sendRequest,
) : TeamCityRunStarter {
    private val endpoint =
        URI.create("${serverUrl.trimEnd('/')}/app/rest/buildQueue").also { uri ->
            require(
                uri.scheme.equals(
                    "https",
                    ignoreCase = true,
                ),
            ) {
                "The public TeamCity automation endpoint must use HTTPS."
            }
            require(uri.host != null && uri.userInfo == null && uri.query == null && uri.fragment == null) {
                "The public TeamCity automation endpoint must be an absolute HTTPS origin."
            }
        }

    init {
        require(teamCityToken.isNotBlank()) { "The TeamCity automation token must not be blank." }
        require(cloudflareAccessToken.isNotBlank()) {
            "The Cloudflare Access token must not be blank."
        }
    }

    /** Queues [buildTypeId] on [branch] through the authenticated public REST endpoint. */
    override fun startRun(
        buildTypeId: String,
        branch: String,
    ): Result<TeamCityRun, TeamCityRunStartError> {
        require(buildTypeId.isNotBlank()) { "The TeamCity build type id must not be blank." }
        require(branch.isNotBlank()) { "The TeamCity branch must not be blank." }
        val body =
            buildJsonObject {
                put(
                    "buildType",
                    buildJsonObject {
                        put(
                            "id",
                            buildTypeId,
                        )
                    },
                )
                put(
                    "branchName",
                    branch,
                )
            }.toString()
        val response =
            try {
                send(
                    endpoint,
                    mapOf(
                        "Accept" to "application/json",
                        "Authorization" to "Bearer $teamCityToken",
                        "CF-Access-Token" to cloudflareAccessToken,
                        "Content-Type" to "application/json",
                    ),
                    body,
                )
            } catch (
                exception: InterruptedException,
            ) {
                Thread.currentThread().interrupt()
                throw exception
            } catch (
                exception: IOException,
            ) {
                return Err(
                    TeamCityRunStartError.Unavailable(
                        detail = exception.message.orEmpty(),
                    ),
                )
            }
        if (response.statusCode !in 200..299) {
            return Err(
                TeamCityRunStartError.RequestRejected(
                    statusCode = response.statusCode,
                    responseBody = response.body.take(MAX_ERROR_BODY_LENGTH),
                ),
            )
        }
        return try {
            Ok(
                Json
                    .parseToJsonElement(response.body)
                    .jsonObject
                    .toTeamCityRun(
                        defaultBranch = branch,
                    ),
            )
        } catch (
            _: RuntimeException,
        ) {
            Err(TeamCityRunStartError.InvalidResponse)
        }
    }

    /**
     * Minimal HTTP response projection exposed by the injectable transport boundary.
     *
     * @property statusCode HTTP response status used to validate queueing.
     * @property body UTF-8 response body containing the created run or error details.
     */
    data class Response(
        val statusCode: Int,
        val body: String,
    )

    private companion object {
        const val MAX_ERROR_BODY_LENGTH = 500
        val requestTimeout: Duration = Duration.ofSeconds(30)

        fun sendRequest(
            uri: URI,
            headers: Map<String, String>,
            body: String,
        ): Response {
            val requestBuilder =
                HttpRequest
                    .newBuilder(uri)
                    .timeout(requestTimeout)
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            body,
                            StandardCharsets.UTF_8,
                        ),
                    )
            headers.forEach(requestBuilder::header)
            val response =
                HttpClient
                    .newBuilder()
                    .connectTimeout(requestTimeout)
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build()
                    .send(
                        requestBuilder.build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8),
                    )
            return Response(
                statusCode = response.statusCode(),
                body = response.body(),
            )
        }
    }
}

private fun JsonObject.toTeamCityRun(
    defaultBranch: String,
): TeamCityRun =
    TeamCityRun(
        id =
            requiredString(
                name = "id",
            ).toLong(),
        state =
            requiredString(
                name = "state",
            ),
        status =
            optionalString(
                name = "status",
            ),
        statusText =
            optionalString(
                name = "statusText",
            ),
        branchName =
            optionalString(
                name = "branchName",
            ) ?: defaultBranch,
        webUrl =
            optionalString(
                name = "webUrl",
            ),
    )

private fun JsonObject.requiredString(
    name: String,
): String =
    optionalString(
        name = name,
    )
        ?: throw IllegalArgumentException("TeamCity REST response is missing '$name'.")

private fun JsonObject.optionalString(
    name: String,
): String? =
    this[name]?.jsonPrimitive?.contentOrNull
