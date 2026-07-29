package com.marmatsan.verificationPlatform.plugin.task.teamcity

import com.github.michaelbull.result.fold
import com.marmatsan.verificationPlatform.data.teamcity.TeamCityRestRunQueue
import com.marmatsan.verificationPlatform.domain.model.teamcity.QueueTeamCityRunError
import com.marmatsan.verificationPlatform.domain.model.teamcity.TeamCityRunRequest
import com.marmatsan.verificationPlatform.domain.service.teamcity.QueueTeamCityRun
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Queues the non-gating Infrastructure Health pipeline through a trusted TeamCity origin. */
@DisableCachingByDefault(
    because = "Queues an external TeamCity pipeline"
)
abstract class RunTeamCityInfrastructureHealthTask : DefaultTask() {
    /** Trusted TeamCity origin used by the REST adapter. */
    @get:Input
    abstract val serverUrl: Property<String>

    /** Stable TeamCity build configuration identifier to queue. */
    @get:Input
    abstract val buildTypeId: Property<String>

    /** Branch specification assigned to the queued build. */
    @get:Input
    abstract val branch: Property<String>

    /** Bearer token read from the environment and intentionally excluded from inputs. */
    @get:Internal
    abstract val teamCityToken: Property<String>

    /** Validates the request and queues the configured non-gating build. */
    @TaskAction
    fun queue() {
        QueueTeamCityRun(
            runQueue =
                TeamCityRestRunQueue(
                    serverUrl = serverUrl.get(),
                    teamCityToken = teamCityToken.get()
                )
        ).execute(
            TeamCityRunRequest(
                buildTypeId = buildTypeId.get(),
                branch = branch.get()
            )
        ).fold(
            success = { result ->
                logger.lifecycle(
                    "TeamCity Infrastructure Health queued: runId=${result.id}, " +
                        "branch=${result.branch}, state=${result.state}, webUrl=${result.webUrl.orEmpty()}"
                )
            },
            failure = { error ->
                throw GradleException(error.gradleMessage())
            }
        )
    }
}

private fun QueueTeamCityRunError.gradleMessage(): String =
    when (this) {
        QueueTeamCityRunError.UnsupportedBuildType -> {
            "TeamCity build type id contains unsupported characters."
        }

        QueueTeamCityRunError.UnsupportedBranch -> {
            "TeamCity branch contains unsupported characters."
        }

        is QueueTeamCityRunError.RequestRejected -> {
            "TeamCity REST queue request failed with HTTP $statusCode: $responseBody"
        }

        QueueTeamCityRunError.InvalidResponse -> {
            "TeamCity REST returned an invalid response."
        }

        is QueueTeamCityRunError.Unavailable -> {
            "TeamCity REST queue request could not reach the trusted endpoint: $detail"
        }
    }
