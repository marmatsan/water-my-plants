package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

/** Plans the high-level pull-request and post-merge journey. */
internal class OverviewCiVisualSectionPlanner : CiVisualSectionPlanner {
    override fun create(
        context: CiVisualPlanningContext,
    ): CiVisualPlan.Section {
        val config = context.config
        val ciPipeline = context.ciPipeline
        val figmaPipeline = context.figmaPipeline
        val ciCheck =
            requireNotNull(publishedChecks(ciPipeline).firstOrNull()) {
                "CI pipeline '${ciPipeline.name}' must expose its versioned required status"
            }
        val figmaCheck = publishedChecks(figmaPipeline).firstOrNull()
        val nodes =
            mutableListOf(
                visualNode(
                    id = "overview-pr",
                    type = CiVisualPlan.Type.GIT_REFERENCE,
                    environment = CiVisualPlan.Environment.GITHUB,
                    name = "Pull Request",
                    description = "Proposes a reviewed change to the repository.",
                    source = config.branchProtectionSource,
                    row = 0,
                    column = 0,
                    config = config,
                ),
                pipelineNode(
                    "overview-ci",
                    ciPipeline,
                    1,
                    0,
                    config,
                ),
                visualNode(
                    id = "overview-ci-check",
                    type = CiVisualPlan.Type.CHECK,
                    environment = CiVisualPlan.Environment.TEAMCITY,
                    name = ciCheck,
                    description = "Reports CI verification to GitHub.",
                    source = config.teamCitySource,
                    row = 2,
                    column = 0,
                    config = config,
                ),
                visualNode(
                    id = "overview-gate",
                    type = CiVisualPlan.Type.GATE,
                    environment = CiVisualPlan.Environment.GITHUB,
                    name = "Pull request merge gate",
                    description = "Requires the CI check before merge.",
                    source = config.branchProtectionSource,
                    row = 3,
                    column = 0,
                    config = config,
                ),
                visualNode(
                    id = "overview-main",
                    type = CiVisualPlan.Type.GIT_REFERENCE,
                    environment = CiVisualPlan.Environment.GITHUB,
                    name = "main",
                    description = "Stable trunk after the reviewed merge.",
                    source = config.branchProtectionSource,
                    row = 4,
                    column = 0,
                    config = config,
                ),
                pipelineNode(
                    "overview-figma",
                    figmaPipeline,
                    5,
                    0,
                    config,
                ),
                visualNode(
                    id = "overview-model",
                    type = CiVisualPlan.Type.ARTIFACT,
                    environment = CiVisualPlan.Environment.JSON,
                    name = "design-model.json",
                    description = "Carries the repository documentation snapshot.",
                    source = config.teamCitySource,
                    row = 6,
                    column = 0,
                    config = config,
                ),
            )
        if (figmaCheck != null) {
            nodes +=
                visualNode(
                    id = "overview-figma-check",
                    type = CiVisualPlan.Type.CHECK,
                    environment = CiVisualPlan.Environment.TEAMCITY,
                    name = figmaCheck,
                    description = "Reports whether Figma metadata matches main.",
                    source = config.teamCitySource,
                    row = 7,
                    column = 0,
                    config = config,
                )
        }
        val connections =
            mutableListOf(
                visualConnection(
                    "overview-pr-trigger",
                    "overview-pr",
                    "overview-ci",
                    triggerLabel(ciPipeline),
                    CiVisualPlan.ConnectionKind.CONTROL,
                ),
                visualConnection(
                    "overview-ci-check",
                    "overview-ci",
                    "overview-ci-check",
                    "Publish check",
                    CiVisualPlan.ConnectionKind.STATUS,
                ),
                visualConnection(
                    "overview-check-gate",
                    "overview-ci-check",
                    "overview-gate",
                    "Required check",
                    CiVisualPlan.ConnectionKind.CONTROL,
                ),
                visualConnection(
                    "overview-gate-main",
                    "overview-gate",
                    "overview-main",
                    "Merge",
                    CiVisualPlan.ConnectionKind.CONTROL,
                ),
                visualConnection(
                    "overview-main-figma",
                    "overview-main",
                    "overview-figma",
                    triggerLabel(figmaPipeline),
                    CiVisualPlan.ConnectionKind.CONTROL,
                ),
                visualConnection(
                    "overview-figma-model",
                    "overview-figma",
                    "overview-model",
                    "Generate and publish",
                    CiVisualPlan.ConnectionKind.DATA,
                ),
            )
        if (figmaCheck != null) {
            connections +=
                visualConnection(
                    "overview-model-check",
                    "overview-model",
                    "overview-figma-check",
                    "Verify model hash",
                    CiVisualPlan.ConnectionKind.STATUS,
                )
        }
        return visualSection(
            target = "ci.overview",
            name = "Overview",
            description = "Simplified pull request and post-merge design documentation journeys.",
            sources = listOf(config.visualContractSource),
            orientation = CiVisualPlan.Orientation.HORIZONTAL,
            nodes = nodes,
            connections = connections,
            config = config,
        )
    }
}

