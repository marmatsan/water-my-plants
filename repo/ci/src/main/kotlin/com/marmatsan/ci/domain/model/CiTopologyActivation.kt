package com.marmatsan.ci.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CiTopologyActivation {
    @SerialName("preview-only")
    PREVIEW_ONLY
}
