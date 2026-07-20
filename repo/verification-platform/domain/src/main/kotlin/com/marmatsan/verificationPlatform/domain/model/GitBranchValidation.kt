package com.marmatsan.verificationPlatform.domain.model

/**
 * Result of validating one Git branch against the repository workflow.
 *
 * @property branch normalized branch name used for validation.
 * @property valid whether the branch satisfies the repository policy.
 * @property providerManaged whether the ref is a synthetic pull request ref
 * created by the CI provider rather than a developer-owned branch.
 * @property message validation failure detail, or `null` when valid.
 */
data class GitBranchValidation(
    val branch: String,
    val valid: Boolean,
    val providerManaged: Boolean,
    val message: String?,
)
