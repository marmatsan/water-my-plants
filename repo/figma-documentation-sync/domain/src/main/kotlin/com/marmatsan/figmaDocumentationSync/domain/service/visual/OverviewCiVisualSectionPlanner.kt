package com.marmatsan.figmaDocumentationSync.domain.service.visual

import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan

/** Plans the high-level pull-request and post-merge journey. */
internal class OverviewCiVisualSectionPlanner : CiVisualSectionPlanner {
    /** Builds the high-level pull-request and post-merge journey section. */
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
