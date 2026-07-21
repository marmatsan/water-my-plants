package com.marmatsan.figmaDocumentationSync.projectConfig

/** Observable outcome of validating or rerunning the canonical Figma Sync pipeline. */
data class TeamCityFigmaSyncRerunResult(
    val runId: Long?,
    val webUrl: String?,
    val branch: String,
    val state: String,
    val reused: Boolean,
)
