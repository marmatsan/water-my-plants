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

            test("creates the six CI documentation sections from portable models") {
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
                        "ci.jobTasks",
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
                        CiVisualPlan.Orientation.GRID,
                    )
            }

            test("keeps post-merge jobs compact and connects the canonical model flow") {
                val section =
                    planner
                        .create(
                            topology(),
                            windowsRuntime(),
                            configuration(),
                            visualConfig,
                        ).sections
                        .single { it.target == "ci.postMergeDesignDocumentation" }

                section.nodes
                    .filter { node -> node.type == CiVisualPlan.Type.JOB }
                    .flatMap(CiVisualPlan.Node::phases) shouldBe emptyList()
                section.nodes
                    .filter { node -> node.type == CiVisualPlan.Type.JOB }
                    .flatMap(CiVisualPlan.Node::outcomes) shouldBe emptyList()
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

            test("documents exact post-merge Gradle tasks in the separate job tasks section") {
                val section =
                    planner
                        .create(
                            topology(),
                            windowsRuntime(),
                            configuration(),
                            visualConfig,
                        ).sections
                        .single { it.target == "ci.jobTasks" }

                section.nodes.map(CiVisualPlan.Node::name) shouldContainExactly
                    listOf(
                        "Verify",
                        "Generate main design model",
                        "Check Figma trunk sync",
                    )
                val generateNode = section.nodes.single { it.name == "Generate main design model" }
                generateNode.phases
                    .map(CiVisualPlan.Phase::title) shouldContainExactly
                    listOf(
                        "Validate agent capabilities",
                        "Classify Figma change impact",
                        "Materialize canonical design model",
                        "Build MCP runners and visual plan",
                    )
                val generateSteps = generateNode.phases.flatMap(CiVisualPlan.Phase::steps)
                generateSteps
                    .mapNotNull(CiVisualPlan.Step::technicalId) shouldContainExactly
                    listOf(
                        "classifyCanonicalFigmaSyncChangeImpact",
                        "cleanCanonicalFigmaSyncReports",
                        "materializeFigmaSyncCiConfiguration",
                        "generateCanonicalFigmaSyncModel",
                        "prepareCanonicalFigmaSync",
                        "writeFigmaWriterProjectConfig",
                    )
                generateSteps
                    .filter { step -> step.condition == "Full Figma verification" }
                    .mapNotNull(CiVisualPlan.Step::technicalId) shouldContainExactly
                    listOf(
                        "materializeFigmaSyncCiConfiguration",
                        "generateCanonicalFigmaSyncModel",
                    )
                generateNode.outcomes.map(CiVisualPlan.Outcome::title) shouldContainExactly
                    listOf("Publish build artifact")
                generateNode.outcomes.single().kind shouldBe CiVisualPlan.OutcomeKind.ARTIFACT

                val checkSteps =
                    section.nodes
                        .single { it.name == "Check Figma trunk sync" }
                        .phases
                        .flatMap(CiVisualPlan.Phase::steps)
                checkSteps
                    .mapNotNull(CiVisualPlan.Step::technicalId) shouldContainExactly
                    listOf(
                        "validateCanonicalFigmaSyncScope",
                        "checkCanonicalFigmaTrunkSync",
                    )
                section.connections shouldBe emptyList()
            }

            test("keeps the pull request job compact") {
                val section =
                    planner
                        .create(
                            topology(),
                            windowsRuntime(),
                            configuration(),
                            visualConfig,
                        ).sections
                        .single { it.target == "ci.pullRequestIntegration" }

                section.nodes.single { it.name == "Verify" }.phases shouldBe emptyList()
                section.nodes.single { it.name == "Verify" }.outcomes shouldBe emptyList()
            }

            test("documents the dynamic Gradle verification selection in the separate job tasks section") {
                val section =
                    planner
                        .create(
                            topology(),
                            windowsRuntime(),
                            configuration(),
                            visualConfig,
                        ).sections
                        .single { it.target == "ci.jobTasks" }

                val verifyNode = section.nodes.single { it.name == "Verify" }
                verifyNode.phases
                    .map(CiVisualPlan.Phase::title) shouldContainExactly
                    listOf(
                        "Validate agent capabilities",
                        "Generate verification plan",
                        "Run planned Gradle checks",
                    )
                val steps = verifyNode.phases.flatMap(CiVisualPlan.Phase::steps)
                steps
                    .mapNotNull(CiVisualPlan.Step::technicalId) shouldContainExactly
                    listOf(
                        "prepareTeamCityCiPlan",
                        "generateCiPlan",
                        "ci.plan.gradleTasks",
                        "checkGitWorkflow · checkDocumentation",
                        "checkRepositoryDiff · checkTeamCityDsl · :<affected-module>:check · checkFigmaCatalogUsage",
                        "check",
                    )
                steps.single { step -> step.technicalId == "ci.plan.gradleTasks" }.role shouldBe
                    CiVisualPlan.StepRole.DECISION
                steps
                    .filter { step ->
                        step.role == CiVisualPlan.StepRole.GROUP
                    }.map(CiVisualPlan.Step::title) shouldContainExactly
                    listOf(
                        "Always",
                        "According to changes",
                        "Full verification",
                    )
                verifyNode.outcomes.single().kind shouldBe CiVisualPlan.OutcomeKind.CHECK
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
                                            name = "Validate canonical sync scope",
                                            command = VALIDATE_CANONICAL_FIGMA_SYNC_SCOPE_COMMAND,
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_3",
                                            name = "Verify Figma sync metadata",
                                            command = CHECK_CANONICAL_FIGMA_TRUNK_SYNC_COMMAND,
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
                                                ".\\gradlew.bat classifyCanonicalFigmaSyncChangeImpact " +
                                                    "-PfigmaCanonicalTeamCityPhasedExecution=true",
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_3",
                                            name = "Materialize canonical design model",
                                            command =
                                                ".\\gradlew.bat materializeFigmaSyncCiConfiguration " +
                                                    "generateCanonicalFigmaSyncModel -PfigmaCanonicalTeamCityPhasedExecution=true",
                                        ),
                                        CiJob.Step(
                                            id = "RUNNER_4",
                                            name = "Build MCP runners and visual plan",
                                            command =
                                                ".\\gradlew.bat prepareCanonicalFigmaSync " +
                                                    "-PfigmaCanonicalTeamCityPhasedExecution=true",
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
        canonicalSyncSource = "repo/figma-documentation-sync/docs/runbooks/canonical-artifact-visual-sync.md",
        canonicalDesignModelPath = "build/reports/figma-sync/design-model.json",
    )

private const val VALIDATE_AGENT_CAPABILITIES_COMMAND =
    "powershell.exe -File " +
        ".teamcity\\scripts\\test-agent-capabilities.ps1"
private const val VALIDATE_CANONICAL_FIGMA_SYNC_SCOPE_COMMAND =
    ".\\gradlew.bat validateCanonicalFigmaSyncScope " +
        "-PfigmaCanonicalTeamCityPhasedExecution=true"
private const val CHECK_CANONICAL_FIGMA_TRUNK_SYNC_COMMAND =
    ".\\gradlew.bat checkCanonicalFigmaTrunkSync " +
        "-PfigmaCanonicalTeamCityPhasedExecution=true"
