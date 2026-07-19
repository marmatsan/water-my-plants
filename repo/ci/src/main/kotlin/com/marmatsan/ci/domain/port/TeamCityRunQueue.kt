package com.marmatsan.ci.domain.port

import com.marmatsan.ci.domain.model.TeamCityQueuedRun
import com.marmatsan.ci.domain.model.TeamCityRunRequest

/** Provider-neutral boundary for queueing one TeamCity run. */
fun interface TeamCityRunQueue {
    fun queue(request: TeamCityRunRequest): TeamCityQueuedRun
}
