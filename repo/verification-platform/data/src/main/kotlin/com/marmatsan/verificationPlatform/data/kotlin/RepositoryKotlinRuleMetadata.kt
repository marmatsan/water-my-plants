package com.marmatsan.verificationPlatform.data.kotlin

import com.pinterest.ktlint.rule.engine.core.api.Rule.About

/** Shared ownership metadata for repository-provided KtLint rules. */
internal object RepositoryKotlinRuleMetadata {
    /** Canonical repository and issue tracker links exposed by every rule. */
    val about =
        About(
            maintainer = "Repository Verification",
            repositoryUrl = "https://github.com/marmatsan/water-my-plants",
            issueTrackerUrl = "https://github.com/marmatsan/water-my-plants/issues"
        )
}
