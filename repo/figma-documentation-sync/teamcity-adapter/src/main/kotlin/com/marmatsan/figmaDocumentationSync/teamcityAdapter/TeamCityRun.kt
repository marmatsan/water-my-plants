package com.marmatsan.figmaDocumentationSync.teamcityAdapter

/**
 * TeamCity run state used by repository-owned operational orchestration.
 *
 * @property id immutable TeamCity build id.
 * @property state lifecycle state used to decide whether polling should continue.
 * @property status optional terminal success or failure status.
 * @property statusText optional human-readable status detail.
 * @property branchName optional branch assigned to the queued run.
 * @property webUrl optional TeamCity UI link for operator inspection.
 */
data class TeamCityRun(
    val id: Long,
    val state: String,
    val status: String?,
    val statusText: String?,
    val branchName: String?,
    val webUrl: String?
)
