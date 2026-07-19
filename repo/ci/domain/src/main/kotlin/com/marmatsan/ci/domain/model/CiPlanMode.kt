package com.marmatsan.ci.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CiPlanMode {
    @SerialName("observation")
    OBSERVATION,

    @SerialName("enforced")
    ENFORCED
}
