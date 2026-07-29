package com.marmatsan.figmaDocumentationSync.data.figma.client

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.marmatsan.figmaDocumentationSync.data.figma.dto.FigmaFileNodesResponse
import com.marmatsan.figmaDocumentationSync.data.figma.dto.FigmaNode
import com.marmatsan.figmaDocumentationSync.domain.model.figma.FigmaNodeContent
import com.marmatsan.figmaDocumentationSync.domain.model.figma.FigmaNodeContentError
import com.marmatsan.figmaDocumentationSync.domain.port.figma.FigmaNodeContentSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * Small Figma API client used by the sync checker to read node content and
 * shared plugin metadata.
 *
 * This client is intentionally narrow: the pipeline only needs the `/nodes`
 * endpoint for a single node id. Write operations and SVG imports are handled
 * by the Figma MCP workflow, not by this Gradle plugin.
 */
class FigmaFileContentClient internal constructor(
    private val fetch: suspend (String, String, String, String?) -> FigmaNode?
) : FigmaNodeContentSource {
    constructor(
        httpClient: HttpClient = defaultHttpClient()
    ) : this(
        fetch = { fileKey, token, nodeId, pluginData ->
            httpClient
                .get(
                    urlString = "https://api.figma.com/v1/files/$fileKey/nodes"
                ) {
                    header(
                        "X-Figma-Token",
                        token
                    )
                    parameter(
                        "ids",
                        nodeId
                    )
                    pluginData?.let {
                        parameter(
                            "plugin_data",
                            it
                        )
                    }
                }.body<FigmaFileNodesResponse>()
                .nodes[nodeId]
                ?.document
        }
    )

    /**
     * Reads a Figma node by [fileKey] and [nodeId].
     *
     * Set [pluginData] to `shared` when the caller needs shared plugin data,
     * such as the `modelHash` written after publishing `design-model.json` to
     * Figma.
     *
     * Expected provider failures are returned as [FigmaNodeContentError]
     * without leaking Ktor or wire DTO types.
     */
    override fun readNodeContent(
        fileKey: String,
        token: String,
        nodeId: String,
        pluginData: String?
    ): Result<FigmaNodeContent, FigmaNodeContentError> =
        runBlocking {
            try {
                val node =
                    fetch(
                        fileKey,
                        token,
                        nodeId,
                        pluginData
                    ) ?: return@runBlocking Err(FigmaNodeContentError.NotFound(nodeId))
                Ok(
                    FigmaNodeContent(
                        sharedPluginData = node.sharedPluginData
                    )
                )
            } catch (
                exception: ResponseException
            ) {
                Err(
                    FigmaNodeContentError.RequestRejected(
                        statusCode = exception.response.status.value,
                        responseBody = exception.response.body<String>().take(MAX_ERROR_BODY_LENGTH)
                    )
                )
            } catch (
                exception: HttpRequestTimeoutException
            ) {
                Err(
                    FigmaNodeContentError.TimedOut(
                        timeoutMillis = REQUEST_TIMEOUT_MILLIS
                    )
                )
            } catch (
                exception: IOException
            ) {
                Err(
                    FigmaNodeContentError.Unavailable(
                        detail = exception.message.orEmpty()
                    )
                )
            } catch (
                _: RuntimeException
            ) {
                Err(FigmaNodeContentError.InvalidResponse)
            }
        }

    private companion object {
        fun defaultHttpClient(): HttpClient =
            HttpClient(CIO) {
                expectSuccess = true
                install(HttpTimeout) {
                    requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                    connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
                    socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS
                }
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                        }
                    )
                }
            }

        const val REQUEST_TIMEOUT_MILLIS = 60_000L
        const val CONNECT_TIMEOUT_MILLIS = 15_000L
        const val SOCKET_TIMEOUT_MILLIS = 15_000L
        const val MAX_ERROR_BODY_LENGTH = 500
    }
}
