package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiJob
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiNode
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

/** Plans canonical model generation, supervised visual synchronization, and post-merge verification. */
internal class PostMergeCiVisualSectionPlanner(
    private val environments: CiVisualEnvironmentResolver,
    private val artifactPaths: CiArtifactPathMatcher,
) : CiVisualSectionPlanner {
    /** Builds the canonical post-merge Figma synchronization section. */
    override fun create(
        context: CiVisualPlanningContext,
    ): CiVisualPlan.Section {
        val config = context.config
        val pipeline = context.figmaPipeline
        val externalById = context.externalTopology.nodes.associateBy(CiNode::id)
        val operator = externalById["operator"]
        val codex = externalById["codex-mcp-client"]
        val figmaDocument = externalById["figma-design-document"]
        val artifactJob =
            pipeline.jobs.find { job ->
                job.artifacts.any { artifact ->
                    artifactPaths.contains(
                        artifact.path,
                        config.canonicalDesignModelPath,
                    )
                }
            }
        val checkJob =
            artifactJob?.let { generated ->
                pipeline.jobs.find { job ->
                    job.dependencies.any { dependency ->
                        dependency.jobId == generated.id &&
                            dependency.artifactPaths.any { path ->
                                artifactPaths.contains(
                                    path,
                                    config.canonicalDesignModelPath,
                                )
                            }
                    }
                }
            }
        val orderedJobs =
            listOfNotNull(
                artifactJob,
                checkJob,
            ) +
                pipeline.jobs.filter { job -> job.id != artifactJob?.id && job.id != checkJob?.id }
        val jobColumns = orderedJobs.mapIndexed { index, job -> job.id to 2 + index * 2 }.toMap()
        val checkColumn = checkJob?.let { jobColumns.getValue(it.id) } ?: 4
        val nodes =
            mutableListOf(
                visualNode(
                    id = "main",
                    type = CiVisualPlan.Type.GIT_REFERENCE,
                    environment = CiVisualPlan.Environment.GITHUB,
                    name = "main",
                    description = "Starts documentation verification after successful CI.",
                    source = config.teamCitySource,
                    row = 0,
                    column = 0,
                    config = config,
                ),
                pipelineNode(
                    "pipeline-${pipeline.id}",
                    pipeline,
                    0,
                    1,
                    config,
                ),
            )
        orderedJobs.forEach { job ->
            val node =
                jobNode(
                    "job-${job.id}",
                    job,
                    0,
                    jobColumns.getValue(job.id),
                    config,
                )
            nodes +=
                if (job.id == checkJob?.id) {
                    node.copy(
                        outcomes = postMergeCheckOutcomes(),
                    )
                } else {
                    node
                }
        }
        artifactJob?.let { job ->
            nodes +=
                visualNode(
                    id = "design-model",
                    type = CiVisualPlan.Type.ARTIFACT,
                    environment = CiVisualPlan.Environment.JSON,
                    name = "design-model.json",
                    description = "Canonical repository snapshot consumed by visual synchronization.",
                    source = config.teamCitySource,
                    row = 0,
                    column = jobColumns.getValue(job.id) + 1,
                    config = config,
                )
        }
        nodes +=
            visualNode(
                id = "rerun-teamcity-figma-sync",
                type = CiVisualPlan.Type.SYSTEM,
                environment = CiVisualPlan.Environment.GRADLE,
                name = "rerunTeamCityFigmaSync",
                description =
                    "Authenticates through Cloudflare, queues the complete Figma Sync pipeline, and waits for success.",
                source = config.canonicalSyncSource,
                row = 1,
                column = 1,
                config = config,
            )
        figmaDocument?.let { node ->
            nodes +=
                externalNode(
                    "figma-document",
                    node,
                    1,
                    checkColumn - 2,
                    config,
                    environments,
                )
        }
        codex?.let { node ->
            nodes +=
                externalNode(
                    "codex",
                    node,
                    1,
                    checkColumn - 1,
                    config,
                    environments,
                )
        }
        operator?.let { node ->
            nodes +=
                externalNode(
                    "operator",
                    node,
                    1,
                    checkColumn,
                    config,
                    environments,
                )
        }

        val connections =
            mutableListOf(
                visualConnection(
                    "main-trigger",
                    "main",
                    "pipeline-${pipeline.id}",
                    triggerLabel(pipeline),
                    CiVisualPlan.ConnectionKind.CONTROL,
                ),
            )
        artifactJob?.let { job ->
            connections +=
                visualConnection(
                    "pipeline-generate",
                    "pipeline-${pipeline.id}",
                    "job-${job.id}",
                    "Run pipeline",
                    CiVisualPlan.ConnectionKind.CONTROL,
                )
            connections +=
                visualConnection(
                    "generate-artifact",
                    "job-${job.id}",
                    "design-model",
                    "Publish artifact",
                    CiVisualPlan.ConnectionKind.DATA,
                )
        }
        checkJob?.let { job ->
            connections +=
                visualConnection(
                    "artifact-check",
                    "design-model",
                    "job-${job.id}",
                    "Compare canonical model",
                    CiVisualPlan.ConnectionKind.DATA,
                )
        }
        addSupervisedSyncConnections(
            connections = connections,
            operator = operator,
            codex = codex,
            figmaDocument = figmaDocument,
            checkJob = checkJob,
            pipelineId = pipeline.id,
        )
        return visualSection(
            target = "ci.postMergeDesignDocumentation",
            name = "Post-merge Design Documentation",
            description = "Canonical model generation, visual synchronization, and verification loop.",
            sources =
                listOf(
                    config.teamCitySource,
                    config.canonicalSyncSource,
                ),
            orientation = CiVisualPlan.Orientation.GRID,
            nodes = nodes,
            connections = connections,
            config = config,
        )
    }

    private fun addSupervisedSyncConnections(
        connections: MutableList<CiVisualPlan.Connection>,
        operator: CiNode?,
        codex: CiNode?,
        figmaDocument: CiNode?,
        checkJob: CiJob?,
        pipelineId: String,
    ) {
        if (figmaDocument != null && checkJob != null) {
            connections +=
                visualConnection(
                    "figma-metadata-check",
                    "figma-document",
                    "job-${checkJob.id}",
                    "Read current metadata",
                    CiVisualPlan.ConnectionKind.DATA,
                )
        }
        if (operator != null && checkJob != null) {
            connections +=
                visualConnection(
                    "check-action",
                    "job-${checkJob.id}",
                    "operator",
                    "Mismatch requires action",
                    CiVisualPlan.ConnectionKind.ATTENTION,
                )
        }
        if (operator != null && codex != null) {
            connections +=
                visualConnection(
                    "operator-codex",
                    "operator",
                    "codex",
                    "Prepare validated handoff",
                    CiVisualPlan.ConnectionKind.CONTROL,
                )
        }
        if (codex != null && figmaDocument != null) {
            connections +=
                visualConnection(
                    "codex-figma",
                    "codex",
                    "figma-document",
                    "Write visuals first · metadata last",
                    CiVisualPlan.ConnectionKind.DATA,
                )
        }
        if (figmaDocument != null) {
            connections +=
                visualConnection(
                    "figma-rerun",
                    "figma-document",
                    "rerun-teamcity-figma-sync",
                    "Run secure rerun",
                    CiVisualPlan.ConnectionKind.CONTROL,
                )
        }
        connections +=
            visualConnection(
                "rerun-pipeline",
                "rerun-teamcity-figma-sync",
                "pipeline-$pipelineId",
                "Queue complete pipeline",
                CiVisualPlan.ConnectionKind.CONTROL,
            )
    }
}
