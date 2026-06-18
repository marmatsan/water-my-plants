package com.marmatsan.figmaCatalogChecks.data.figma.client

import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaFileNodesResponse
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaFileResponse
import com.marmatsan.figmaCatalogChecks.data.figma.dto.FigmaNode
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

class FigmaFileContentClient(
    private val httpClient: HttpClient = defaultHttpClient()
) {
    fun getFileContent(
        fileKey: String,
        token: String,
        depth: Int? = null
    ): FigmaFileResponse = runBlocking {
        try {
            httpClient
                .get("https://api.figma.com/v1/files/$fileKey") {
                    header("X-Figma-Token", token)
                    depth?.let { parameter("depth", it) }
                }
                .body()
        } catch (exception: ResponseException) {
            throw FigmaFileContentException(
                "Figma file content request failed with HTTP ${exception.response.status.value}: ${
                    exception.response.body<String>().take(500)
                }",
                exception
            )
        } catch (exception: HttpRequestTimeoutException) {
            throw FigmaFileContentException(
                "Figma file content request timed out after ${REQUEST_TIMEOUT_MILLIS} ms",
                exception
            )
        }
    }

    fun getNodeContent(
        fileKey: String,
        token: String,
        nodeId: String
    ): FigmaNode = runBlocking {
        try {
            val response = httpClient
                .get("https://api.figma.com/v1/files/$fileKey/nodes") {
                    header("X-Figma-Token", token)
                    parameter("ids", nodeId)
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
