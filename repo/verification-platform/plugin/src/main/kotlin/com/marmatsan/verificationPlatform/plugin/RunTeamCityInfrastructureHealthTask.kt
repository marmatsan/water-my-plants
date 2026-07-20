package com.marmatsan.verificationPlatform.plugin

import com.marmatsan.verificationPlatform.data.teamcity.TeamCityRestRunQueue
import com.marmatsan.verificationPlatform.domain.model.TeamCityRunRequest
import com.marmatsan.verificationPlatform.domain.service.QueueTeamCityRun
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Queues the non-gating Infrastructure Health pipeline through a trusted TeamCity origin. */
@DisableCachingByDefault(
    because = "Queues an external TeamCity pipeline",
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
        val result =
            QueueTeamCityRun(
                runQueue =
                    TeamCityRestRunQueue(
                        serverUrl = serverUrl.get(),
                        teamCityToken = teamCityToken.get(),
                    ),
            ).execute(
                TeamCityRunRequest(
                    buildTypeId = buildTypeId.get(),
                    branch = branch.get(),
                ),
            )
        logger.lifecycle(
            "TeamCity Infrastructure Health queued: runId=${result.id}, " +
                "branch=${result.branch}, state=${result.state}, webUrl=${result.webUrl.orEmpty()}",
        )
    }
}
