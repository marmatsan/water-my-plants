package com.marmatsan.verificationPlatform.domain.port.teamcity

import com.github.michaelbull.result.Result
import com.marmatsan.verificationPlatform.domain.model.teamcity.QueueTeamCityRunError
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityQueuedRun
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityRunRequest

/** Provider-neutral boundary for queueing one TeamCity run. */
fun interface TeamCityRunQueue {
    /**
     * Queues [request] and returns the identity reported by the provider.
     *
     * Implementations translate expected transport, provider, and response
     * failures into [QueueTeamCityRunError] instead of fabricating a run or
     * leaking infrastructure exceptions.
     */
    fun queue(
        request: TeamCityRunRequest,
    ): Result<TeamCityQueuedRun, QueueTeamCityRunError>
}
