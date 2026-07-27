package com.marmatsan.figmaDocumentationSync.domain.model.impact

/**
 * Git comparison base and repository-relative paths changed from that base.
 *
 * @property comparisonBase resolved Git revision used as the comparison base.
 * @property changedPaths normalized repository-relative changed paths.
 */
data class RepositoryChangeSet(
    val comparisonBase: String?,
    val changedPaths: List<String>,
)
