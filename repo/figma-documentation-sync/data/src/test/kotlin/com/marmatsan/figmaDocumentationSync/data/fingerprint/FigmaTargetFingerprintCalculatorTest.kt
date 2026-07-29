package com.marmatsan.figmaDocumentationSync.data.fingerprint

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal class FigmaTargetFingerprintCalculatorTest :
    FunSpec(
        {
            test("matches the scoped target hashes produced by the Node writer") {
                val model =
                    Json
                        .parseToJsonElement(
                            """
                            {
                              "content": {
                                "versions": {"k": "1"},
                                "versionSections": [],
                                "catalogs": {
                                  "waterMyPlants": {
                                    "libraries": [
                                      {"group": "androidx", "name": "core"},
                                      {"group": "square", "name": "retrofit"}
                                    ]
                                  }
                                },
                                "ci": {"pipelines": []}
                              }
                            }
                            """.trimIndent()
                        ).jsonObject

                val result =
                    FigmaTargetFingerprintCalculator().create(
                        designModel = model,
                        visualTargets =
                            listOf(
                                "preflight",
                                "headers",
                                "versions",
                                "waterMyPlants.libraries",
                                "ci.overview"
                            ),
                        catalogTargets = listOf("waterMyPlants.libraries")
                    )

                result["preflight"] shouldBe
                    "sha256:b74fddde67426b9eba32064b444faa40dc38224b7bb90a6b6a922457a678ef3c"
                result["headers"] shouldBe
                    "sha256:ab03213ce57125d63bcba163b79b952e58e96068eb53e41304953c0bea6857a8"
                result["versions"] shouldBe
                    "sha256:616e69390f8350e65efe1b64656be1cee9514c3c93770e599f22816bf8cc8672"
                result["waterMyPlants.libraries"] shouldBe
                    "sha256:fdceec06c73df8485e771a8c777ff4d7a6981073b24ec3b949d3b49d677a2e56"
                result["waterMyPlants.libraries.androidx"] shouldBe
                    "sha256:62c599d033ef543ee1af74e78cefe3637fb99fe9bdb1e8c131200a1ca3f756c3"
                result["waterMyPlants.libraries.cleanup"] shouldBe
                    "sha256:f378ec330bdc61b1d8acb883333b3117d848d5aae5eced982d71681ed46bed4e"
                result["ci.overview"] shouldBe
                    "sha256:f6cddc759e841bf1520301259892b223587f389b9d85795bcae9f02be832122d"
            }
        }
    )
