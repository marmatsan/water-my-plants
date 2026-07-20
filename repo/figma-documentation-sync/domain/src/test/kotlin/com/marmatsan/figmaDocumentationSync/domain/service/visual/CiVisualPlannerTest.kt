package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConnection
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiJob
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiNode
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiTrigger
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.time.LocalDate

internal class CiVisualPlannerTest : FunSpec({
    val planner = CiVisualPlanner()

    test("creates the five CI documentation sections from portable models") {
        val plan = planner.create(topology(), windowsRuntime(), configuration(), visualConfig)

        plan.parentName shouldBe "Continuous Integration and Design Documentation"
        plan.sections.map(CiVisualPlan.Section::target) shouldContainExactly listOf(
            "ci.overview",
            "ci.pullRequestIntegration",
            "ci.postMergeDesignDocumentation",
            "ci.infrastructureAndAccess",
            "ci.windowsRuntime"
        )
        plan.sections.map(CiVisualPlan.Section::orientation) shouldContainExactly listOf(
            CiVisualPlan.Orientation.HORIZONTAL,
            CiVisualPlan.Orientation.HORIZONTAL,
            CiVisualPlan.Orientation.HORIZONTAL,
            CiVisualPlan.Orientation.GRID,
            CiVisualPlan.Orientation.GRID
        )
    }

    test("summarizes post-merge jobs and connects the official model flow") {
        val section = planner.create(topology(), windowsRuntime(), configuration(), visualConfig)
            .sections.single { it.target == "ci.postMergeDesignDocumentation" }

        section.nodes.single { it.name == "Generate main design model" }.steps shouldBe
            "Generate effective TeamCity configuration\nGenerate design model"
        section.connections
            .filter { it.id in setOf("pipeline-generate", "generate-artifact", "artifact-check") }
            .map { it.source to it.target } shouldContainExactly listOf(
                "pipeline-Root_FigmaSync" to "job-generate",
                "job-generate" to "design-model",
                "design-model" to "job-check"
            )
        section.connections.map(CiVisualPlan.Connection::label).contains("Rerun via HTTPS client") shouldBe true
    }

    test("does not invent a Figma status when no versioned publisher exists") {
        val overview = planner.create(topology(), windowsRuntime(), configuration(), visualConfig)
            .sections.single { it.target == "ci.overview" }

        overview.nodes.any { it.name == "TeamCity Figma Sync" } shouldBe false
        overview.connections.any { it.id == "overview-model-check" } shouldBe false
    }

    test("maps explicit external and Windows environments") {
        planner.externalEnvironment("operator") shouldBe CiVisualPlan.Environment.OPERATOR
        planner.externalEnvironment("codex-mcp-client") shouldBe CiVisualPlan.Environment.CODEX
        planner.windowsRuntimeEnvironment("cloudflare-tunnel") shouldBe CiVisualPlan.Environment.CLOUDFLARE
        shouldThrow<IllegalArgumentException> { planner.externalEnvironment("unknown") }
            .message shouldContain "no .ci icon environment mapping"
    }

    test("normalizes TeamCity artifact publication paths") {
        planner.artifactPathContains(
            "build\\reports\\figma-sync\\** => figma-sync",
            "build/reports/figma-sync/design-model.json"
        ) shouldBe true
        planner.artifactPathContains(
            "build/reports/unrelated",
            "build/reports/figma-sync/design-model.json"
        ) shouldBe false
    }
})

