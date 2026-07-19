package com.marmatsan.ci.plugin

import com.marmatsan.ci.data.teamcity.TeamCityRestRunQueue
import com.marmatsan.ci.domain.model.TeamCityRunRequest
import com.marmatsan.ci.domain.service.QueueTeamCityRun
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Queues the non-gating Infrastructure Health pipeline through a trusted TeamCity origin. */
@DisableCachingByDefault(because = "Queues an external TeamCity pipeline")
abstract class RunTeamCityInfrastructureHealthTask : DefaultTask() {
    @get:Input
    abstract val serverUrl: Property<String>

    @get:Input
    abstract val buildTypeId: Property<String>

    @get:Input
    abstract val branch: Property<String>

    @get:Internal
    abstract val teamCityToken: Property<String>

    @TaskAction
    fun queue() {
        val result = QueueTeamCityRun(
            TeamCityRestRunQueue(
                serverUrl = serverUrl.get(),
                teamCityToken = teamCityToken.get()
            )
        ).execute(
            TeamCityRunRequest(
                buildTypeId = buildTypeId.get(),
                branch = branch.get()
            )
        )
        logger.lifecycle(
            "TeamCity Infrastructure Health queued: runId=${result.id}, " +
                "branch=${result.branch}, state=${result.state}, webUrl=${result.webUrl.orEmpty()}"
        )
    }
}
