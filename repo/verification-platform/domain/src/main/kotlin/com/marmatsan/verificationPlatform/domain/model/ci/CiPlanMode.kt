package com.marmatsan.verificationPlatform.domain.model.ci

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Controls whether a generated CI plan is informational or authoritative. */
@Serializable
enum class CiPlanMode {
    /** Reports the selected work without allowing an adapter to enforce it. */
    @SerialName("observation")
    OBSERVATION,

    /** Allows a reviewed provider adapter to execute the selected work. */
    @SerialName("enforced")
    ENFORCED,
}
