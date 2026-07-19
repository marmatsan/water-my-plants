package com.marmatsan.figmaDesignSync.projectConfig

import com.marmatsan.figmaDesignSync.teamcityAdapter.TeamCityCompositeRunClient
import com.marmatsan.figmaDesignSync.teamcityAdapter.TeamCityCliClient
import com.marmatsan.figmaDesignSync.teamcityAdapter.TeamCityRestRunStarter
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Gradle entry point for validating or rerunning the official TeamCity Figma Sync pipeline. */
@DisableCachingByDefault(because = "Reads and mutates external TeamCity run state")
abstract class RerunTeamCityFigmaSyncTask : DefaultTask() {
    @get:Input
    abstract val serverUrl: Property<String>

    @get:Input
    abstract val validateOnly: Property<Boolean>

    @get:Input
    abstract val waitForCompletion: Property<Boolean>

    @get:Input
    abstract val pollIntervalSeconds: Property<Int>

    @get:Input
    abstract val timeoutMinutes: Property<Int>

    @get:Input
    abstract val buildTypeId: Property<String>

    @get:Input
    abstract val branch: Property<String>

    @TaskAction
    fun rerun() {
        val credentials = EnvironmentTeamCityAutomationCredentialsProvider().load(serverUrl.get())
        val cliClient = TeamCityCliClient(
            environment = mapOf(
                "TEAMCITY_URL" to credentials.serverUrl,
                "TEAMCITY_TOKEN" to credentials.teamCityToken,
                "TEAMCITY_HEADER_CF_ACCESS_TOKEN" to credentials.cloudflareAccessToken,
                "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID" to null,
                "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET" to null
            )
        )
        val client = TeamCityCompositeRunClient(
            readClient = cliClient,
            runStarter = TeamCityRestRunStarter(
                serverUrl = credentials.serverUrl,
                teamCityToken = credentials.teamCityToken,
                cloudflareAccessToken = credentials.cloudflareAccessToken
            )
        )
        val result = TeamCityFigmaSyncRerunner(
            teamCityClient = client,
            buildTypeId = buildTypeId.get(),
            branch = branch.get()
        ).rerun(
            TeamCityFigmaSyncRerunner.Request(
                validateOnly = validateOnly.get(),
                waitForCompletion = waitForCompletion.get(),
                pollIntervalSeconds = pollIntervalSeconds.get(),
                timeoutMinutes = timeoutMinutes.get()
            )
        )
        logger.lifecycle(
            "TeamCity Figma Sync: runId=${result.runId ?: "none"}, " +
                "branch=${result.branch}, state=${result.state}, reused=${result.reused}, " +
                "webUrl=${result.webUrl ?: "none"}"
        )
    }
}
