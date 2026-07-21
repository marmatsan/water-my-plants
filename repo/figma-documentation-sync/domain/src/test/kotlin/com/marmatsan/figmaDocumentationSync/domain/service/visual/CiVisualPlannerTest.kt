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

internal class CiVisualPlannerTest :
    FunSpec(
        {
            val planner = CiVisualPlanner()

            test("creates the five CI documentation sections from portable models") {
                val plan =
                    planner.create(
                        topology(),
                        windowsRuntime(),
                        configuration(),
                        visualConfig,
                    )

                plan.parentName shouldBe "Continuous Integration and Design Documentation"
                plan.sections.map(
                    transform = CiVisualPlan.Section::target,
                ) shouldContainExactly
                    listOf(
                        "ci.overview",
                        "ci.pullRequestIntegration",
                        "ci.postMergeDesignDocumentation",
                        "ci.infrastructureAndAccess",
                        "ci.windowsRuntime",
                    )
                plan.sections.map(
                    transform = CiVisualPlan.Section::orientation,
                ) shouldContainExactly
                    listOf(
                        CiVisualPlan.Orientation.HORIZONTAL,
                        CiVisualPlan.Orientation.HORIZONTAL,
                        CiVisualPlan.Orientation.HORIZONTAL,
                        CiVisualPlan.Orientation.GRID,
                        CiVisualPlan.Orientation.GRID,
                    )
            }

            test("exposes exact post-merge Gradle tasks and connects the official model flow") {
                val section =
                    planner
                        .create(
                            topology(),
                            windowsRuntime(),
                            configuration(),
                            visualConfig,
                        ).sections
                        .single { it.target == "ci.postMergeDesignDocumentation" }

                val generateSteps = section.nodes.single { it.name == "Generate main design model" }.steps
                generateSteps
                    .filter { step -> step.level == CiVisualPlan.StepLevel.PHASE }
                    .map(CiVisualPlan.Step::title) shouldContainExactly
                    listOf(
                        "Validate agent capabilities",
                        "Classify Figma change impact",
                        "Materialize official design model",
                        "Build MCP runners and visual plan",
                        "Publish build artifact",
                    )
                generateSteps
                    .filter { step -> step.level == CiVisualPlan.StepLevel.NESTED }
                    .mapNotNull(CiVisualPlan.Step::technicalId) shouldContainExactly
                    listOf(
                        "classifyOfficialFigmaSyncChangeImpact",
                        "cleanOfficialFigmaSyncReports",
                        "materializeFigmaSyncCiConfiguration",
                        "generateOfficialFigmaSyncModel",
                        "prepareOfficialFigmaSync",
                        "writeFigmaWriterProjectConfig",
                    )
                generateSteps
                    .filter { step -> step.condition == "Full Figma verification" }
                    .mapNotNull(CiVisualPlan.Step::technicalId) shouldContainExactly
                    listOf(
                        "materializeFigmaSyncCiConfiguration",
                        "generateOfficialFigmaSyncModel",
                    )
                generateSteps.last().role shouldBe CiVisualPlan.StepRole.OUTCOME

                val checkSteps = section.nodes.single { it.name == "Check Figma trunk sync" }.steps
                checkSteps
                    .filter { step -> step.level == CiVisualPlan.StepLevel.NESTED }
                    .mapNotNull(CiVisualPlan.Step::technicalId) shouldContainExactly
                    listOf(
                        "validateOfficialFigmaSyncScope",
                        "checkOfficialFigmaTrunkSync",
                    )
                section.connections
                    .filter {
                        it.id in
                            setOf(
                                "pipeline-generate",
                                "generate-artifact",
                                "artifact-check",
                            )
                    }.map { it.source to it.target } shouldContainExactly
                    listOf(
                        "pipeline-Root_FigmaSync" to "job-generate",
                        "job-generate" to "design-model",
                        "design-model" to "job-check",
                    )
                section.connections
                    .map(
                        transform = CiVisualPlan.Connection::label,
                    ).contains("Rerun via HTTPS client") shouldBe true
            }

            test("documents the dynamic Gradle verification selection in the pull request job") {
                val section =
                    planner
                        .create(
                            topology(),
                            windowsRuntime(),
                            configuration(),
                            visualConfig,
                        ).sections
                        .single { it.target == "ci.pullRequestIntegration" }

                val steps = section.nodes.single { it.name == "Verify" }.steps
                steps
                    .filter { step -> step.level == CiVisualPlan.StepLevel.PHASE }
                    .map(CiVisualPlan.Step::title) shouldContainExactly
                    listOf(
                        "Validate agent capabilities",
                        "Generate verification plan",
                        "Run planned Gradle checks",
                        "Publish GitHub check",
                    )
                steps
                    .filter { step -> step.level == CiVisualPlan.StepLevel.NESTED }
                    .mapNotNull(CiVisualPlan.Step::technicalId) shouldContainExactly
                    listOf(
                        "prepareTeamCityCiPlan",
                        "generateCiPlan",
                        "ci.plan.gradleTasks",
                        "checkGitWorkflow",
                        "checkDocumentation",
                        "checkRepositoryDiff",
                        "checkTeamCityDsl",
                        "check",
                        ":<affected-module>:check",
                        "checkFigmaCatalogUsage",
                        "checkFigmaVersionNaming",
                        "checkCiExternalTopologyFreshness",
                        "checkCiWindowsRuntimeFreshness",
                        "checkKotlinStyle",
                        "verification-platform:check",
                    )
                steps.single { step -> step.technicalId == "ci.plan.gradleTasks" }.role shouldBe
                    CiVisualPlan.StepRole.DECISION
                steps.last().role shouldBe CiVisualPlan.StepRole.OUTCOME
            }

            test("does not invent a Figma status when no versioned publisher exists") {
                val overview =
                    planner
                        .create(
                            topology(),
                            windowsRuntime(),
                            configuration(),
                            visualConfig,
                        ).sections
                        .single { it.target == "ci.overview" }

                overview.nodes.any { it.name == "TeamCity Figma Sync" } shouldBe false
                overview.connections.any { it.id == "overview-model-check" } shouldBe false
            }

            test("maps explicit external and Windows environments") {
                planner.externalEnvironment(
                    id = "operator",
                ) shouldBe CiVisualPlan.Environment.OPERATOR
                planner.externalEnvironment(
                    id = "codex-mcp-client",
                ) shouldBe CiVisualPlan.Environment.CODEX
                planner.windowsRuntimeEnvironment(
                    id = "cloudflare-tunnel",
                ) shouldBe CiVisualPlan.Environment.CLOUDFLARE
                shouldThrow<IllegalArgumentException> {
                    planner.externalEnvironment(
                        id = "unknown",
                    )
                }.message shouldContain "no .ci icon environment mapping"
            }

            test("normalizes TeamCity artifact publication paths") {
                planner.artifactPathContains(
                    publishedPath = "build\\reports\\figma-sync\\** => figma-sync",
                    requiredFile = "build/reports/figma-sync/design-model.json",
                ) shouldBe true
                planner.artifactPathContains(
                    publishedPath = "build/reports/unrelated",
                    requiredFile = "build/reports/figma-sync/design-model.json",
                ) shouldBe false
            }
        },
    )

