package com.marmatsan.ci.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CiScope {
    @SerialName("documentation-only")
    DOCUMENTATION_ONLY,

    @SerialName("teamcity")
    TEAMCITY,

    @SerialName("figma-tooling")
    FIGMA_TOOLING,

    @SerialName("dependency-infrastructure")
    DEPENDENCY_INFRASTRUCTURE,

    @SerialName("application")
    APPLICATION,

    @SerialName("mixed")
    MIXED,

    @SerialName("unknown")
    UNKNOWN
}
