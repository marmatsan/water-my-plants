package com.marmatsan.figmaDocumentationSync.teamcityAdapter

/** TeamCity run state used by repository-owned operational orchestration. */
data class TeamCityRun(
    val id: Long,
    val state: String,
    val status: String?,
    val statusText: String?,
    val branchName: String?,
    val webUrl: String?,
)
