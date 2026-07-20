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
        config: CiVisualPlanConfig
    ): CiVisualPlan {
        val ciPipeline = requirePipeline(configuration, config.ciPipelineName)
        val figmaPipeline = requirePipeline(configuration, config.figmaPipelineName)
        return CiVisualPlan(
            parentName = "Continuous Integration and Design Documentation",
            sections = listOf(
                createOverviewSection(ciPipeline, figmaPipeline, config),
                createPullRequestSection(ciPipeline, config),
                createPostMergeSection(externalTopology, figmaPipeline, config),
                createInfrastructureSection(externalTopology, config),
                createWindowsRuntimeSection(windowsRuntime, config)
            )
        )
    }

    private fun createOverviewSection(
        ciPipeline: CiPipeline,
        figmaPipeline: CiPipeline,
        config: CiVisualPlanConfig
    ): CiVisualPlan.Section {
        val ciCheck = requireNotNull(publishedChecks(ciPipeline).firstOrNull()) {
            "CI pipeline '${ciPipeline.name}' must expose its versioned required status"
        }
        val figmaCheck = publishedChecks(figmaPipeline).firstOrNull()
        val nodes = mutableListOf(
            visualNode("overview-pr", CiVisualPlan.Type.GIT_REFERENCE, CiVisualPlan.Environment.GITHUB, "Pull Request", "Proposes a reviewed change to the repository.", config.branchProtectionSource, 0, 0, config),
            pipelineNode("overview-ci", ciPipeline, 1, 0, config),
            visualNode("overview-ci-check", CiVisualPlan.Type.CHECK, CiVisualPlan.Environment.TEAMCITY, ciCheck, "Reports CI verification to GitHub.", config.teamCitySource, 2, 0, config),
            visualNode("overview-gate", CiVisualPlan.Type.GATE, CiVisualPlan.Environment.GITHUB, "Pull request merge gate", "Requires the CI check before merge.", config.branchProtectionSource, 3, 0, config),
            visualNode("overview-main", CiVisualPlan.Type.GIT_REFERENCE, CiVisualPlan.Environment.GITHUB, "main", "Stable trunk after the reviewed merge.", config.branchProtectionSource, 4, 0, config),
            pipelineNode("overview-figma", figmaPipeline, 5, 0, config),
            visualNode("overview-model", CiVisualPlan.Type.ARTIFACT, CiVisualPlan.Environment.JSON, "design-model.json", "Carries the repository documentation snapshot.", config.teamCitySource, 6, 0, config)
        )
        if (figmaCheck != null) {
            nodes += visualNode("overview-figma-check", CiVisualPlan.Type.CHECK, CiVisualPlan.Environment.TEAMCITY, figmaCheck, "Reports whether Figma metadata matches main.", config.teamCitySource, 7, 0, config)
        }
        val connections = mutableListOf(
            connection("overview-pr-trigger", "overview-pr", "overview-ci", triggerLabel(ciPipeline)),
            connection("overview-ci-check", "overview-ci", "overview-ci-check", "Publish check"),
            connection("overview-check-gate", "overview-ci-check", "overview-gate", "Required check"),
            connection("overview-gate-main", "overview-gate", "overview-main", "Merge"),
            connection("overview-main-figma", "overview-main", "overview-figma", triggerLabel(figmaPipeline)),
            connection("overview-figma-model", "overview-figma", "overview-model", "Generate and publish")
        )
        if (figmaCheck != null) {
            connections += connection("overview-model-check", "overview-model", "overview-figma-check", "Verify model hash")
        }
        return section(
            target = "ci.overview",
            name = "Overview",
            description = "Simplified pull request and post-merge design documentation journeys.",
            sources = listOf(config.visualContractSource),
            orientation = CiVisualPlan.Orientation.HORIZONTAL,
            nodes = nodes,
            connections = connections,
            config = config
        )
    }

    private fun createPullRequestSection(
        pipeline: CiPipeline,
        config: CiVisualPlanConfig
    ): CiVisualPlan.Section {
        val nodes = mutableListOf(
            visualNode("pr", CiVisualPlan.Type.GIT_REFERENCE, CiVisualPlan.Environment.GITHUB, "Pull Request", "Contains the branch revision proposed for main.", config.branchProtectionSource, 0, 0, config),
            pipelineNode("pipeline-${pipeline.id}", pipeline, 1, 0, config)
        )
        pipeline.jobs.forEachIndexed { index, job -> nodes += jobNode("job-${job.id}", job, 2 + index, 0, config) }
        val checks = publishedChecks(pipeline)
        checks.forEachIndexed { index, check ->
            nodes += visualNode("check-$index", CiVisualPlan.Type.CHECK, CiVisualPlan.Environment.TEAMCITY, check, "Publishes the CI result to GitHub.", config.teamCitySource, 2 + pipeline.jobs.size, index, config)
        }
        nodes += visualNode("merge-gate", CiVisualPlan.Type.GATE, CiVisualPlan.Environment.GITHUB, "Pull request merge gate", "Requires TeamCity CI before merging.", config.branchProtectionSource, 3 + pipeline.jobs.size, 0, config)
        nodes += visualNode("main", CiVisualPlan.Type.GIT_REFERENCE, CiVisualPlan.Environment.GITHUB, "main", "Receives the reviewed change after the gate passes.", config.branchProtectionSource, 4 + pipeline.jobs.size, 0, config)

        val connections = mutableListOf(connection("pr-trigger", "pr", "pipeline-${pipeline.id}", triggerLabel(pipeline)))
        pipeline.jobs.forEachIndexed { index, job ->
            connections += connection(
                "pipeline-job-${job.id}",
                if (index == 0) "pipeline-${pipeline.id}" else "job-${pipeline.jobs[index - 1].id}",
                "job-${job.id}",
                if (index == 0) "Run pipeline" else "Continue"
            )
        }
        checks.forEachIndexed { index, _ ->
            connections += connection("job-check-$index", pipeline.jobs.lastOrNull()?.let { "job-${it.id}" } ?: "pipeline-${pipeline.id}", "check-$index", "Publish check")
            connections += connection("check-gate-$index", "check-$index", "merge-gate", "Required check")
        }
        connections += connection("gate-main", "merge-gate", "main", "Merge")
        return section("ci.pullRequestIntegration", "Pull Request Integration", "Detailed merge-gate flow derived from the effective CI pipeline.", listOf(config.teamCitySource, config.branchProtectionSource), CiVisualPlan.Orientation.HORIZONTAL, nodes, connections, config)
    }

    private fun createPostMergeSection(
        topology: CiExternalTopology,
        pipeline: CiPipeline,
        config: CiVisualPlanConfig
    ): CiVisualPlan.Section {
        val externalById = topology.nodes.associateBy(CiNode::id)
        val operator = externalById["operator"]
        val codex = externalById["codex-mcp-client"]
        val figmaDocument = externalById["figma-design-document"]
        val artifactJob = pipeline.jobs.find { job ->
            job.artifacts.any { artifact -> artifactPathContains(artifact.path, config.officialDesignModelPath) }
        }
        val checkJob = artifactJob?.let { generated ->
            pipeline.jobs.find { job ->
                job.dependencies.any { dependency ->
                    dependency.jobId == generated.id && dependency.artifactPaths.any { path ->
                        artifactPathContains(path, config.officialDesignModelPath)
                    }
                }
            }
        }
        val orderedJobs = listOfNotNull(artifactJob, checkJob) + pipeline.jobs.filter { job ->
            job.id != artifactJob?.id && job.id != checkJob?.id
        }
        val jobRows = orderedJobs.mapIndexed { index, job -> job.id to 2 + index * 2 }.toMap()
        val nodes = mutableListOf(
            visualNode("main", CiVisualPlan.Type.GIT_REFERENCE, CiVisualPlan.Environment.GITHUB, "main", "Starts documentation verification after successful CI.", config.teamCitySource, 0, 0, config),
            pipelineNode("pipeline-${pipeline.id}", pipeline, 1, 0, config)
        )
        pipeline.jobs.forEachIndexed { index, job ->
            nodes += jobNode("job-${job.id}", job, jobRows[job.id] ?: 2 + index * 2, 0, config)
        }
        val artifactRow = artifactJob?.let { (jobRows[it.id] ?: 2) + 1 } ?: 2
        nodes += visualNode("design-model", CiVisualPlan.Type.ARTIFACT, CiVisualPlan.Environment.JSON, "design-model.json", "Official repository snapshot consumed by visual synchronization.", config.teamCitySource, artifactRow, 0, config)
        val checkRow = maxOf(2 + pipeline.jobs.size * 2, nodes.maxOf { node -> node.row + 1 })
        val checks = publishedChecks(pipeline)
        checks.forEachIndexed { index, check ->
            nodes += visualNode("figma-check-$index", CiVisualPlan.Type.CHECK, CiVisualPlan.Environment.TEAMCITY, check, "Publishes the post-merge documentation result.", config.teamCitySource, checkRow, index, config)
        }
        operator?.let { nodes += externalNode("operator", it, checkRow + 1, 0, config) }
        codex?.let { nodes += externalNode("codex", it, checkRow + 2, 0, config) }
        figmaDocument?.let { nodes += externalNode("figma-document", it, checkRow + 3, 0, config) }

        val connections = mutableListOf(connection("main-trigger", "main", "pipeline-${pipeline.id}", triggerLabel(pipeline)))
        artifactJob?.let {
            connections += connection("pipeline-generate", "pipeline-${pipeline.id}", "job-${it.id}", "Run pipeline")
            connections += connection("generate-artifact", "job-${it.id}", "design-model", "Publish artifact")
        }
        checkJob?.let { connections += connection("artifact-check", "design-model", "job-${it.id}", "Consume artifact") }
        checks.forEachIndexed { index, _ ->
            connections += connection("check-status-$index", checkJob?.let { "job-${it.id}" } ?: "pipeline-${pipeline.id}", "figma-check-$index", "Publish status")
        }
        if (operator != null && checks.isNotEmpty()) connections += connection("mismatch-operator", "figma-check-0", "operator", "Mismatch requires action")
        if (operator != null && codex != null) connections += connection("operator-codex", "operator", "codex", "Request visual synchronization")
        if (codex != null && figmaDocument != null) connections += connection("codex-figma", "codex", "figma-document", "Apply visual changes")
        if (figmaDocument != null && checkJob != null) connections += connection("rerun", "figma-document", "job-${checkJob.id}", "Rerun via HTTPS client")
        return section("ci.postMergeDesignDocumentation", "Post-merge Design Documentation", "Official model generation, visual synchronization, and verification loop.", listOf(config.teamCitySource, config.officialSyncSource), CiVisualPlan.Orientation.HORIZONTAL, nodes, connections, config)
    }

    private fun createInfrastructureSection(
        topology: CiExternalTopology,
        config: CiVisualPlanConfig
    ): CiVisualPlan.Section {
        val placement = infrastructurePlacement()
        val nodes = topology.nodes.mapIndexed { index, node ->
            val position = placement[node.id] ?: Position(index, 0)
            externalNode("external-${node.id}", node, position.row, position.column, config)
        }
        val nodeIds = topology.nodes.associate { node -> node.id to "external-${node.id}" }
        val connections = topology.connections.map { edge ->
            connection("external-${edge.id}", nodeIds[edge.sourceNodeId], nodeIds[edge.targetNodeId], edge.label)
        }
        return section("ci.infrastructureAndAccess", "Infrastructure and Access", "External systems, trust boundaries, authentication paths, and automation modes.", listOf(config.topologySource, "docs/ci/external-topology-validation.md"), CiVisualPlan.Orientation.GRID, nodes, connections, config)
    }

    private fun createWindowsRuntimeSection(
        runtime: CiWindowsRuntime,
        config: CiVisualPlanConfig
    ): CiVisualPlan.Section {
        val nodes = runtime.services.mapIndexed { index, service ->
            visualNode("windows-runtime-${service.id}", CiVisualPlan.Type.SYSTEM, windowsRuntimeEnvironment(service.id), service.name, service.description, config.windowsRuntimeSource, 0, index, config).copy(
                runtime = CiVisualPlan.Runtime(runtime.platform, service.service, service.startup, service.identity)
            )
        }
        return section("ci.windowsRuntime", "Windows Service Runtime", "Versioned inventory of the Windows services that host the local CI runtime.", listOf(config.windowsRuntimeSource, config.windowsRuntimeRunbookSource), CiVisualPlan.Orientation.GRID, nodes, emptyList(), config)
    }

    private fun section(
        target: String,
        name: String,
        description: String,
        sources: List<String>,
        orientation: CiVisualPlan.Orientation,
        nodes: List<CiVisualPlan.Node>,
        connections: List<CiVisualPlan.Connection>,
        config: CiVisualPlanConfig
    ) = CiVisualPlan.Section(target, name, description, orientation, sources.map { source -> CiVisualPlan.HeaderSource(source, sourceUrl(source, config)) }, nodes, connections)

    private fun pipelineNode(id: String, pipeline: CiPipeline, row: Int, column: Int, config: CiVisualPlanConfig) =
        visualNode(id, CiVisualPlan.Type.PIPELINE, CiVisualPlan.Environment.TEAMCITY, pipeline.name, pipelineDescription(pipeline.name, config), config.teamCitySource, row, column, config)

    private fun jobNode(id: String, job: CiJob, row: Int, column: Int, config: CiVisualPlanConfig) =
        visualNode(id, CiVisualPlan.Type.JOB, CiVisualPlan.Environment.TEAMCITY, job.name, jobDescription(job.name), config.teamCitySource, row, column, config).copy(
            steps = job.steps.map { step -> summarizeCommand(step.command, step.name) }.joinToString("\n").ifEmpty { null }
        )

    private fun externalNode(id: String, node: CiNode, row: Int, column: Int, config: CiVisualPlanConfig) =
        visualNode(id, node.type.toVisualType(), externalEnvironment(node.id), node.name, node.description, config.topologySource, row, column, config)

    private fun visualNode(
        id: String,
        type: CiVisualPlan.Type,
        environment: CiVisualPlan.Environment,
        name: String,
        description: String,
        source: String,
        row: Int,
        column: Int,
        config: CiVisualPlanConfig
    ) = CiVisualPlan.Node(id, type, environment, name, description, null, null, source, sourceUrl(source, config), row, column)

    private fun connection(id: String, source: String?, target: String?, label: String): CiVisualPlan.Connection {
        require(source != null && target != null) { "CI visual connection '$id' has an unknown endpoint." }
        return CiVisualPlan.Connection(id, source, target, label)
    }

    private fun publishedChecks(pipeline: CiPipeline) = pipeline.jobs.flatMap(CiJob::publishedChecks).map(CiJob.PublishedCheck::name)

    fun artifactPathContains(publishedPath: String, requiredFile: String): Boolean {
        val normalizedPublishedPath = normalizeArtifactPath(publishedPath)
        val normalizedRequiredFile = normalizeArtifactPath(requiredFile)
        return normalizedPublishedPath == normalizedRequiredFile || normalizedRequiredFile.startsWith("$normalizedPublishedPath/")
    }

    private fun normalizeArtifactPath(path: String): String = path
        .substringBefore("=>")
        .trim()
        .replace('\\', '/')
        .replace(Regex("/(?:\\*\\*?|\\*\\.\\*)$"), "")
        .removeSuffix("/")

    private fun requirePipeline(configuration: CiConfiguration, name: String): CiPipeline =
        configuration.pipelines.find { pipeline -> pipeline.name == name }
            ?: throw IllegalArgumentException("Effective CI configuration is missing pipeline '$name'.")

    private fun triggerLabel(pipeline: CiPipeline): String {
        val trigger = pipeline.triggers.firstOrNull() ?: return "Run manually"
        return when (trigger.type) {
            CiTrigger.Type.Vcs -> "VCS trigger ${trigger.branchFilter.orEmpty()}".trim()
            CiTrigger.Type.PipelineFinish -> "After ${trigger.dependencyPipelineId ?: "pipeline"} succeeds"
            CiTrigger.Type.Schedule -> trigger.type.serializedName
        }
    }

    private fun summarizeCommand(command: String, fallback: String): String = when {
        "teamcity-configs:generate" in command -> "Generate effective TeamCity configuration"
        "generateFigmaDesignModel" in command -> "Generate design model"
        "checkFigmaTrunkSync" in command -> "Check Figma trunk sync"
        Regex("gradlew(?:\\.bat)?\\s+check(?:\\s|$)", RegexOption.IGNORE_CASE).containsMatchIn(command) -> "Gradle check"
        else -> fallback
    }

    private fun pipelineDescription(name: String, config: CiVisualPlanConfig): String =
        if (name == config.ciPipelineName) "Validates pull requests and branch revisions before merge."
        else "Verifies post-merge Figma documentation against main."

    private fun jobDescription(name: String): String = when (name) {
        "Verify" -> "Runs repository verification and publishes the required CI check."
        "Generate main design model" -> "Builds and publishes the official design-model.json artifact."
        "Check Figma trunk sync" -> "Compares current Figma metadata with the official model hash."
        else -> "Executes an effective TeamCity pipeline job."
    }

    fun externalEnvironment(id: String): CiVisualPlan.Environment = externalEnvironments[id]
        ?: throw IllegalArgumentException("External CI node '$id' has no .ci icon environment mapping.")

    fun windowsRuntimeEnvironment(id: String): CiVisualPlan.Environment = windowsRuntimeEnvironments[id]
        ?: throw IllegalArgumentException("Windows CI runtime service '$id' has no .ci icon environment mapping.")

    private fun sourceUrl(source: String, config: CiVisualPlanConfig) = "${config.githubMainBlobUrl}/$source"

    private fun CiNode.Type.toVisualType(): CiVisualPlan.Type = CiVisualPlan.Type.entries.single { type ->
        type.wireValue == serializedName
    }

    private fun infrastructurePlacement() = mapOf(
        "browser" to Position(0, 0),
        "teamcity-cli" to Position(0, 1),
        "github-app" to Position(0, 2),
        "operator" to Position(0, 3),
        "cloudflare-access" to Position(1, 1),
        "cloudflare-tunnel" to Position(2, 1),
        "teamcity-server" to Position(3, 1),
        "github-repository" to Position(3, 2),
        "build-agent" to Position(4, 1),
        "codex-mcp-client" to Position(4, 3),
        "figma-api" to Position(5, 2),
        "figma-design-document" to Position(6, 2)
    )

    private data class Position(val row: Int, val column: Int)

    private companion object {
        val externalEnvironments = mapOf(
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
            "figma-design-document" to CiVisualPlan.Environment.FIGMA
        )
        val windowsRuntimeEnvironments = mapOf(
            "teamcity-server" to CiVisualPlan.Environment.TEAMCITY,
            "build-agent" to CiVisualPlan.Environment.TEAMCITY,
            "cloudflare-tunnel" to CiVisualPlan.Environment.CLOUDFLARE
        )
    }
}
