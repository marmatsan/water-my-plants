package com.marmatsan.verificationPlatform.data.json

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DocumentationCoverageJsonTest :
    FunSpec(
        {
            test("parses provider-neutral documentation coverage rules") {
                val rules =
                    DocumentationCoverageJson().read(
                        """
                        {
                          "schemaVersion": 4,
                          "rules": [
                            {
                              "id": "verification-platform",
                              "sourcePaths": ["repo/verification-platform/*"],
                              "documentationPaths": ["repo/verification-platform/README.md"]
                            }
                          ]
                        }
                        """.trimIndent()
                    )

                rules.single().id shouldBe "verification-platform"
                rules.single().sourcePaths shouldBe listOf("repo/verification-platform/*")
                rules.single().documentationPaths shouldBe listOf("repo/verification-platform/README.md")
            }
        }
    )
