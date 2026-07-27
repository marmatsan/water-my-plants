package com.marmatsan.figmaDocumentationSync.domain.model.visual

/**
 * Project-owned names and source links used by the portable CI visual planner.
 *
 * @property configurationModelName selected effective CI configuration model.
 * @property ciPipelineName pipeline rendered as the primary CI journey.
 * @property figmaPipelineName pipeline rendered as the Figma publication journey.
 * @property githubMainBlobUrl GitHub main-branch URL prefix for source files.
 * @property teamCitySource canonical TeamCity configuration source.
 * @property topologySource canonical external topology source.
 * @property windowsRuntimeSource canonical Windows runtime source.
 * @property windowsRuntimeRunbookSource operational Windows runtime runbook.
 * @property visualContractSource canonical Figma visual contract.
 * @property branchProtectionSource canonical branch-protection documentation.
 * @property canonicalSyncSource canonical Figma synchronization workflow.
 * @property canonicalDesignModelPath repository-relative canonical design-model path.
 */
data class CiVisualPlanConfig(
    val configurationModelName: String,
    val ciPipelineName: String,
    val figmaPipelineName: String,
    val githubMainBlobUrl: String,
    val teamCitySource: String,
    val topologySource: String,
    val windowsRuntimeSource: String,
    val windowsRuntimeRunbookSource: String,
    val visualContractSource: String,
    val branchProtectionSource: String,
    val canonicalSyncSource: String,
    val canonicalDesignModelPath: String,
)
