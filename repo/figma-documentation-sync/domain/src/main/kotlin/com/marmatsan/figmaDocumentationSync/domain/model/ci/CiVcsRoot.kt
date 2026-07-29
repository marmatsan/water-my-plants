package com.marmatsan.figmaDocumentationSync.domain.model.ci

/**
 * Repository metadata supplied by an effective CI configuration.
 *
 * @property id stable adapter-independent repository identity.
 * @property name human-readable repository name.
 * @property url canonical repository URL.
 * @property defaultBranchRef full default-branch reference.
 * @property branchSpec additional branch-selection expressions.
 */
data class CiVcsRoot(
    val id: String,
    val name: String,
    val url: String,
    val defaultBranchRef: String,
    val branchSpec: List<String>
)