private fun configuration() = CiConfiguration(
    pipelines = listOf(
        CiPipeline(
            id = "Root_Ci",
            name = "CI",
            triggers = listOf(CiTrigger(CiTrigger.Type.Vcs, "+:*", null, null)),
            jobs = listOf(
                job(
                    id = "verify",
                    name = "Verify",
                    steps = listOf(CiJob.Step("RUNNER_1", "Run Gradle check", ".\\gradlew.bat check")),
                    checks = listOf(CiJob.PublishedCheck("TeamCity CI"))
                )
            )
        ),
        CiPipeline(
            id = "Root_FigmaSync",
            name = "Figma Sync",
            triggers = listOf(CiTrigger(CiTrigger.Type.PipelineFinish, null, "Root_Ci", true)),
            jobs = listOf(
                job(
                    id = "check",
                    name = "Check Figma trunk sync",
                    steps = listOf(CiJob.Step("RUNNER_1", "Check", ".\\gradlew.bat checkFigmaTrunkSync")),
                    dependencies = listOf(CiJob.Dependency("generate", listOf("build/reports/figma-sync")))
                ),
                job(
                    id = "generate",
                    name = "Generate main design model",
                    steps = listOf(
                        CiJob.Step("RUNNER_1", "Generate config", ".\\mvnw.cmd teamcity-configs:generate"),
                        CiJob.Step("RUNNER_2", "Generate model", ".\\gradlew.bat generateFigmaDesignModel")
                    ),
                    artifacts = listOf(CiJob.Artifact("build/reports/figma-sync", true, true))
                )
            )
        )
    ),
    vcsRoots = emptyList()
)

private fun job(
    id: String,
    name: String,
    steps: List<CiJob.Step>,
    artifacts: List<CiJob.Artifact> = emptyList(),
    dependencies: List<CiJob.Dependency> = emptyList(),
    checks: List<CiJob.PublishedCheck> = emptyList()
) = CiJob(id, name, steps, emptyList(), artifacts, dependencies, checks)

private fun topology() = CiExternalTopology(
    schemaVersion = 1,
    validation = CiExternalTopology.Validation(LocalDate.parse("2026-07-18"), 90),
    nodes = listOf(
        CiNode("operator", CiNode.Type.Actor, "Operator", "Starts manual actions."),
        CiNode("cloudflare-access", CiNode.Type.System, "Cloudflare Access", "Applies access policy."),
        CiNode("teamcity-server", CiNode.Type.System, "TeamCity Server", "Orchestrates pipelines."),
        CiNode("codex-mcp-client", CiNode.Type.System, "Codex/MCP Client", "Applies visual changes."),
        CiNode("figma-design-document", CiNode.Type.System, "Figma Design Document", "Stores visual documentation.")
    ),
    connections = listOf(
        CiConnection(
            id = "operator-access",
            sourceNodeId = "operator",
            targetNodeId = "cloudflare-access",
            label = "Open TeamCity",
            description = "Open protected TeamCity.",
            protocol = "HTTPS",
            authentication = emptyList(),
            policy = null,
            path = null,
            automation = CiConnection.Automation.Manual,
            annotation = null
        )
    )
)

private fun windowsRuntime() = CiWindowsRuntime(
    schemaVersion = 1,
    validation = CiWindowsRuntime.Validation(LocalDate.parse("2026-07-18"), 90),
    platform = "Windows",
    services = listOf(
        CiWindowsRuntime.Service("teamcity-server", "TeamCity Server", "Hosts TeamCity.", "TeamCity", "Automatic", "NT SERVICE\\TeamCity"),
        CiWindowsRuntime.Service("build-agent", "Build Agent", "Runs builds.", "TCBuildAgent", "Automatic", "NT SERVICE\\TCBuildAgent"),
        CiWindowsRuntime.Service("cloudflare-tunnel", "Cloudflare Tunnel", "Publishes TeamCity.", "Cloudflared", "Automatic", "LocalSystem")
    )
)

private val visualConfig = CiVisualPlanConfig(
    configurationModelName = "teamCity",
    ciPipelineName = "CI",
    figmaPipelineName = "Figma Sync",
    githubMainBlobUrl = "https://github.com/marmatsan/water-my-plants/blob/main",
    teamCitySource = ".teamcity/settings.kts",
    topologySource = "docs/ci/external-topology.yaml",
    windowsRuntimeSource = "docs/ci/windows-runtime.yaml",
    windowsRuntimeRunbookSource = "docs/runbooks/teamcity-cloudflare-access.md",
    visualContractSource = "docs/ci/visual-model-contract.md",
    branchProtectionSource = "docs/ci/main-branch-protection.md",
    officialSyncSource = "repo/figma-documentation-sync/docs/runbooks/official-artifact-visual-sync.md",
    officialDesignModelPath = "build/reports/figma-sync/design-model.json"
)
