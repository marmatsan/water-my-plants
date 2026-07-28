package com.marmatsan.verificationPlatform.domain.service.teamcity

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.marmatsan.verificationPlatform.domain.model.teamcity.QueueTeamCityRunError
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
     * Invalid identifiers and expected provider failures are returned through
     * the capability-owned [QueueTeamCityRunError] channel.
     */
    fun execute(
        request: TeamCityRunRequest,
    ): Result<TeamCityQueuedRun, QueueTeamCityRunError> =
        when {
            !BUILD_TYPE_ID.matches(request.buildTypeId) -> {
                Err(QueueTeamCityRunError.UnsupportedBuildType)
            }

            !BRANCH.matches(request.branch) -> {
                Err(QueueTeamCityRunError.UnsupportedBranch)
            }

            else -> {
                runQueue.queue(
                    request = request,
                )
            }
        }

    private companion object {
        val BUILD_TYPE_ID = Regex("[A-Za-z0-9_.-]+")
        val BRANCH = Regex("[A-Za-z0-9_./-]+")
    }
}
