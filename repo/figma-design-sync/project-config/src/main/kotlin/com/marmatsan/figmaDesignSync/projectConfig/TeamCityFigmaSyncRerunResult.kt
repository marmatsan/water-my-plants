package com.marmatsan.figmaDesignSync.projectConfig

/** Observable outcome of validating or rerunning the official Figma Sync pipeline. */
data class TeamCityFigmaSyncRerunResult(
    val runId: Long?,
    val webUrl: String?,
    val branch: String,
    val state: String,
    val reused: Boolean
)
