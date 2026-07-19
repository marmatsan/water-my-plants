package com.marmatsan.ci.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CiTopologyMode {
    @SerialName("single-agent-sequential")
    SINGLE_AGENT_SEQUENTIAL,

    @SerialName("multi-agent-parallel")
    MULTI_AGENT_PARALLEL
}
