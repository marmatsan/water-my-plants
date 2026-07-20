package com.marmatsan.ci.data.json

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DocumentationCoverageJsonTest : FunSpec({
    test("parses provider-neutral documentation coverage rules") {
        val rules = DocumentationCoverageJson().read(
            """
            {
              "schemaVersion": 4,
              "rules": [
                {
                  "id": "ci-planner",
                  "sourcePaths": ["repo/ci/*"],
                  "documentationPaths": ["repo/ci/README.md"]
                }
              ]
            }
            """.trimIndent()
        )

        rules.single().id shouldBe "ci-planner"
        rules.single().sourcePaths shouldBe listOf("repo/ci/*")
        rules.single().documentationPaths shouldBe listOf("repo/ci/README.md")
    }
})
