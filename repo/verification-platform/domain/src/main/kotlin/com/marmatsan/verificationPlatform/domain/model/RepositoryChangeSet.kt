package com.marmatsan.verificationPlatform.domain.model

/**
 * Committed repository diff used as the input to CI planning.
 *
 * @property comparisonBase lower Git revision of the comparison, or `null`
 * when unavailable.
 * @property head Git revision whose committed state is being verified.
 * @property changedFiles repository-relative paths changed between the two
 * revisions.
 */
data class RepositoryChangeSet(
    val comparisonBase: String?,
    val head: String,
    val changedFiles: List<String>
)
