package com.marmatsan.ci.domain.service

import com.marmatsan.ci.domain.model.TeamCityQueuedRun
import com.marmatsan.ci.domain.model.TeamCityRunRequest
import com.marmatsan.ci.domain.port.TeamCityRunQueue

/** Validates and queues an explicit TeamCity build configuration on one branch. */
class QueueTeamCityRun(
    private val runQueue: TeamCityRunQueue
) {
    fun execute(request: TeamCityRunRequest): TeamCityQueuedRun {
        require(BUILD_TYPE_ID.matches(request.buildTypeId)) {
            "TeamCity build type id contains unsupported characters."
        }
        require(BRANCH.matches(request.branch)) {
            "TeamCity branch contains unsupported characters."
        }
        return runQueue.queue(request)
    }

    private companion object {
        val BUILD_TYPE_ID = Regex("[A-Za-z0-9_.-]+")
        val BRANCH = Regex("[A-Za-z0-9_./-]+")
    }
}
