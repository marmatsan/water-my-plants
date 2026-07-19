package com.marmatsan.figmaDesignSync.data.mcp

import com.marmatsan.figmaDesignSync.domain.model.writer.McpToolResult
import com.marmatsan.figmaDesignSync.domain.port.writer.McpClientPort
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.sse.SSE
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpClientTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceRequest
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceRequestParams
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import kotlinx.coroutines.runBlocking

/** Official Kotlin MCP SDK adapter for the local Streamable HTTP endpoint. */
class KotlinSdkMcpClient private constructor(
    private val httpClient: HttpClient,
    private val client: Client
) : McpClientPort {
    private val assetUploader = KtorFigmaPngAssetUploader()

    override suspend fun listToolNames(): List<String> = client.listTools().tools.map { tool -> tool.name }

    override suspend fun readTextResource(uri: String): String = client.readResource(
        ReadResourceRequest(ReadResourceRequestParams(uri = uri))
    ).contents.filterIsInstance<TextResourceContents>().joinToString("\n") { content -> content.text }.trim()

    override suspend fun useFigma(
        fileKey: String,
        code: String,
        description: String,
        skillNames: String
    ): McpToolResult = client.callTool(
        name = USE_FIGMA_TOOL,
        arguments = mapOf(
            "fileKey" to fileKey,
            "code" to code,
            "description" to description,
            "skillNames" to skillNames
        )
    ).toDomain()

    override suspend fun requestAssetUpload(fileKey: String, count: Int): McpToolResult = client.callTool(
        name = UPLOAD_ASSETS_TOOL,
        arguments = mapOf("fileKey" to fileKey, "count" to count)
    ).toDomain()

    override suspend fun uploadAsset(url: String, bytes: ByteArray) = assetUploader.upload(url, bytes)

    override fun close() {
        runBlocking { client.close() }
        httpClient.close()
    }

    private fun io.modelcontextprotocol.kotlin.sdk.types.CallToolResult.toDomain(): McpToolResult = McpToolResult(
        isError = isError == true,
        text = content.filterIsInstance<TextContent>().joinToString("\n") { value -> value.text }.trim()
    )

    companion object {
        fun connect(endpoint: String, clientName: String): KotlinSdkMcpClient {
            val httpClient = HttpClient(CIO) { install(SSE) }
            val client = Client(clientInfo = Implementation(name = clientName, version = CLIENT_VERSION))
            val transport = StreamableHttpClientTransport(client = httpClient, url = endpoint)
            runBlocking { client.connect(transport) }
            return KotlinSdkMcpClient(httpClient, client)
        }

        private const val CLIENT_VERSION = "1.0.0"
        private const val USE_FIGMA_TOOL = "use_figma"
        private const val UPLOAD_ASSETS_TOOL = "upload_assets"
    }
}