private fun configuration() =
    CiConfiguration(
        pipelines =
            listOf(
                CiPipeline(
                    id = "Root_Ci",
                    name = "CI",
                    triggers =
                        listOf(
                            CiTrigger(
                                type = CiTrigger.Type.Vcs,
                                branchFilter = "+:*",
                                dependencyPipelineId = null,
                                afterSuccessfulBuildOnly = null,
                            ),
                        ),
                    jobs =
                        listOf(
                            job(
                                id = "verify",
                                name = "Verify",
                                steps =
                                    listOf(
                                        CiJob.Step(
                                            id = "RUNNER_1",
                                            name = "Validate agent capabilities",
                                            command = VALIDATE_AGENT_CAPABILITIES_COMMAND,
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_2",
                                            name = "Generate verification plan",
                                            command = ".\\gradlew.bat prepareTeamCityCiPlan --stacktrace",
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_3",
                                            name = "Run planned Gradle checks",
                                            command = "call .\\gradlew.bat %ci.plan.gradleTasks% --stacktrace",
                                        ),
                                    ),
                                checks =
                                    listOf(
                                        CiJob.PublishedCheck(
                                            name = "TeamCity CI",
                                        ),
                                    ),
                            ),
                        ),
                ),
                CiPipeline(
                    id = "Root_FigmaSync",
                    name = "Figma Sync",
                    triggers =
                        listOf(
                            CiTrigger(
                                type = CiTrigger.Type.PipelineFinish,
                                branchFilter = null,
                                dependencyPipelineId = "Root_Ci",
                                afterSuccessfulBuildOnly = true,
                            ),
                        ),
                    jobs =
                        listOf(
                            job(
                                id = "check",
                                name = "Check Figma trunk sync",
                                steps =
                                    listOf(
                                        CiJob.Step(
                                            id = "RUNNER_1",
                                            name = "Validate agent capabilities",
                                            command = VALIDATE_AGENT_CAPABILITIES_COMMAND,
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_2",
                                            name = "Validate official sync scope",
                                            command = VALIDATE_OFFICIAL_FIGMA_SYNC_SCOPE_COMMAND,
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_3",
                                            name = "Verify Figma sync metadata",
                                            command = CHECK_OFFICIAL_FIGMA_TRUNK_SYNC_COMMAND,
                                        ),
                                    ),
                                dependencies =
                                    listOf(
                                        CiJob.Dependency(
                                            jobId = "generate",
                                            artifactPaths = listOf("build/reports/figma-sync"),
                                        ),
                                    ),
                            ),
                            job(
                                id = "generate",
                                name = "Generate main design model",
                                steps =
                                    listOf(
                                        CiJob.Step(
                                            id = "RUNNER_1",
                                            name = "Validate agent capabilities",
                                            command = VALIDATE_AGENT_CAPABILITIES_COMMAND,
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_2",
                                            name = "Classify Figma change impact",
                                            command =
                                                ".\\gradlew.bat classifyOfficialFigmaSyncChangeImpact " +
                                                    "-PfigmaOfficialTeamCityPhasedExecution=true",
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_3",
                                            name = "Materialize official design model",
                                            command =
                                                ".\\gradlew.bat materializeFigmaSyncCiConfiguration " +
                                                    "generateOfficialFigmaSyncModel -PfigmaOfficialTeamCityPhasedExecution=true",
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_4",
                                            name = "Build MCP runners and visual plan",
                                            command =
                                                ".\\gradlew.bat prepareOfficialFigmaSync " +
                                                    "-PfigmaOfficialTeamCityPhasedExecution=true",
                                        ),
                                    ),
                                artifacts =
                                    listOf(
                                        CiJob.Artifact(
                                            "build/reports/figma-sync",
                                            true,
                                            true,
                                        ),
                                    ),
                            ),
                        ),
                ),
            ),
        vcsRoots = emptyList(),
    )

