package com.marmatsan.figmaDesignSync.plugin.task.visual

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

internal class GenerateCiVisualPlanGradleTaskTest : FunSpec({
    test("generates a target-scoped Kotlin plan from portable JSON inputs") {
        val project = Files.createTempDirectory("figma-ci-visual-plan-gradle").toFile()
        try {
            project.resolve("settings.gradle.kts").writeText("rootProject.name = \"ci-visual-plan-test\"")
            project.resolve("build.gradle.kts").writeText(
                "plugins { id(\"com.marmatsan.figmaDesignSync\") }"
            )
            val model = project.resolve("design-model.json").apply { writeText(designModelFixture) }
            val config = project.resolve("writer-project-config.json").apply { writeText(writerConfigFixture) }
            val output = project.resolve("build/ci-visual-plan.json")

            val result = GradleRunner.create()
                .withProjectDir(project)
                .withPluginClasspath()
                .withArguments(
                    "generateFigmaCiVisualPlan",
                    "-PfigmaCiVisualDesignModel=${model.absolutePath}",
                    "-PfigmaWriterProjectConfig=${config.absolutePath}",
                    "-PfigmaCiVisualTarget=ci.windowsRuntime",
                    "-PfigmaCiVisualPlanOutput=${output.absolutePath}"
                )
                .build()

            result.task(":generateFigmaCiVisualPlan")?.outcome shouldBe TaskOutcome.SUCCESS
            val plan = Json.parseToJsonElement(output.readText()).jsonObject
            plan.getValue("sections").jsonArray.single().jsonObject
                .getValue("target").jsonPrimitive.content shouldBe "ci.windowsRuntime"
        } finally {
            project.deleteRecursively()
        }
    }
})

private val designModelFixture =
    """
    {
      "content": {
        "ci": {
          "externalTopology": {
            "schemaVersion": 1,
            "validation": {"lastValidatedOn":"2026-07-18","warnAfterDays":90},
            "nodes": [],
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
              {"id":"Root_Ci","name":"CI","triggers":[],"jobs":[]},
              {"id":"Root_FigmaSync","name":"Figma Sync","triggers":[],"jobs":[]}
            ]
          }
        }
      }
    }
    """.trimIndent()

private val writerConfigFixture =
    """
    {
      "schemaVersion": 1,
      "METADATA_PAGE_ID": "1:1",
      "METADATA_NAMESPACE": "test",
      "FIGMA_FILE_KEY": "file",
      "PROJECT_DISPLAY_NAME": "Test",
      "MCP_CLIENT_NAME": "test",
      "REPOSITORY_ROOT_RELATIVE_TO_TOOLS": "../../..",
      "CHANGE_IMPACT_POLICY_RELATIVE_TO_REPOSITORY": "policy.json",
      "WRITER_TARGET_NAMES": ["ci.windowsRuntime"],
      "CATALOG_TARGET_NAMES": [],
      "CI_VISUAL_TARGET_NAMES": ["ci.windowsRuntime"],
      "CI_CONFIGURATION_MODEL_NAME": "teamCity",
      "CI_PIPELINE_NAME": "CI",
      "FIGMA_PIPELINE_NAME": "Figma Sync",
      "GITHUB_MAIN_BLOB_URL": "https://example.test/blob/main",
      "TEAMCITY_SOURCE": ".teamcity/settings.kts",
      "TOPOLOGY_SOURCE": "docs/ci/external-topology.yaml",
      "WINDOWS_RUNTIME_SOURCE": "docs/ci/windows-runtime.yaml",
      "WINDOWS_RUNTIME_RUNBOOK_SOURCE": "docs/runbooks/teamcity.md",
      "VISUAL_CONTRACT_SOURCE": "docs/ci/visual-model-contract.md",
      "BRANCH_PROTECTION_SOURCE": "docs/ci/main-branch-protection.md",
      "OFFICIAL_SYNC_SOURCE": "docs/runbooks/official-sync.md",
      "OFFICIAL_DESIGN_MODEL_PATH": "build/reports/figma-sync/design-model.json"
    }
    """.trimIndent()
