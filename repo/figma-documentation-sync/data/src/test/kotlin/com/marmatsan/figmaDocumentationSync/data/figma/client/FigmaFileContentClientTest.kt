package com.marmatsan.figmaDocumentationSync.data.figma.client

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.marmatsan.figmaDocumentationSync.data.figma.dto.FigmaNode
import com.marmatsan.figmaDocumentationSync.domain.model.figma.FigmaNodeContent
import com.marmatsan.figmaDocumentationSync.domain.model.figma.FigmaNodeContentError
import com.marmatsan.unitTest.dsl.given
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.IOException

internal class FigmaFileContentClientTest :
    FunSpec(
        {
            test("maps shared plugin data without exposing the wire DTO") {
                given {
                    FigmaFileContentClient { _, _, _, _ ->
                        FigmaNode(
                            id = "metadata-node",
                            name = "Metadata",
                            type = "FRAME",
                            sharedPluginData =
                                mapOf(
                                    "sync" to mapOf("modelHash" to "abc123")
                                )
                        )
                    }
                }.whenever { client ->
                    client.readNodeContent(
                        fileKey = "file-key",
                        token = "token",
                        nodeId = "metadata-node",
                        pluginData = "shared"
                    )
                }.then { result ->
                    result shouldBe
                        Ok(
                            FigmaNodeContent(
                                sharedPluginData =
                                    mapOf(
                                        "sync" to mapOf("modelHash" to "abc123")
                                    )
                            )
                        )
                }
            }

            test("returns a typed error when the requested node is absent") {
                given {
                    FigmaFileContentClient { _, _, _, _ -> null }
                }.whenever { client ->
                    client.readNodeContent(
                        fileKey = "file-key",
                        token = "token",
                        nodeId = "missing-node",
                        pluginData = null
                    )
                }.then { result ->
                    result shouldBe Err(FigmaNodeContentError.NotFound("missing-node"))
                }
            }

            test("returns a typed unavailable error for transport failures") {
                given {
                    FigmaFileContentClient { _, _, _, _ ->
                        throw IOException("connection refused")
                    }
                }.whenever { client ->
                    client.readNodeContent(
                        fileKey = "file-key",
                        token = "token",
                        nodeId = "metadata-node",
                        pluginData = null
                    )
                }.then { result ->
                    result shouldBe Err(FigmaNodeContentError.Unavailable("connection refused"))
                }
            }

            test("returns a typed invalid response error for unexpected decoding failures") {
                given {
                    FigmaFileContentClient { _, _, _, _ ->
                        throw IllegalArgumentException("invalid payload")
                    }
                }.whenever { client ->
                    client.readNodeContent(
                        fileKey = "file-key",
                        token = "token",
                        nodeId = "metadata-node",
                        pluginData = null
                    )
                }.then { result ->
                    result shouldBe Err(FigmaNodeContentError.InvalidResponse)
                }
            }
        }
    )
