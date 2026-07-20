package com.marmatsan.verificationPlatform.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Declares whether an execution topology may affect active CI configuration. */
@Serializable
enum class CiTopologyActivation {
    /** The topology is diagnostic and must not be executed by an adapter. */
    @SerialName("preview-only")
    PREVIEW_ONLY,
}