private fun job(
    id: String,
    name: String,
    steps: List<CiJob.Step>,
    artifacts: List<CiJob.Artifact> = emptyList(),
    dependencies: List<CiJob.Dependency> = emptyList(),
    checks: List<CiJob.PublishedCheck> = emptyList(),
) = CiJob(
    id = id,
    name = name,
    steps = steps,
    repositoryIds = emptyList(),
    artifacts = artifacts,
    dependencies = dependencies,
    publishedChecks = checks,
)

private fun topology() =
    CiExternalTopology(
        schemaVersion = 1,
        validation =
            CiExternalTopology.Validation(
                lastValidatedOn =
                    LocalDate.parse(
                        "2026-07-18",
                    ),
                warnAfterDays = 90,
            ),
        nodes =
            listOf(
                CiNode(
                    id = "operator",
                    type = CiNode.Type.Actor,
                    name = "Operator",
                    description = "Starts manual actions.",
                ),
                CiNode(
                    id = "cloudflare-access",
                    type = CiNode.Type.System,
                    name = "Cloudflare Access",
                    description = "Applies access policy.",
                ),
                CiNode(
                    id = "teamcity-server",
                    type = CiNode.Type.System,
                    name = "TeamCity Server",
                    description = "Orchestrates pipelines.",
                ),
                CiNode(
                    id = "codex-mcp-client",
                    type = CiNode.Type.System,
                    name = "Codex/MCP Client",
                    description = "Applies visual changes.",
                ),
                CiNode(
                    id = "figma-design-document",
                    type = CiNode.Type.System,
                    name = "Figma Design Document",
                    description = "Stores visual documentation.",
                ),
            ),
        connections =
            listOf(
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
                    annotation = null,
                ),
            ),
    )

private fun windowsRuntime() =
    CiWindowsRuntime(
        schemaVersion = 1,
        validation =
            CiWindowsRuntime.Validation(
                lastValidatedOn =
                    LocalDate.parse(
                        "2026-07-18",
                    ),
                warnAfterDays = 90,
            ),
        platform = "Windows",
        services =
            listOf(
                CiWindowsRuntime.Service(
                    id = "teamcity-server",
                    name = "TeamCity Server",
                    description = "Hosts TeamCity.",
                    service = "TeamCity",
                    startup = "Automatic",
                    identity = "NT SERVICE\\TeamCity",
                ),
                CiWindowsRuntime.Service(
                    id = "build-agent",
                    name = "Build Agent",
                    description = "Runs builds.",
                    service = "TCBuildAgent",
                    startup = "Automatic",
                    identity = "NT SERVICE\\TCBuildAgent",
                ),
                CiWindowsRuntime.Service(
                    id = "cloudflare-tunnel",
                    name = "Cloudflare Tunnel",
                    description = "Publishes TeamCity.",
                    service = "Cloudflared",
                    startup = "Automatic",
                    identity = "LocalSystem",
                ),
            ),
    )

private val visualConfig =
    CiVisualPlanConfig(
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
        officialDesignModelPath = "build/reports/figma-sync/design-model.json",
    )

private const val VALIDATE_AGENT_CAPABILITIES_COMMAND =
    "powershell.exe -File " +
        ".teamcity\\scripts\\test-agent-capabilities.ps1"
private const val VALIDATE_OFFICIAL_FIGMA_SYNC_SCOPE_COMMAND =
    ".\\gradlew.bat validateOfficialFigmaSyncScope " +
        "-PfigmaOfficialTeamCityPhasedExecution=true"
private const val CHECK_OFFICIAL_FIGMA_TRUNK_SYNC_COMMAND =
    ".\\gradlew.bat checkOfficialFigmaTrunkSync " +
        "-PfigmaOfficialTeamCityPhasedExecution=true"
