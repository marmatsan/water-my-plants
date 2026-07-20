package com.marmatsan.figmaDocumentationSync.data.png

import com.marmatsan.figmaDocumentationSync.domain.model.writer.OfficialSyncPayload
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.zip.CRC32
import java.util.zip.DeflaterOutputStream

/** Encodes an official writer payload in a valid one-pixel PNG text chunk. */
class PayloadPngEncoder {
    fun payloadJson(
        payload: OfficialSyncPayload,
    ): String {
        val json =
            JsonObject(
                linkedMapOf(
                    "payloadSchemaVersion" to JsonPrimitive(payload.payloadSchemaVersion),
                    "designModelJson" to JsonPrimitive(payload.designModelJson),
                    "designModelHash" to JsonPrimitive(payload.designModelHash),
                    "designModelGitSha" to JsonPrimitive(payload.designModelGitSha),
                    "designModelLength" to JsonPrimitive(payload.designModelLength),
                    "script" to JsonPrimitive(payload.script),
                    "scriptLength" to JsonPrimitive(payload.scriptLength),
                    "writerHash" to JsonPrimitive(payload.writerHash),
                    "transportHash" to JsonPrimitive(payload.transportHash),
                ),
            )
        return Json
            .encodeToString(
                JsonObject.serializer(),
                json,
            ).toAsciiJson()
    }

    fun encode(
        payloadJson: String,
    ): ByteArray {
        val encodedPayload = Base64.getEncoder().encodeToString(payloadJson.toByteArray(StandardCharsets.UTF_8))
        val textPayload = "$TEXT_KEYWORD\u0000$encodedPayload".toByteArray(StandardCharsets.ISO_8859_1)
        val ihdr =
            ByteBuffer
                .allocate(13)
                .putInt(1)
                .putInt(1)
                .put(8)
                .put(6)
                .put(0)
                .put(0)
                .put(0)
                .array()
        val compressedPixel =
            ByteArrayOutputStream().use { output ->
                DeflaterOutputStream(output).use { deflater ->
                    deflater.write(
                        byteArrayOf(
                            0,
                            -1,
                            -1,
                            -1,
                            -1,
                        ),
                    )
                }
                output.toByteArray()
            }

        return ByteArrayOutputStream().use { output ->
            output.write(PNG_SIGNATURE)
            output.write(
                chunk(
                    type = "IHDR",
                    data = ihdr,
                ),
            )
            output.write(
                chunk(
                    type = "tEXt",
                    data = textPayload,
                ),
            )
            output.write(
                chunk(
                    type = "IDAT",
                    data = compressedPixel,
                ),
            )
            output.write(
                chunk(
                    type = "IEND",
                    data = byteArrayOf(),
                ),
            )
            output.toByteArray()
        }
    }

    private fun chunk(
        type: String,
        data: ByteArray,
    ): ByteArray {
        val typeBytes = type.toByteArray(StandardCharsets.US_ASCII)
        val crc =
            CRC32()
                .apply {
                    update(typeBytes)
                    update(data)
                }.value
        return ByteBuffer
            .allocate(12 + data.size)
            .putInt(data.size)
            .put(typeBytes)
            .put(data)
            .putInt(crc.toInt())
            .array()
    }

    private fun String.toAsciiJson(): String =
        buildString {
            this@toAsciiJson.forEach { character ->
                if (character.code in 0x7f..0xffff) {
                    append("\\u")
                    append(
                        character.code.toString(16).padStart(
                            4,
                            '0',
                        ),
                    )
                } else {
                    append(character)
                }
            }
        }

    companion object {
        const val TEXT_KEYWORD = "figmaSyncPayload"
        const val PAYLOAD_SCHEMA_VERSION = 3
        const val MAX_FIGMA_UPLOAD_ASSET_BYTES = 10 * 1024 * 1024

        val PNG_SIGNATURE =
            byteArrayOf(
                -119,
                80,
                78,
                71,
                13,
                10,
                26,
                10,
            )
    }
}
