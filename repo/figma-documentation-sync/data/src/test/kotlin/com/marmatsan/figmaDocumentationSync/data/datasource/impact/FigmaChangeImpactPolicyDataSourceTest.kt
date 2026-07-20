package com.marmatsan.figmaDocumentationSync.data.datasource.impact

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files

internal class FigmaChangeImpactPolicyDataSourceTest :
    FunSpec(
        {
            test("read maps the versioned JSON policy to domain rules") {
                val policyFile =
                    Files
                        .createTempFile(
                            "figma-impact-policy",
                            ".json",
                        ).toFile()
                try {
                    policyFile.writeText(
                        """
                        {
                          "schemaVersion": 1,
                          "documentationOnlyPaths": ["docs/*.md"],
                          "figmaTransportOnlyPaths": ["tools/scripts/*"],
                          "figmaModelNeutralPaths": ["ci/tests/*"],
                          "figmaModelContentPaths": ["*/build.gradle.kts"],
                          "figmaVisualWriterPaths": ["tools/src/*"],
                          "figmaVisualTargetRules": [
                            {
                              "paths": ["tools/src/version-*"],
                              "targets": ["versions"]
                            }
                          ]
                        }
                        """.trimIndent(),
                    )

                    val policy = FigmaChangeImpactPolicyDataSource().read(policyFile.absolutePath)

                    policy.documentationOnlyPaths shouldBe listOf("docs/*.md")
                    policy.transportOnlyPaths shouldBe listOf("tools/scripts/*")
                    policy.modelNeutralPaths shouldBe listOf("ci/tests/*")
                    policy.modelContentPaths shouldBe listOf("*/build.gradle.kts")
                    policy.visualWriterPaths shouldBe listOf("tools/src/*")
                    policy.visualTargetRules.single().targets shouldBe listOf("versions")
                } finally {
                    policyFile.delete()
                }
            }

            test("read rejects an unsupported policy schema") {
                val policyFile =
                    Files
                        .createTempFile(
                            "figma-impact-policy",
                            ".json",
                        ).toFile()
                try {
                    policyFile.writeText(
                        """
                        {
                          "schemaVersion": 2,
                          "documentationOnlyPaths": [],
                          "figmaTransportOnlyPaths": [],
                          "figmaModelNeutralPaths": [],
                          "figmaModelContentPaths": [],
                          "figmaVisualWriterPaths": [],
                          "figmaVisualTargetRules": []
                        }
                        """.trimIndent(),
                    )

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            FigmaChangeImpactPolicyDataSource().read(policyFile.absolutePath)
                        }

                    error.message shouldBe "Unsupported Figma change-impact policy schema 2; expected 1"
                } finally {
                    policyFile.delete()
                }
            }
        },
    )
