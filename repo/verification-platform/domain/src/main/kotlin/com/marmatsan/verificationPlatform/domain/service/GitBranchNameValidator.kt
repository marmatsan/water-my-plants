package com.marmatsan.verificationPlatform.domain.service

import com.marmatsan.verificationPlatform.domain.model.GitBranchValidation

/** Validates provider-neutral Git branch names for the trunk-based workflow. */
class GitBranchNameValidator {
    /**
     * Validates [branchRef] after removing supported local and remote prefixes.
     *
     * Synthetic `refs/pull/<number>/head` refs are accepted because TeamCity
     * also validates the source `refs/heads/<branch>` build for the same commit.
     *
     * @param branchRef branch name or full Git ref supplied by a local checkout
     * or CI provider.
     * @return normalized validation result with a stable failure explanation.
     */
    fun validate(branchRef: String): GitBranchValidation {
        val branch = normalize(branchRef)
        val providerManaged = PULL_REQUEST_REF.matches(branch)
        val valid = branch == MAIN_BRANCH ||
            providerManaged ||
            SHORT_LIVED_BRANCH.matches(branch) ||
            RELEASE_BRANCH.matches(branch)

        return GitBranchValidation(
            branch = branch,
            valid = valid,
            providerManaged = providerManaged,
            message = if (valid) {
                null
            } else {
                "Branch '${branch.ifEmpty { "<blank>" }}' violates the Git workflow. " +
                    "Expected main, feature/<kebab-case>, fix/<kebab-case>, " +
                    "chore/<kebab-case>, release/<x.y.z>, or hotfix/<kebab-case>."
            }
        )
    }

    private fun normalize(branchRef: String): String {
        val trimmed = branchRef.trim().replace('\\', '/')
        return when {
            trimmed.startsWith("refs/remotes/origin/") -> trimmed.removePrefix("refs/remotes/origin/")
            trimmed.startsWith("refs/heads/") -> trimmed.removePrefix("refs/heads/")
            trimmed.startsWith("origin/") -> trimmed.removePrefix("origin/")
            trimmed.startsWith("refs/pull/") -> trimmed.removePrefix("refs/")
            else -> trimmed
        }
    }

    private companion object {
        const val MAIN_BRANCH = "main"
        val SHORT_LIVED_BRANCH = Regex(
            "(?:feature|fix|chore|hotfix)/[a-z0-9]+(?:-[a-z0-9]+)*"
        )
        val RELEASE_BRANCH = Regex(
            "release/(?:0|[1-9][0-9]*)\\.(?:0|[1-9][0-9]*)\\.(?:0|[1-9][0-9]*)"
        )
        val PULL_REQUEST_REF = Regex("pull/[1-9][0-9]{0,}/head")
    }
}
