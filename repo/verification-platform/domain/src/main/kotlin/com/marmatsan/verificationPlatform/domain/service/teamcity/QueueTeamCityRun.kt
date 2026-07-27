package com.marmatsan.verificationPlatform.domain.service.teamcity

import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityQueuedRun
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityRunRequest
import com.marmatsan.verificationPlatform.domain.port.teamcity.TeamCityRunQueue

/**
 * Validates and queues an explicit TeamCity build configuration on one branch.
 *
 * @property runQueue provider boundary used only after identifiers satisfy the
 * domain allow-list.
 */
class QueueTeamCityRun(
    private val runQueue: TeamCityRunQueue,
) {
    /**
     * Validates [request] and delegates it to the configured queue adapter.
     *
     * @throws IllegalArgumentException when the build type or branch contains
     * characters outside the reviewed allow-list.
     */
    fun execute(
        request: TeamCityRunRequest,
    ): TeamCityQueuedRun {
        require(BUILD_TYPE_ID.matches(request.buildTypeId)) {
            "TeamCity build type id contains unsupported characters."
        }
        require(BRANCH.matches(request.branch)) {
            "TeamCity branch contains unsupported characters."
        }
        return runQueue.queue(
            request = request,
        )
    }

    private companion object {
        val BUILD_TYPE_ID = Regex("[A-Za-z0-9_.-]+")
        val BRANCH = Regex("[A-Za-z0-9_./-]+")
    }
}
