package com.marmatsan.figmaDocumentationSync.data.figma.client

import com.marmatsan.figmaDocumentationSync.data.figma.dto.FigmaFileNodesResponse
import com.marmatsan.figmaDocumentationSync.data.figma.dto.FigmaNode
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * Small Figma API client used by the sync checker to read node content and
 * shared plugin metadata.
 *
 * This client is intentionally narrow: the pipeline only needs the `/nodes`
 * endpoint for a single node id. Write operations and SVG imports are handled
 * by the Figma MCP workflow, not by this Gradle plugin.
 */
class FigmaFileContentClient(
    private val httpClient: HttpClient = defaultHttpClient()
) {
    /**
     * Reads a Figma node by [fileKey] and [nodeId].
     *
     * Set [pluginData] to `shared` when the caller needs shared plugin data,
     * such as the `modelHash` written after publishing `design-model.json` to
     * Figma.
     *
     * @throws FigmaFileContentException when Figma returns an error response or
     * the request times out.
     */
    fun getNodeContent(
        fileKey: String,
        token: String,
        nodeId: String,
        pluginData: String? = null
    ): FigmaNode = runBlocking {
        try {
            val response = httpClient
                .get("https://api.figma.com/v1/files/$fileKey/nodes") {
                    header("X-Figma-Token", token)
                    parameter("ids", nodeId)
                    pluginData?.let { parameter("plugin_data", it) }
                }
                .body<FigmaFileNodesResponse>()

            response.nodes[nodeId]?.document
                ?: error("Figma node '$nodeId' was not found")
        } catch (exception: ResponseException) {
            throw FigmaFileContentException(
                "Figma node content request failed with HTTP ${exception.response.status.value}: ${
                    exception.response.body<String>().take(500)
                }",
                exception
            )
        } catch (exception: HttpRequestTimeoutException) {
            throw FigmaFileContentException(
                "Figma node content request timed out after ${REQUEST_TIMEOUT_MILLIS} ms",
                exception
            )
        }
    }

    private companion object {
        fun defaultHttpClient(): HttpClient = HttpClient(CIO) {
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
    }
}
