package com.marmatsan.verificationPlatform.domain.port.teamcity

import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityQueuedRun
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityRunRequest

/** Provider-neutral boundary for queueing one TeamCity run. */
fun interface TeamCityRunQueue {
    /**
     * Queues [request] and returns the identity reported by the provider.
     *
     * Implementations must reject transport failures and malformed provider
     * responses instead of fabricating a successful run.
     */
    fun queue(
        request: TeamCityRunRequest,
    ): TeamCityQueuedRun
}
