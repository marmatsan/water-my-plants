package com.marmatsan.verificationPlatform.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Scheduling strategy selected from the number of available agents. */
@Serializable
enum class CiTopologyMode {
    /** One agent executes every required unit in a single ordered lane. */
    @SerialName("single-agent-sequential")
    SINGLE_AGENT_SEQUENTIAL,

    /** Independent units are separated into lanes that may run concurrently. */
    @SerialName("multi-agent-parallel")
    MULTI_AGENT_PARALLEL,
}
