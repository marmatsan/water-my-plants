package com.marmatsan.figmaDocumentationSync.domain.model.visual

/** Project-owned names and source links used by the portable CI visual planner. */
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