/** Plans the detailed pull-request merge-gate flow. */
internal class PullRequestCiVisualSectionPlanner : CiVisualSectionPlanner {
    override fun create(
        context: CiVisualPlanningContext,
    ): CiVisualPlan.Section {
        val config = context.config
        val pipeline = context.ciPipeline
        val nodes =
            mutableListOf(
                visualNode(
                    id = "pr",
                    type = CiVisualPlan.Type.GIT_REFERENCE,
                    environment = CiVisualPlan.Environment.GITHUB,
                    name = "Pull Request",
                    description = "Contains the branch revision proposed for main.",
                    source = config.branchProtectionSource,
                    row = 0,
                    column = 0,
                    config = config,
                ),
                pipelineNode(
                    "pipeline-${pipeline.id}",
                    pipeline,
                    1,
                    0,
                    config,
                ),
            )
        pipeline.jobs.forEachIndexed { index, job ->
            nodes +=
                jobNode(
                    "job-${job.id}",
                    job,
                    2 + index,
                    0,
                    config,
                )
        }
        val checks = publishedChecks(pipeline)
        checks.forEachIndexed { index, check ->
            nodes +=
                visualNode(
                    id = "check-$index",
                    type = CiVisualPlan.Type.CHECK,
                    environment = CiVisualPlan.Environment.TEAMCITY,
                    name = check,
                    description = "Publishes the CI result to GitHub.",
                    source = config.teamCitySource,
                    row = 2 + pipeline.jobs.size,
                    column = index,
                    config = config,
                )
        }
        nodes +=
            visualNode(
                id = "merge-gate",
                type = CiVisualPlan.Type.GATE,
                environment = CiVisualPlan.Environment.GITHUB,
                name = "Pull request merge gate",
                description = "Requires TeamCity CI before merging.",
                source = config.branchProtectionSource,
                row = 3 + pipeline.jobs.size,
                column = 0,
                config = config,
            )
        nodes +=
            visualNode(
                id = "main",
                type = CiVisualPlan.Type.GIT_REFERENCE,
                environment = CiVisualPlan.Environment.GITHUB,
                name = "main",
                description = "Receives the reviewed change after the gate passes.",
                source = config.branchProtectionSource,
                row = 4 + pipeline.jobs.size,
                column = 0,
                config = config,
            )
        val connections =
            mutableListOf(
                visualConnection(
                    "pr-trigger",
                    "pr",
                    "pipeline-${pipeline.id}",
                    triggerLabel(pipeline),
                    CiVisualPlan.ConnectionKind.CONTROL,
                ),
            )
        pipeline.jobs.forEachIndexed { index, job ->
            connections +=
                visualConnection(
                    id = "pipeline-job-${job.id}",
                    source = if (index == 0) "pipeline-${pipeline.id}" else "job-${pipeline.jobs[index - 1].id}",
                    target = "job-${job.id}",
                    label = if (index == 0) "Run pipeline" else "Continue",
                    kind = CiVisualPlan.ConnectionKind.CONTROL,
                )
        }
        checks.forEachIndexed { index, _ ->
            connections +=
                visualConnection(
                    "job-check-$index",
                    pipeline.jobs.lastOrNull()?.let { "job-${it.id}" } ?: "pipeline-${pipeline.id}",
                    "check-$index",
                    "Publish check",
                    CiVisualPlan.ConnectionKind.STATUS,
                )
            connections +=
                visualConnection(
                    "check-gate-$index",
                    "check-$index",
                    "merge-gate",
                    "Required check",
                    CiVisualPlan.ConnectionKind.CONTROL,
                )
        }
        connections +=
            visualConnection(
                "gate-main",
                "merge-gate",
                "main",
                "Merge",
                CiVisualPlan.ConnectionKind.CONTROL,
            )
        return visualSection(
            target = "ci.pullRequestIntegration",
            name = "Pull Request Integration",
            description = "Detailed merge-gate flow derived from the effective CI pipeline.",
            sources =
                listOf(
                    config.teamCitySource,
                    config.branchProtectionSource,
                ),
            orientation = CiVisualPlan.Orientation.HORIZONTAL,
            nodes = nodes,
            connections = connections,
            config = config,
        )
    }
}

/** Plans executable phases and outcomes for every effective CI job. */
internal class JobTasksCiVisualSectionPlanner : CiVisualSectionPlanner {
    override fun create(
        context: CiVisualPlanningContext,
    ): CiVisualPlan.Section {
        val config = context.config
        val nodes =
            listOf(
                context.ciPipeline,
                context.figmaPipeline,
            ).flatMap { pipeline -> jobsInDependencyOrder(pipeline).map { job -> pipeline to job } }
                .filter { (_, job) -> visualPhases(job).isNotEmpty() || visualOutcomes(job).isNotEmpty() }
                .mapIndexed { index, (pipeline, job) ->
                    jobTaskNode(
                        id = "job-tasks-${pipeline.id}-${job.id}",
                        pipeline = pipeline,
                        job = job,
                        row = 0,
                        column = index,
                        config = config,
                    )
                }
        return visualSection(
            target = "ci.jobTasks",
            name = "Job Tasks",
            description = "Ordered TeamCity phases, Gradle tasks, decisions, and outcomes for every CI job.",
            sources =
                listOf(
                    config.teamCitySource,
                    config.visualContractSource,
                ),
            orientation = CiVisualPlan.Orientation.GRID,
            nodes = nodes,
            connections = emptyList(),
            config = config,
        )
    }
}
