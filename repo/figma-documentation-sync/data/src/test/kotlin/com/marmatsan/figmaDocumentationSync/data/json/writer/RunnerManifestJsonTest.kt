package com.marmatsan.figmaDocumentationSync.data.json.writer

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal class RunnerManifestJsonTest :
    FunSpec(
        {
            test("matches the canonical Node manifest hash contract") {
                val body =
                    Json
                        .parseToJsonElement(
                            """
                            {
                              "schemaVersion": 3,
                              "mode": "official",
                              "files": ["a.mcp.js"],
                              "fileHashes": {"a.mcp.js": "sha256:file"}
                            }
                            """.trimIndent(),
                        ).jsonObject

                RunnerManifestJson().hash(body) shouldBe
                    "sha256:852eab00de37dc1dcc82201d19367aae9ac7dba37e4d90506493ec52c37a91e7"
            }
        },
    )
