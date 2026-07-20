package com.marmatsan.verificationPlatform.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Highest-level classification assigned to one repository change. */
@Serializable
enum class CiScope {
    /** Every changed path belongs to an approved documentation surface. */
    @SerialName("documentation-only")
    DOCUMENTATION_ONLY,

    /** TeamCity configuration or its adapters changed. */
    @SerialName("teamcity")
    TEAMCITY,

    /** Figma Documentation Sync implementation or tooling changed. */
    @SerialName("figma-tooling")
    FIGMA_TOOLING,

    /** Dependency catalog, build conventions, or root Gradle files changed. */
    @SerialName("dependency-infrastructure")
    DEPENDENCY_INFRASTRUCTURE,

    /** One or more application modules changed. */
    @SerialName("application")
    APPLICATION,

    /** The change combines more than one known scope. */
    @SerialName("mixed")
    MIXED,

    /** At least one path or graph condition cannot be classified safely. */
    @SerialName("unknown")
    UNKNOWN,
}
