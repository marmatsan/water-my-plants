package com.marmatsan.figmaDocumentationSync.data.json.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class CiVisualPlanJsonTest : FunSpec(
    {
    test("adapts the language-neutral design model to one target-scoped Kotlin plan") {
        val designModel = Json.parseToJsonElement(
            """
            {
              "content": {
                "ci": {
                  "externalTopology": {
                    "schemaVersion": 1,
                    "validation": {"lastValidatedOn":"2026-07-18","warnAfterDays":90},
                    "nodes": [
                      {"id":"operator","type":"actor","name":"Operator","description":"Starts manual actions."}
                    ],
                    "connections": []
                  },
                  "windowsRuntime": {
                    "schemaVersion": 1,
                    "validation": {"lastValidatedOn":"2026-07-18","warnAfterDays":90},
                    "platform": "Windows",
                    "services": [
                      {
                        "id":"teamcity-server",
                        "name":"TeamCity Server",
                        "description":"Hosts TeamCity.",
                        "service":"TeamCity",
                        "startup":"Automatic",
                        "identity":"NT SERVICE\\\\TeamCity"
                      }
                    ]
                  },
                  "teamCity": {
                    "vcsRoots": [],
                    "pipelines": [
                      {
                        "id":"Root_Ci",
                        "name":"CI",
                        "triggers":[],
                        "jobs":[{
                          "id":"verify",
                          "name":"Verify",
                          "steps":[],
                          "repositoryIds":[],
                          "artifacts":[],
                          "dependencies":[],
                          "publishedChecks":[{"name":"TeamCity CI"}]
                        }]
                      },
                      {"id":"Root_FigmaSync","name":"Figma Sync","triggers":[],"jobs":[]}
                    ]
                  }
                }
              }
            }
            """.trimIndent()
        ).jsonObject

        val plan = CiVisualPlanJson.create(
            designModel,
            config,
            "ci.windowsRuntime"
        )

        plan["parentName"]?.jsonPrimitive?.content shouldBe
            "Continuous Integration and Design Documentation"
        val sections = plan["sections"]!!.jsonArray
        sections.size shouldBe 1
        sections.single().jsonObject["target"]?.jsonPrimitive?.content shouldBe "ci.windowsRuntime"
        val node = sections.single().jsonObject["nodes"]!!.jsonArray.single().jsonObject
        node["environment"]?.jsonPrimitive?.content shouldBe "teamcity"
    }
}
)

private val config = CiVisualPlanConfig(
    configurationModelName = "teamCity",
    ciPipelineName = "CI",
    figmaPipelineName = "Figma Sync",
    githubMainBlobUrl = "https://example.test/blob/main",
    teamCitySource = ".teamcity/settings.kts",
    topologySource = "docs/ci/external-topology.yaml",
    windowsRuntimeSource = "docs/ci/windows-runtime.yaml",
    windowsRuntimeRunbookSource = "docs/runbooks/teamcity.md",
    visualContractSource = "docs/ci/visual-model-contract.md",
    branchProtectionSource = "docs/ci/main-branch-protection.md",
    officialSyncSource = "docs/runbooks/official-sync.md",
    officialDesignModelPath = "build/reports/figma-sync/design-model.json"
)
