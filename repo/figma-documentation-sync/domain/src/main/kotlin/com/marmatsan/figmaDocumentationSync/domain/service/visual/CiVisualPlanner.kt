package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig

/** Composes independently extensible CI section planners into one deterministic visual plan. */
class CiVisualPlanner internal constructor(
    private val sectionPlanners: List<CiVisualSectionPlanner>,
) {
    constructor() : this(defaultCiVisualSectionPlanners())

    fun create(
        externalTopology: CiExternalTopology,
        windowsRuntime: CiWindowsRuntime,
        configuration: CiConfiguration,
        config: CiVisualPlanConfig,
    ): CiVisualPlan {
        val context =
            CiVisualPlanningContext(
                externalTopology = externalTopology,
                windowsRuntime = windowsRuntime,
                config = config,
                ciPipeline = configuration.requirePipeline(config.ciPipelineName),
                figmaPipeline = configuration.requirePipeline(config.figmaPipelineName),
            )
        return CiVisualPlan(
            parentName = "Continuous Integration and Documentation Automation",
            sections = sectionPlanners.map { planner -> planner.create(context) },
        )
    }
}

private fun CiConfiguration.requirePipeline(
    name: String,
): CiPipeline =
    pipelines.find { pipeline -> pipeline.name == name }
        ?: throw IllegalArgumentException("Effective CI configuration is missing pipeline '$name'.")

private fun defaultCiVisualSectionPlanners(): List<CiVisualSectionPlanner> {
    val environments = CiVisualEnvironmentResolver()
    val artifactPaths = CiArtifactPathMatcher()
    return listOf(
        OverviewCiVisualSectionPlanner(),
        PullRequestCiVisualSectionPlanner(),
        PostMergeCiVisualSectionPlanner(
            environments,
            artifactPaths,
        ),
        JobTasksCiVisualSectionPlanner(),
        InfrastructureCiVisualSectionPlanner(environments),
        WindowsRuntimeCiVisualSectionPlanner(environments),
    )
}
