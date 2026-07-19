package com.marmatsan.ci.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class VerificationUnitId {
    @SerialName("documentation")
    DOCUMENTATION,

    @SerialName("repository-diff")
    REPOSITORY_DIFF,

    @SerialName("teamcity-dsl")
    TEAMCITY_DSL,

    @SerialName("figma-tooling")
    FIGMA_TOOLING,

    @SerialName("dependency-catalog")
    DEPENDENCY_CATALOG,

    @SerialName("gradle-verification")
    GRADLE_VERIFICATION,

    @SerialName("publish-reports")
    PUBLISH_REPORTS
}
