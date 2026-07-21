package com.marmatsan.figmaDocumentationSync.data.mcp

import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import java.net.URI

/** Uploads one validated canonical PNG only to a single-use Figma MCP asset URL. */
class KtorFigmaPngAssetUploader internal constructor(
    private val send: suspend (URI, ByteArray) -> Int,
) {
    constructor() : this(::sendWithKtor)

    suspend fun upload(
        url: String,
        bytes: ByteArray,
    ) {
        val target =
            requireAllowedTarget(
                url = url,
            )
        require(bytes.size in 1..PayloadPngEncoder.MAX_FIGMA_UPLOAD_ASSET_BYTES) {
            "Canonical Figma payload must contain between 1 and " +
                "${PayloadPngEncoder.MAX_FIGMA_UPLOAD_ASSET_BYTES} bytes."
        }
        require(
            bytes.startsWith(
                prefix = PayloadPngEncoder.PNG_SIGNATURE,
            ),
        ) {
            "Canonical Figma payload is not a PNG file."
        }

        val status =
            send(
                target,
                bytes,
            )
        require(status in 200..299) { "Payload upload failed with HTTP $status." }
    }

    fun uploadBlocking(
        url: String,
        bytes: ByteArray,
    ) = runBlocking {
        upload(
            url = url,
            bytes = bytes,
        )
    }

    private fun requireAllowedTarget(
        url: String,
    ): URI {
        val target =
            runCatching { URI(url) }.getOrElse { failure ->
                throw IllegalArgumentException(
                    "Figma upload URL is invalid.",
                    failure,
                )
            }
        require(
            !target.isOpaque &&
                target.scheme.equals(
                    "https",
                    ignoreCase = true,
                ),
        ) {
            "Figma upload URL must use HTTPS."
        }
        require(
            target.host.equals(
                ALLOWED_HOST,
                ignoreCase = true,
            ),
        ) {
            "Figma upload URL host must be $ALLOWED_HOST."
        }
        require(target.port == -1 || target.port == HTTPS_PORT) {
            "Figma upload URL must use the default HTTPS port."
        }
        require(target.userInfo == null && target.fragment == null) {
            "Figma upload URL must not contain user info or a fragment."
        }
        require(UPLOAD_PATH.matches(target.path)) {
            "Figma upload URL path is not an MCP asset submit endpoint."
        }
        require(target.rawQuery == EXPECTED_QUERY) {
            "Figma upload URL must use the expected PNG placement query."
        }
        return target
    }

    private fun ByteArray.startsWith(
        prefix: ByteArray,
    ): Boolean =
        size >= prefix.size && prefix.indices.all { index -> this[index] == prefix[index] }

    private companion object {
        const val ALLOWED_HOST = "mcp.figma.com"
        const val HTTPS_PORT = 443
        const val EXPECTED_QUERY = "scaleMode=FILL"
        val UPLOAD_PATH =
            Regex(
                "^/mcp/upload/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-" +
                    "[0-9a-fA-F]{4}-[0-9a-fA-F]{12}/submit$",
            )

        suspend fun sendWithKtor(
            target: URI,
            bytes: ByteArray,
        ): Int =
            HttpClient(CIO) { followRedirects = false }.use { client ->
                client
                    .post(target.toString()) {
                        header(
                            HttpHeaders.ContentType,
                            ContentType.Image.PNG.toString(),
                        )
                        setBody(bytes)
                    }.status.value
            }
    }
}
