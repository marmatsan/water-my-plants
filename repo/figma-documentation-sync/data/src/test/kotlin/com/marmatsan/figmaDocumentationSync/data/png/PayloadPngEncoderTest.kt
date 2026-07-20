package com.marmatsan.figmaDocumentationSync.data.png

import com.marmatsan.figmaDocumentationSync.domain.model.writer.OfficialSyncPayload
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.imageio.ImageIO

internal class PayloadPngEncoderTest :
    FunSpec(
        {
            test("encodes the official payload as ASCII JSON in a valid PNG text chunk") {
                val encoder = PayloadPngEncoder()
                val payloadJson =
                    encoder.payloadJson(
                        payload =
                            OfficialSyncPayload(
                                payloadSchemaVersion = PayloadPngEncoder.PAYLOAD_SCHEMA_VERSION,
                                designModelJson = "{\"name\":\"Jardín\"}",
                                designModelHash = "sha256:model",
                                designModelGitSha = "abc123",
                                designModelLength = 17,
                                script = "return 1;",
                                scriptLength = 9,
                                writerHash = "sha256:writer",
                                transportHash = "sha256:transport",
                            ),
                    )
                val png = encoder.encode(payloadJson)

                payloadJson.shouldContain("Jard\\u00edn")
                ImageIO.read(ByteArrayInputStream(png)).width shouldBe 1
                decodePayloadText(
                    png = png,
                ) shouldBe payloadJson
            }
        },
    )

private fun decodePayloadText(
    png: ByteArray,
): String {
    var offset = PayloadPngEncoder.PNG_SIGNATURE.size
    while (offset + 12 <= png.size) {
        val length =
            ByteBuffer
                .wrap(
                    png,
                    offset,
                    4,
                ).int
        val type =
            String(
                png,
                offset + 4,
                4,
                StandardCharsets.US_ASCII,
            )
        val dataStart = offset + 8
        if (type == "tEXt") {
            val text =
                String(
                    png,
                    dataStart,
                    length,
                    StandardCharsets.ISO_8859_1,
                )
            val encoded = text.substringAfter("${PayloadPngEncoder.TEXT_KEYWORD}\u0000")
            return String(
                Base64.getDecoder().decode(
                    encoded,
                ),
                StandardCharsets.UTF_8,
            )
        }
        offset = dataStart + length + 4
    }
    error("PNG payload text chunk was not found")
}
