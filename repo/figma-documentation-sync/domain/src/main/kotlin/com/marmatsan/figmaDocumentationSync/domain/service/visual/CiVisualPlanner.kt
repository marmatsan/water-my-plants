package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiJob
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiNode
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiTrigger
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig

/** Converts portable CI models into a deterministic visual plan without using the Figma API. */
class CiVisualPlanner {
    fun create(
        externalTopology: CiExternalTopology,
        windowsRuntime: CiWindowsRuntime,
        configuration: CiConfiguration,
        config: CiVisualPlanConfig,
    ): CiVisualPlan {
        val ciPipeline =
            requirePipeline(
                configuration = configuration,
                name = config.ciPipelineName,
            )
        val figmaPipeline =
            requirePipeline(
                configuration = configuration,
                name = config.figmaPipelineName,
            )
        return CiVisualPlan(
            parentName = "Continuous Integration and Design Documentation",
            sections =
                listOf(
                    createOverviewSection(
                        ciPipeline = ciPipeline,
                        figmaPipeline = figmaPipeline,
                        config = config,
                    ),
                    createPullRequestSection(
                        pipeline = ciPipeline,
                        config = config,
                    ),
                    createPostMergeSection(
                        topology = externalTopology,
                        pipeline = figmaPipeline,
                        config = config,
                    ),
                    createJobTasksSection(
                        ciPipeline = ciPipeline,
                        figmaPipeline = figmaPipeline,
                        config = config,
                    ),
                    createInfrastructureSection(
                        topology = externalTopology,
                        config = config,
                    ),
                    createWindowsRuntimeSection(
                        runtime = windowsRuntime,
                        config = config,
                    ),
                ),
        )
    }

