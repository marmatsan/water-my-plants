package com.marmatsan.figmaDocumentationSync.data.mcp

import com.marmatsan.figmaDocumentationSync.data.png.PayloadPngEncoder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class KtorFigmaPngAssetUploaderTest :
    FunSpec(
        {
            val uploadUrl =
                "https://mcp.figma.com/mcp/upload/0d188fd2-0c70-46f5-b30a-6dd4f3904998/submit?scaleMode=FILL"
            val png = PayloadPngEncoder().encode("{}")

            test("uploads a PNG only to the exact Figma MCP submit endpoint") {
                var capturedUrl = ""
                var capturedBytes = byteArrayOf()
                val uploader =
                    KtorFigmaPngAssetUploader { target, bytes ->
                        capturedUrl = target.toString()
                        capturedBytes = bytes
                        204
                    }

                uploader.uploadBlocking(
                    url = uploadUrl,
                    bytes = png
                )

                capturedUrl shouldBe uploadUrl
                capturedBytes shouldBe png
            }

            test("rejects upload targets outside the single-use Figma MCP contract") {
                val invalidUrls =
                    listOf(
                        uploadUrl.replace(
                            "https://",
                            "http://"
                        ),
                        uploadUrl.replace(
                            "mcp.figma.com",
                            "example.com"
                        ),
                        uploadUrl.replace(
                            "mcp.figma.com",
                            "mcp.figma.com.example.com"
                        ),
                        uploadUrl.replace(
                            "mcp.figma.com",
                            "mcp.figma.com:8443"
                        ),
                        uploadUrl.replace(
                            "/mcp/upload/",
                            "/other/upload/"
                        ),
                        uploadUrl.replace(
                            "?scaleMode=FILL",
                            "?scaleMode=FIT"
                        ),
                        uploadUrl.replace(
                            "https://",
                            "https://user@mcp.figma.com/"
                        )
                    )
                var sends = 0
                val uploader =
                    KtorFigmaPngAssetUploader { _, _ ->
                        sends += 1
                        204
                    }

                invalidUrls.forEach { url ->
                    shouldThrow<IllegalArgumentException> {
                        uploader.uploadBlocking(
                            url = url,
                            bytes = png
                        )
                    }
                }

                sends shouldBe 0
            }

            test("rejects non-PNG content and oversized payloads before sending") {
                var sends = 0
                val uploader =
                    KtorFigmaPngAssetUploader { _, _ ->
                        sends += 1
                        204
                    }

                shouldThrow<IllegalArgumentException> {
                    uploader.uploadBlocking(
                        url = uploadUrl,
                        bytes = "not-a-png".encodeToByteArray()
                    )
                }
                shouldThrow<IllegalArgumentException> {
                    uploader.uploadBlocking(
                        url = uploadUrl,
                        bytes = ByteArray(10 * 1024 * 1024 + 1)
                    )
                }

                sends shouldBe 0
            }

            test("reports a failed Figma upload status") {
                val uploader = KtorFigmaPngAssetUploader { _, _ -> 500 }

                val failure =
                    shouldThrow<IllegalArgumentException> {
                        uploader.uploadBlocking(
                            url = uploadUrl,
                            bytes = png
                        )
                    }

                failure.message shouldBe "Payload upload failed with HTTP 500."
            }
        }
    )
