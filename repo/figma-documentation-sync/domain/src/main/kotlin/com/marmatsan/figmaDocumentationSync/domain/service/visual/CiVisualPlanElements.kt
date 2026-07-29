package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiJob
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiNode
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiTrigger
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig

internal fun visualSection(
    target: String,
    name: String,
    description: String,
    sources: List<String>,
    orientation: CiVisualPlan.Orientation,
    nodes: List<CiVisualPlan.Node>,
    connections: List<CiVisualPlan.Connection>,
    config: CiVisualPlanConfig
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
                        config = config
                    )
            )
        },
    nodes = nodes,
    connections = connections
)

internal fun pipelineNode(
    id: String,
    pipeline: CiPipeline,
    row: Int,
    column: Int,
    config: CiVisualPlanConfig
) = visualNode(
    id = id,
    type = CiVisualPlan.Type.PIPELINE,
    environment = CiVisualPlan.Environment.TEAMCITY,
    name = pipeline.name,
    description =
        pipelineDescription(
            name = pipeline.name,
            config = config
        ),
    source = config.teamCitySource,
    row = row,
    column = column,
    config = config
)

internal fun jobNode(
    id: String,
    job: CiJob,
    row: Int,
    column: Int,
    config: CiVisualPlanConfig
) = visualNode(
    id = id,
    type = CiVisualPlan.Type.JOB,
    environment = CiVisualPlan.Environment.TEAMCITY,
    name = job.name,
    description =
        jobDescription(
            name = job.name
        ),
    source = config.teamCitySource,
    row = row,
    column = column,
    config = config
)

internal fun externalNode(
    id: String,
    node: CiNode,
    row: Int,
    column: Int,
    config: CiVisualPlanConfig,
    environments: CiVisualEnvironmentResolver
) = visualNode(
    id = id,
    type = node.type.toVisualType(),
    environment = environments.external(node.id),
    name = node.name,
    description = node.description,
    source = config.topologySource,
    row = row,
    column = column,
    config = config
)

internal fun visualNode(
    id: String,
    type: CiVisualPlan.Type,
    environment: CiVisualPlan.Environment,
    name: String,
    description: String,
    source: String,
    row: Int,
    column: Int,
    config: CiVisualPlanConfig
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
            config = config
        ),
    row = row,
    column = column
)

internal fun visualConnection(
    id: String,
    source: String?,
    target: String?,
    label: String,
    kind: CiVisualPlan.ConnectionKind
): CiVisualPlan.Connection {
    require(source != null && target != null) { "CI visual connection '$id' has an unknown endpoint." }
    return CiVisualPlan.Connection(
        id = id,
        source = source,
        target = target,
        label = label,
        kind = kind
    )
}

internal fun externalConnectionKind(
    connectionId: String
): CiVisualPlan.ConnectionKind =
    when (connectionId) {
        "github-source-checkout",
        "figma-metadata-read",
        "mcp-figma-write",
        "figma-document-update"
        -> CiVisualPlan.ConnectionKind.DATA

        "github-check-publication",
        "teamcity-build-results"
        -> CiVisualPlan.ConnectionKind.STATUS

        "browser-teamcity-access",
        "cli-teamcity-access",
        "github-webhook-ingress",
        "cloudflare-origin-routing",
        "teamcity-origin-delivery",
        "teamcity-job-dispatch",
        "operator-visual-sync"
        -> CiVisualPlan.ConnectionKind.CONTROL

        else -> CiVisualPlan.ConnectionKind.NEUTRAL
    }

internal fun publishedChecks(
    pipeline: CiPipeline
): List<String> =
    pipeline.jobs
        .flatMap(CiJob::publishedChecks)
        .map(CiJob.PublishedCheck::name)

internal fun triggerLabel(
    pipeline: CiPipeline
): String {
    val trigger = pipeline.triggers.firstOrNull() ?: return "Run manually"
    return when (trigger.type) {
        CiTrigger.Type.Vcs -> "VCS trigger ${trigger.branchFilter.orEmpty()}".trim()
        CiTrigger.Type.PipelineFinish -> "After ${trigger.dependencyPipelineId ?: "pipeline"} succeeds"
        CiTrigger.Type.Schedule -> trigger.type.serializedName
    }
}

internal fun jobDescription(
    name: String
): String =
    when (name) {
        "Verify" -> "Runs repository verification and publishes the required CI check."
        "Generate main design model" -> "Builds and publishes the canonical design-model.json artifact."
        "Check Figma trunk sync" -> "Compares current Figma metadata with the canonical model hash."
        else -> "Executes an effective TeamCity pipeline job."
    }

private fun pipelineDescription(
    name: String,
    config: CiVisualPlanConfig
): String =
    if (name == config.ciPipelineName) {
        "Validates pull requests and branch revisions before merge."
    } else {
        "Verifies post-merge Figma documentation against main."
    }

private fun sourceUrl(
    source: String,
    config: CiVisualPlanConfig
) = "${config.githubMainBlobUrl}/$source"

private fun CiNode.Type.toVisualType(): CiVisualPlan.Type =
    CiVisualPlan.Type.entries.single { type -> type.wireValue == serializedName }
