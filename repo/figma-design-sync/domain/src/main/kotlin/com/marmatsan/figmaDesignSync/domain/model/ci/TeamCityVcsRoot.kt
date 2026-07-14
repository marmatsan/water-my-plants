package com.marmatsan.figmaDesignSync.domain.model.ci

/**
 * Repository metadata extracted from an effective TeamCity VCS root.
 */
data class TeamCityVcsRoot(
    val id: String,
    val name: String,
    val url: String,
    val defaultBranchRef: String,
    val branchSpec: List<String>
)
