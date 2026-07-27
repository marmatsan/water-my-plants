package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

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
