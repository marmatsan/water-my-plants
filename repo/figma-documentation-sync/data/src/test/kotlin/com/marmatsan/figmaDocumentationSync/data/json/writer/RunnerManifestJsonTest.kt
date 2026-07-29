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
                              "schemaVersion": 4,
                              "mode": "canonical",
                              "files": ["a.mcp.js"],
                              "fileHashes": {"a.mcp.js": "sha256:file"}
                            }
                            """.trimIndent()
                        ).jsonObject

                RunnerManifestJson().hash(body) shouldBe
                    "sha256:978f117b2d11ed29454dc0c23b419c430d59a33ef933f084f86ac17161315393"
            }
        }
    )
