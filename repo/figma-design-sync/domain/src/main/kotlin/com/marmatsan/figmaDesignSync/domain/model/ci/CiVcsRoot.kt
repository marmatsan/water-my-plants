package com.marmatsan.figmaDesignSync.domain.model.ci

/** Repository metadata supplied by an effective CI configuration. */
data class CiVcsRoot(
    val id: String,
    val name: String,
    val url: String,
    val defaultBranchRef: String,
    val branchSpec: List<String>
)
