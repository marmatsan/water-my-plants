package com.marmatsan.verificationPlatform.domain.model

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

    /** Verify Figma Documentation Sync tooling and contracts. */
    @SerialName("figma-tooling")
    FIGMA_TOOLING,

    /** Verify dependency catalogs and shared Gradle infrastructure. */
    @SerialName("dependency-catalog")
    DEPENDENCY_CATALOG,

    /** Execute the selected Gradle verification tasks. */
    @SerialName("gradle-verification")
    GRADLE_VERIFICATION,

    /** Publish plans and verification evidence through the final CI gate. */
    @SerialName("publish-reports")
    PUBLISH_REPORTS
}
