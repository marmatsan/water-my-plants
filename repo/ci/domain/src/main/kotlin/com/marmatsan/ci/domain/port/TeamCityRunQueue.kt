package com.marmatsan.ci.domain.port

import com.marmatsan.ci.domain.model.TeamCityQueuedRun
import com.marmatsan.ci.domain.model.TeamCityRunRequest

/** Provider-neutral boundary for queueing one TeamCity run. */
fun interface TeamCityRunQueue {
    /**
     * Queues [request] and returns the identity reported by the provider.
     *
     * Implementations must reject transport failures and malformed provider
     * responses instead of fabricating a successful run.
     */
    fun queue(request: TeamCityRunRequest): TeamCityQueuedRun
}
