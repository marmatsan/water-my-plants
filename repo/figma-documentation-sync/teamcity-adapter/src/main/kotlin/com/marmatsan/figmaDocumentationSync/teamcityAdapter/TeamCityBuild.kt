package com.marmatsan.figmaDocumentationSync.teamcityAdapter

/**
 * TeamCity build identity required before consuming its published artifacts.
 *
 * @property id immutable TeamCity build id.
 * @property state lifecycle state such as queued, running, or finished.
 * @property status terminal success or failure status reported by TeamCity.
 * @property branchName branch whose revision produced the artifacts.
 * @property buildTypeName human-readable build configuration name.
 * @property webUrl optional TeamCity UI link for operator inspection.
 */
data class TeamCityBuild(
    val id: Long,
    val state: String,
    val status: String,
    val branchName: String,
    val buildTypeName: String,
    val webUrl: String?,
)
