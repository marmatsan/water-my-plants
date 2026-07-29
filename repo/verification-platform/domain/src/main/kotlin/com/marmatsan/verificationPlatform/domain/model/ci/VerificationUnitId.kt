package com.marmatsan.verificationPlatform.domain.model.ci

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Stable identifiers for work that reviewed CI adapters may execute. */
@Serializable
enum class VerificationUnitId {
    /** Validate the current branch against the trunk-based Git workflow. */
    @SerialName("git-workflow")
    GIT_WORKFLOW,

    /** Validate documentation structure, metadata, links, and coverage. */
    @SerialName("documentation")
    DOCUMENTATION,

    /** Validate that every committed path is covered by repository policy. */
    @SerialName("repository-diff")
    REPOSITORY_DIFF,

    /** Compile and validate TeamCity Kotlin DSL settings. */
    @SerialName("teamcity-dsl")
    TEAMCITY_DSL,

    /** Verify repository-configured tooling and contracts. */
    @SerialName("tooling")
    TOOLING,

    /** Verify repository-configured build infrastructure. */
    @SerialName("build-infrastructure")
    BUILD_INFRASTRUCTURE,

    /** Verify staged Maven and npm-facing artifacts through an external consumer. */
    @SerialName("portable-distribution")
    PORTABLE_DISTRIBUTION,

    /** Execute the selected Gradle verification tasks. */
    @SerialName("gradle-verification")
    GRADLE_VERIFICATION,

    /** Publish plans and verification evidence through the final CI gate. */
    @SerialName("publish-reports")
    PUBLISH_REPORTS
}
