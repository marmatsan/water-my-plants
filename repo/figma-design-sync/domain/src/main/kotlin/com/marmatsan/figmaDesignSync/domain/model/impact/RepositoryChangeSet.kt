package com.marmatsan.figmaDesignSync.domain.model.impact

/** Git comparison base and repository-relative paths changed from that base. */
data class RepositoryChangeSet(
    val comparisonBase: String?,
    val changedPaths: List<String>
)