    private fun createJobTasksSection(
        ciPipeline: CiPipeline,
        figmaPipeline: CiPipeline,
        config: CiVisualPlanConfig,
    ): CiVisualPlan.Section {
        val nodes =
            listOf(
                ciPipeline,
                figmaPipeline,
            ).flatMap { pipeline ->
                jobsInDependencyOrder(
                    pipeline = pipeline,
                ).map { job -> pipeline to job }
            }.filter { (_, job) ->
                visualPhases(
                    job = job,
                ).isNotEmpty() ||
                    visualOutcomes(
                        job = job,
                    ).isNotEmpty()
            }.mapIndexed { index, (pipeline, job) ->
                jobTaskNode(
                    id = "job-tasks-${pipeline.id}-${job.id}",
                    pipeline = pipeline,
                    job = job,
                    row = 0,
                    column = index,
                    config = config,
                )
            }
        return section(
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

    private fun jobsInDependencyOrder(
        pipeline: CiPipeline,
    ): List<CiJob> {
        val remaining = pipeline.jobs.toMutableList()
        val ordered = mutableListOf<CiJob>()
        val pipelineJobIds = pipeline.jobs.map(CiJob::id).toSet()
        while (remaining.isNotEmpty()) {
            val completedIds = ordered.map(CiJob::id).toSet()
            val next =
                remaining.firstOrNull { job ->
                    job.dependencies
                        .map(CiJob.Dependency::jobId)
                        .filter { dependencyId -> dependencyId in pipelineJobIds }
                        .all { dependencyId -> dependencyId in completedIds }
                } ?: return pipeline.jobs
            ordered += next
            remaining -= next
        }
        return ordered
    }

    private fun createOverviewSection(
        ciPipeline: CiPipeline,
        figmaPipeline: CiPipeline,
        config: CiVisualPlanConfig,
    ): CiVisualPlan.Section {
        val ciCheck =
            requireNotNull(
                publishedChecks(
                    pipeline = ciPipeline,
                ).firstOrNull(),
            ) {
                "CI pipeline '${ciPipeline.name}' must expose its versioned required status"
            }
        val figmaCheck =
            publishedChecks(
                pipeline = figmaPipeline,
            ).firstOrNull()
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
                    id = "overview-ci",
                    pipeline = ciPipeline,
                    row = 1,
                    column = 0,
                    config = config,
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
                    id = "overview-figma",
                    pipeline = figmaPipeline,
                    row = 5,
                    column = 0,
                    config = config,
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
                connection(
                    id = "overview-pr-trigger",
                    source = "overview-pr",
                    target = "overview-ci",
                    label =
                        triggerLabel(
                            pipeline = ciPipeline,
                        ),
                ),
                connection(
                    id = "overview-ci-check",
                    source = "overview-ci",
                    target = "overview-ci-check",
                    label = "Publish check",
                ),
                connection(
                    id = "overview-check-gate",
                    source = "overview-ci-check",
                    target = "overview-gate",
                    label = "Required check",
                ),
                connection(
                    id = "overview-gate-main",
                    source = "overview-gate",
                    target = "overview-main",
                    label = "Merge",
                ),
                connection(
                    id = "overview-main-figma",
                    source = "overview-main",
                    target = "overview-figma",
                    label =
                        triggerLabel(
                            pipeline = figmaPipeline,
                        ),
                ),
                connection(
                    id = "overview-figma-model",
                    source = "overview-figma",
                    target = "overview-model",
                    label = "Generate and publish",
                ),
            )
        if (figmaCheck != null) {
            connections +=
                connection(
                    id = "overview-model-check",
                    source = "overview-model",
                    target = "overview-figma-check",
                    label = "Verify model hash",
                )
        }
        return section(
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

    private fun createPullRequestSection(
        pipeline: CiPipeline,
        config: CiVisualPlanConfig,
    ): CiVisualPlan.Section {
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
                    id = "pipeline-${pipeline.id}",
                    pipeline = pipeline,
                    row = 1,
                    column = 0,
                    config = config,
                ),
            )
        pipeline.jobs.forEachIndexed { index, job ->
            nodes +=
                jobNode(
                    id = "job-${job.id}",
                    job = job,
                    row = 2 + index,
                    column = 0,
                    config = config,
                )
        }
        val checks =
            publishedChecks(
                pipeline = pipeline,
            )
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
                connection(
                    id = "pr-trigger",
                    source = "pr",
                    target = "pipeline-${pipeline.id}",
                    label =
                        triggerLabel(
                            pipeline = pipeline,
                        ),
                ),
            )
        pipeline.jobs.forEachIndexed { index, job ->
            connections +=
                connection(
                    id = "pipeline-job-${job.id}",
                    source = if (index == 0) "pipeline-${pipeline.id}" else "job-${pipeline.jobs[index - 1].id}",
                    target = "job-${job.id}",
                    label = if (index == 0) "Run pipeline" else "Continue",
                )
        }
        checks.forEachIndexed { index, _ ->
            connections +=
                connection(
                    id = "job-check-$index",
                    source = pipeline.jobs.lastOrNull()?.let { "job-${it.id}" } ?: "pipeline-${pipeline.id}",
                    target = "check-$index",
                    label = "Publish check",
                )
            connections +=
                connection(
                    id = "check-gate-$index",
                    source = "check-$index",
                    target = "merge-gate",
                    label = "Required check",
                )
        }
        connections +=
            connection(
                id = "gate-main",
                source = "merge-gate",
                target = "main",
                label = "Merge",
            )
        return section(
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

    private fun createPostMergeSection(
        topology: CiExternalTopology,
        pipeline: CiPipeline,
        config: CiVisualPlanConfig,
    ): CiVisualPlan.Section {
        val externalById = topology.nodes.associateBy(CiNode::id)
        val operator = externalById["operator"]
        val codex = externalById["codex-mcp-client"]
        val figmaDocument = externalById["figma-design-document"]
        val artifactJob =
            pipeline.jobs.find { job ->
                job.artifacts.any { artifact ->
                    artifactPathContains(
                        publishedPath = artifact.path,
                        requiredFile = config.canonicalDesignModelPath,
                    )
                }
            }
        val checkJob =
            artifactJob?.let { generated ->
                pipeline.jobs.find { job ->
                    job.dependencies.any { dependency ->
                        dependency.jobId == generated.id &&
                            dependency.artifactPaths.any { path ->
                                artifactPathContains(
                                    publishedPath = path,
                                    requiredFile = config.canonicalDesignModelPath,
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
                pipeline.jobs.filter { job ->
                    job.id != artifactJob?.id && job.id != checkJob?.id
                }
        val jobRows = orderedJobs.mapIndexed { index, job -> job.id to 2 + index * 2 }.toMap()
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
                    id = "pipeline-${pipeline.id}",
                    pipeline = pipeline,
                    row = 1,
                    column = 0,
                    config = config,
                ),
            )
        pipeline.jobs.forEachIndexed { index, job ->
            nodes +=
                jobNode(
                    id = "job-${job.id}",
                    job = job,
                    row = jobRows[job.id] ?: 2 + index * 2,
                    column = 0,
                    config = config,
                )
        }
        val artifactRow = artifactJob?.let { (jobRows[it.id] ?: 2) + 1 } ?: 2
        nodes +=
            visualNode(
                id = "design-model",
                type = CiVisualPlan.Type.ARTIFACT,
                environment = CiVisualPlan.Environment.JSON,
                name = "design-model.json",
                description = "Canonical repository snapshot consumed by visual synchronization.",
                source = config.teamCitySource,
                row = artifactRow,
                column = 0,
                config = config,
            )
        val checkRow =
            maxOf(
                2 + pipeline.jobs.size * 2,
                nodes.maxOf { node -> node.row + 1 },
            )
        val checks =
            publishedChecks(
                pipeline = pipeline,
            )
        checks.forEachIndexed { index, check ->
            nodes +=
                visualNode(
                    id = "figma-check-$index",
                    type = CiVisualPlan.Type.CHECK,
                    environment = CiVisualPlan.Environment.TEAMCITY,
                    name = check,
                    description = "Publishes the post-merge documentation result.",
                    source = config.teamCitySource,
                    row = checkRow,
                    column = index,
                    config = config,
                )
        }
        operator?.let {
            nodes +=
                externalNode(
                    id = "operator",
                    node = it,
                    row = checkRow + 1,
                    column = 0,
                    config = config,
                )
        }
        codex?.let {
            nodes +=
                externalNode(
                    id = "codex",
                    node = it,
                    row = checkRow + 2,
                    column = 0,
                    config = config,
                )
        }
        figmaDocument?.let {
            nodes +=
                externalNode(
                    id = "figma-document",
                    node = it,
                    row = checkRow + 3,
                    column = 0,
                    config = config,
                )
        }

        val connections =
            mutableListOf(
                connection(
                    id = "main-trigger",
                    source = "main",
                    target = "pipeline-${pipeline.id}",
                    label =
                        triggerLabel(
                            pipeline = pipeline,
                        ),
                ),
            )
        artifactJob?.let {
            connections +=
                connection(
                    id = "pipeline-generate",
                    source = "pipeline-${pipeline.id}",
                    target = "job-${it.id}",
                    label = "Run pipeline",
                )
            connections +=
                connection(
                    id = "generate-artifact",
                    source = "job-${it.id}",
                    target = "design-model",
                    label = "Publish artifact",
                )
        }
        checkJob?.let {
            connections +=
                connection(
                    id = "artifact-check",
                    source = "design-model",
                    target = "job-${it.id}",
                    label = "Consume artifact",
                )
        }
        checks.forEachIndexed { index, _ ->
            connections +=
                connection(
                    id = "check-status-$index",
                    source = checkJob?.let { "job-${it.id}" } ?: "pipeline-${pipeline.id}",
                    target = "figma-check-$index",
                    label = "Publish status",
                )
        }
        if (operator != null && checks.isNotEmpty()) {
            connections +=
                connection(
                    id = "mismatch-operator",
                    source = "figma-check-0",
                    target = "operator",
                    label = "Mismatch requires action",
                )
        }
        if (operator != null && codex != null) {
            connections +=
                connection(
                    id = "operator-codex",
                    source = "operator",
                    target = "codex",
                    label = "Request visual synchronization",
                )
        }
        if (codex != null && figmaDocument != null) {
            connections +=
                connection(
                    id = "codex-figma",
                    source = "codex",
                    target = "figma-document",
                    label = "Apply visual changes",
                )
        }
        if (figmaDocument != null && checkJob != null) {
            connections +=
                connection(
                    id = "rerun",
                    source = "figma-document",
                    target = "job-${checkJob.id}",
                    label = "Rerun via HTTPS client",
                )
        }
        return section(
            target = "ci.postMergeDesignDocumentation",
            name = "Post-merge Design Documentation",
            description = "Canonical model generation, visual synchronization, and verification loop.",
            sources =
                listOf(
                    config.teamCitySource,
                    config.canonicalSyncSource,
                ),
            orientation = CiVisualPlan.Orientation.HORIZONTAL,
            nodes = nodes,
            connections = connections,
            config = config,
        )
    }

    private fun createInfrastructureSection(
        topology: CiExternalTopology,
        config: CiVisualPlanConfig,
    ): CiVisualPlan.Section {
        val placement = infrastructurePlacement()
        val nodes =
            topology.nodes.mapIndexed { index, node ->
                val position =
                    placement[node.id] ?: Position(
                        row = index,
                        column = 0,
                    )
                externalNode(
                    id = "external-${node.id}",
                    node = node,
                    row = position.row,
                    column = position.column,
                    config = config,
                )
            }
        val nodeIds = topology.nodes.associate { node -> node.id to "external-${node.id}" }
        val connections =
            topology.connections.map { edge ->
                connection(
                    id = "external-${edge.id}",
                    source = nodeIds[edge.sourceNodeId],
                    target = nodeIds[edge.targetNodeId],
                    label = edge.label,
                )
            }
        return section(
            target = "ci.infrastructureAndAccess",
            name = "Infrastructure and Access",
            description = "External systems, trust boundaries, authentication paths, and automation modes.",
            sources =
                listOf(
                    config.topologySource,
                    "docs/ci/external-topology-validation.md",
                ),
            orientation = CiVisualPlan.Orientation.GRID,
            nodes = nodes,
            connections = connections,
            config = config,
        )
    }

    private fun createWindowsRuntimeSection(
        runtime: CiWindowsRuntime,
        config: CiVisualPlanConfig,
    ): CiVisualPlan.Section {
        val nodes =
            runtime.services.mapIndexed { index, service ->
                visualNode(
                    id = "windows-runtime-${service.id}",
                    type = CiVisualPlan.Type.SYSTEM,
                    environment =
                        windowsRuntimeEnvironment(
                            id = service.id,
                        ),
                    name = service.name,
                    description = service.description,
                    source = config.windowsRuntimeSource,
                    row = 0,
                    column = index,
                    config = config,
                ).copy(
                    runtime =
                        CiVisualPlan.Runtime(
                            platform = runtime.platform,
                            service = service.service,
                            startup = service.startup,
                            identity = service.identity,
                        ),
                )
            }
        return section(
            target = "ci.windowsRuntime",
            name = "Windows Service Runtime",
            description = "Versioned inventory of the Windows services that host the local CI runtime.",
            sources =
                listOf(
                    config.windowsRuntimeSource,
                    config.windowsRuntimeRunbookSource,
                ),
            orientation = CiVisualPlan.Orientation.GRID,
            nodes = nodes,
            connections = emptyList(),
            config = config,
        )
    }

    private fun section(
        target: String,
        name: String,
        description: String,
        sources: List<String>,
        orientation: CiVisualPlan.Orientation,
        nodes: List<CiVisualPlan.Node>,
        connections: List<CiVisualPlan.Connection>,
        config: CiVisualPlanConfig,
    ) = CiVisualPlan.Section(
        target = target,
        name = name,
        description = description,
        orientation = orientation,
        headerSources =
            sources.map { source ->
                CiVisualPlan.HeaderSource(
                    label = source,
                    url =
                        sourceUrl(
                            source = source,
                            config = config,
                        ),
                )
            },
        nodes = nodes,
        connections = connections,
    )

    private fun pipelineNode(
        id: String,
        pipeline: CiPipeline,
        row: Int,
        column: Int,
        config: CiVisualPlanConfig,
    ) =
        visualNode(
            id = id,
            type = CiVisualPlan.Type.PIPELINE,
            environment = CiVisualPlan.Environment.TEAMCITY,
            name = pipeline.name,
            description =
                pipelineDescription(
                    name = pipeline.name,
                    config = config,
                ),
            source = config.teamCitySource,
            row = row,
            column = column,
            config = config,
        )

    private fun jobNode(
        id: String,
        job: CiJob,
        row: Int,
        column: Int,
        config: CiVisualPlanConfig,
    ) =
        visualNode(
            id = id,
            type = CiVisualPlan.Type.JOB,
            environment = CiVisualPlan.Environment.TEAMCITY,
            name = job.name,
            description =
                jobDescription(
                    name = job.name,
                ),
            source = config.teamCitySource,
            row = row,
            column = column,
            config = config,
        )

    private fun jobTaskNode(
        id: String,
        pipeline: CiPipeline,
        job: CiJob,
        row: Int,
        column: Int,
        config: CiVisualPlanConfig,
    ): CiVisualPlan.Node {
        val jobDescription =
            jobDescription(
                name = job.name,
            )
        return visualNode(
            id = id,
            type = CiVisualPlan.Type.JOB,
            environment = CiVisualPlan.Environment.TEAMCITY,
            name = job.name,
            description = "$jobDescription TeamCity pipeline: ${pipeline.name}.",
            source = config.teamCitySource,
            row = row,
            column = column,
            config = config,
        ).copy(
            phases =
                visualPhases(
                    job = job,
                ),
            outcomes =
                visualOutcomes(
                    job = job,
                ),
        )
    }

    private fun externalNode(
        id: String,
        node: CiNode,
        row: Int,
        column: Int,
        config: CiVisualPlanConfig,
    ) =
        visualNode(
            id = id,
            type = node.type.toVisualType(),
            environment =
                externalEnvironment(
                    id = node.id,
                ),
            name = node.name,
            description = node.description,
            source = config.topologySource,
            row = row,
            column = column,
            config = config,
        )

    private fun visualNode(
        id: String,
        type: CiVisualPlan.Type,
        environment: CiVisualPlan.Environment,
        name: String,
        description: String,
        source: String,
        row: Int,
        column: Int,
        config: CiVisualPlanConfig,
    ) = CiVisualPlan.Node(
        id = id,
        type = type,
        environment = environment,
        name = name,
        description = description,
        phases = emptyList(),
        outcomes = emptyList(),
        runtime = null,
        source = source,
        sourceUrl =
            sourceUrl(
                source = source,
                config = config,
            ),
        row = row,
        column = column,
    )

    private fun connection(
        id: String,
        source: String?,
        target: String?,
        label: String,
    ): CiVisualPlan.Connection {
        require(source != null && target != null) { "CI visual connection '$id' has an unknown endpoint." }
        return CiVisualPlan.Connection(
            id = id,
            source = source,
            target = target,
            label = label,
        )
    }

    private fun publishedChecks(
        pipeline: CiPipeline,
    ) = pipeline.jobs.flatMap(CiJob::publishedChecks).map(
        transform = CiJob.PublishedCheck::name,
    )

    fun artifactPathContains(
        publishedPath: String,
        requiredFile: String,
    ): Boolean {
        val normalizedPublishedPath =
            normalizeArtifactPath(
                path = publishedPath,
            )
        val normalizedRequiredFile =
            normalizeArtifactPath(
                path = requiredFile,
            )
        return normalizedPublishedPath == normalizedRequiredFile ||
            normalizedRequiredFile.startsWith(
                prefix = "$normalizedPublishedPath/",
            )
    }

    private fun normalizeArtifactPath(
        path: String,
    ): String =
        path
            .substringBefore("=>")
            .trim()
            .replace(
                '\\',
                '/',
            ).replace(
                Regex("/(?:\\*\\*?|\\*\\.\\*)$"),
                "",
            ).removeSuffix("/")

    private fun requirePipeline(
        configuration: CiConfiguration,
        name: String,
    ): CiPipeline =
        configuration.pipelines.find { pipeline -> pipeline.name == name }
            ?: throw IllegalArgumentException("Effective CI configuration is missing pipeline '$name'.")

    private fun triggerLabel(
        pipeline: CiPipeline,
    ): String {
        val trigger = pipeline.triggers.firstOrNull() ?: return "Run manually"
        return when (trigger.type) {
            CiTrigger.Type.Vcs -> "VCS trigger ${trigger.branchFilter.orEmpty()}".trim()
            CiTrigger.Type.PipelineFinish -> "After ${trigger.dependencyPipelineId ?: "pipeline"} succeeds"
            CiTrigger.Type.Schedule -> trigger.type.serializedName
        }
    }

    private fun visualPhases(
        job: CiJob,
    ): List<CiVisualPlan.Phase> =
        job.steps.mapIndexed { phaseIndex, step ->
            val order =
                formattedOrder(
                    value = phaseIndex + 1,
                )
            val gradleTasks =
                gradleTaskNames(
                    command = step.command,
                )
            CiVisualPlan.Phase(
                order = order,
                title = step.name,
                technicalId = step.id,
                description =
                    phaseDescription(
                        command = step.command,
                        hasGradleTasks = gradleTasks.isNotEmpty(),
                    ),
                steps =
                    gradleTasks
                        .flatMap { task ->
                            gradleTaskSpecs(
                                task = task,
                            )
                        }.mapIndexed { stepIndex, spec ->
                            spec.toVisualStep(
                                order = "$order.${stepIndex + 1}",
                            )
                        },
            )
        }

    private fun visualOutcomes(
        job: CiJob,
    ): List<CiVisualPlan.Outcome> {
        var nextOrder = job.steps.size + 1
        val artifacts =
            job.artifacts
                .filter(CiJob.Artifact::publish)
                .map { artifact ->
                    CiVisualPlan.Outcome(
                        order =
                            formattedOrder(
                                value = nextOrder++,
                            ),
                        kind = CiVisualPlan.OutcomeKind.ARTIFACT,
                        title = "Publish build artifact",
                        technicalId = artifact.path,
                        description = "Makes the job output available to later CI jobs.",
                        condition = "After successful job execution",
                    )
                }
        val checks =
            job.publishedChecks.map { check ->
                CiVisualPlan.Outcome(
                    order =
                        formattedOrder(
                            value = nextOrder++,
                        ),
                    kind = CiVisualPlan.OutcomeKind.CHECK,
                    title = "Publish GitHub check",
                    technicalId = check.name,
                    description = "Reports the verified job result to the pull request.",
                    condition = "After successful job execution",
                )
            }
        return artifacts + checks
    }

    private fun formattedOrder(
        value: Int,
    ): String =
        value
            .toString()
            .padStart(
                length = 2,
                padChar = '0',
            )

    private fun phaseDescription(
        command: String,
        hasGradleTasks: Boolean,
    ): String =
        when {
            hasGradleTasks -> "Runs the Gradle entry points owned by this TeamCity phase."
            "test-agent-capabilities.ps1" in command -> "Validates the build agent before repository work begins."
            "teamcity-configs:generate" in command -> "Generates the effective TeamCity configuration with Maven."
            else -> "Runs the repository-owned command for this TeamCity phase."
        }

    private fun gradleTaskSpecs(
        task: String,
    ): List<StepSpec> =
        when (task) {
            "prepareTeamCityCiPlan" -> {
                listOf(
                    actionSpec(
                        task = task,
                        description = "Prepares the reviewed verification plan consumed by TeamCity.",
                    ),
                    actionSpec(
                        task = "generateCiPlan",
                        description = "Calculates the affected verification scope.",
                        condition = "Gradle dependency of prepareTeamCityCiPlan",
                    ),
                )
            }

            "%ci.plan.gradleTasks%" -> {
                dynamicCiPlanStepSpecs
            }

            "check" -> {
                listOf(
                    actionSpec(
                        task = task,
                        description =
                            "Runs the repository verification lifecycle, including Kotlin style, catalogs, " +
                                "versions, CI freshness, and verification-platform checks.",
                    ),
                )
            }

            "classifyCanonicalFigmaSyncChangeImpact" -> {
                listOf(
                    actionSpec(
                        task = task,
                        description = "Classifies whether the canonical Figma sync needs full verification.",
                    ),
                    actionSpec(
                        task = "cleanCanonicalFigmaSyncReports",
                        description = "Removes stale canonical Figma sync reports.",
                        condition = "Gradle dependency of classifyCanonicalFigmaSyncChangeImpact",
                    ),
                )
            }

            "materializeFigmaSyncCiConfiguration",
            "generateCanonicalFigmaSyncModel",
            "checkCanonicalFigmaTrunkSync",
            -> {
                listOf(
                    actionSpec(
                        task = task,
                        description = "Runs only for a validated full Figma verification scope.",
                        condition = "Full Figma verification",
                    ),
                )
            }

            "prepareCanonicalFigmaSync" -> {
                listOf(
                    actionSpec(
                        task = task,
                        description = "Builds the MCP runners and target-scoped visual plan.",
                    ),
                    actionSpec(
                        task = "writeFigmaWriterProjectConfig",
                        description = "Projects the repository-specific Figma writer contract.",
                        condition = "Gradle dependency of prepareCanonicalFigmaSync",
                    ),
                )
            }

            else -> {
                listOf(
                    actionSpec(
                        task = task,
                        description = "Runs this repository-owned Gradle entry point.",
                    ),
                )
            }
        }

    private fun StepSpec.toVisualStep(
        order: String,
    ) =
        CiVisualPlan.Step(
            order = order,
            role = role,
            title = title,
            technicalId = technicalId,
            description = description,
            condition = condition,
        )

    private fun gradleTaskNames(
        command: String,
    ): List<String> =
        command
            .lineSequence()
            .mapNotNull { line ->
                gradleInvocation.find(line.trim())?.groupValues?.get(
                    index = 1,
                )
            }.flatMap { arguments ->
                arguments
                    .trim()
                    .split(Regex("\\s+"))
                    .takeWhile { argument ->
                        !argument.startsWith(
                            prefix = "-",
                        )
                    }.asSequence()
            }.toList()

    private fun pipelineDescription(
        name: String,
        config: CiVisualPlanConfig,
    ): String =
        if (name == config.ciPipelineName) {
            "Validates pull requests and branch revisions before merge."
        } else {
            "Verifies post-merge Figma documentation against main."
        }

    private fun jobDescription(
        name: String,
    ): String =
        when (name) {
            "Verify" -> "Runs repository verification and publishes the required CI check."
            "Generate main design model" -> "Builds and publishes the canonical design-model.json artifact."
            "Check Figma trunk sync" -> "Compares current Figma metadata with the canonical model hash."
            else -> "Executes an effective TeamCity pipeline job."
        }

    fun externalEnvironment(
        id: String,
    ): CiVisualPlan.Environment =
        externalEnvironments[id]
            ?: throw IllegalArgumentException("External CI node '$id' has no .ci icon environment mapping.")

    fun windowsRuntimeEnvironment(
        id: String,
    ): CiVisualPlan.Environment =
        windowsRuntimeEnvironments[id]
            ?: throw IllegalArgumentException("Windows CI runtime service '$id' has no .ci icon environment mapping.")

    private fun sourceUrl(
        source: String,
        config: CiVisualPlanConfig,
    ) = "${config.githubMainBlobUrl}/$source"

    private fun CiNode.Type.toVisualType(): CiVisualPlan.Type =
        CiVisualPlan.Type.entries.single { type ->
            type.wireValue == serializedName
        }

    private fun infrastructurePlacement() =
        mapOf(
            "browser" to
                Position(
                    row = 0,
                    column = 0,
                ),
            "teamcity-cli" to
                Position(
                    row = 0,
                    column = 1,
                ),
            "github-app" to
                Position(
                    row = 0,
                    column = 2,
                ),
            "operator" to
                Position(
                    row = 0,
                    column = 3,
                ),
            "cloudflare-access" to
                Position(
                    row = 1,
                    column = 1,
                ),
            "cloudflare-tunnel" to
                Position(
                    row = 2,
                    column = 1,
                ),
            "teamcity-server" to
                Position(
                    row = 3,
                    column = 1,
                ),
            "github-repository" to
                Position(
                    row = 3,
                    column = 2,
                ),
            "build-agent" to
                Position(
                    row = 4,
                    column = 1,
                ),
            "codex-mcp-client" to
                Position(
                    row = 4,
                    column = 3,
                ),
            "figma-api" to
                Position(
                    row = 5,
                    column = 2,
                ),
            "figma-design-document" to
                Position(
                    row = 6,
                    column = 2,
                ),
        )

    private data class Position(
        val row: Int,
        val column: Int,
    )

    private data class StepSpec(
        val role: CiVisualPlan.StepRole,
        val title: String,
        val technicalId: String?,
        val description: String?,
        val condition: String?,
    )

    private companion object {
        fun actionSpec(
            task: String,
            description: String,
            condition: String? = null,
        ) =
            StepSpec(
                role = CiVisualPlan.StepRole.ACTION,
                title =
                    gradleTaskTitle(
                        task = task,
                    ),
                technicalId = task,
                description = description,
                condition = condition,
            )

        fun groupSpec(
            title: String,
            technicalId: String,
            description: String,
            condition: String,
        ) =
            StepSpec(
                role = CiVisualPlan.StepRole.GROUP,
                title = title,
                technicalId = technicalId,
                description = description,
                condition = condition,
            )

        fun gradleTaskTitle(
            task: String,
        ): String =
            when (task) {
                ":<affected-module>:check" -> {
                    "Check affected module"
                }

                "verification-platform:check" -> {
                    "Check verification platform"
                }

                else -> {
                    task
                        .substringAfterLast(':')
                        .replace(
                            Regex("([a-z0-9])([A-Z])"),
                            "${'$'}1 ${'$'}2",
                        ).replaceFirstChar(Char::uppercaseChar)
                }
            }

        val gradleInvocation =
            Regex(
                """(?:^|\s)(?:call\s+)?(?:\.\\|\./)?gradlew(?:\.bat)?\s+(.+)$""",
                RegexOption.IGNORE_CASE,
            )
        val dynamicCiPlanStepSpecs =
            listOf(
                StepSpec(
                    role = CiVisualPlan.StepRole.DECISION,
                    title = "Select verification tasks",
                    technicalId = "ci.plan.gradleTasks",
                    description = "Expands the reviewed CI plan into the tasks for this change.",
                    condition = "Repository change scope",
                ),
                groupSpec(
                    title = "Always",
                    technicalId = "checkGitWorkflow · checkDocumentation",
                    description = "Validates the Git workflow and repository documentation contracts.",
                    condition = "Always",
                ),
                groupSpec(
                    title = "According to changes",
                    technicalId =
                        "checkRepositoryDiff · checkTeamCityDsl · :<affected-module>:check · " +
                            "checkFigmaCatalogUsage",
                    description = "Runs only the repository, TeamCity, module, and catalog checks selected by impact.",
                    condition = "Repository change scope",
                ),
                groupSpec(
                    title = "Full verification",
                    technicalId = "check",
                    description =
                        "Includes Kotlin style, catalog usage and naming, CI freshness, and " +
                            "verification-platform checks.",
                    condition = "TeamCity changes or fail-closed fallback",
                ),
            )
        val externalEnvironments =
            mapOf(
                "operator" to CiVisualPlan.Environment.OPERATOR,
                "browser" to CiVisualPlan.Environment.BROWSER,
                "teamcity-cli" to CiVisualPlan.Environment.TERMINAL,
                "github-repository" to CiVisualPlan.Environment.GITHUB,
                "github-app" to CiVisualPlan.Environment.GITHUB,
                "cloudflare-access" to CiVisualPlan.Environment.CLOUDFLARE,
                "cloudflare-tunnel" to CiVisualPlan.Environment.CLOUDFLARE,
                "teamcity-server" to CiVisualPlan.Environment.TEAMCITY,
                "build-agent" to CiVisualPlan.Environment.TEAMCITY,
                "codex-mcp-client" to CiVisualPlan.Environment.CODEX,
                "figma-api" to CiVisualPlan.Environment.FIGMA,
                "figma-design-document" to CiVisualPlan.Environment.FIGMA,
            )
        val windowsRuntimeEnvironments =
            mapOf(
                "teamcity-server" to CiVisualPlan.Environment.TEAMCITY,
                "build-agent" to CiVisualPlan.Environment.TEAMCITY,
                "cloudflare-tunnel" to CiVisualPlan.Environment.CLOUDFLARE,
            )
    }
}
