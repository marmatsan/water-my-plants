package com.marmatsan.figmaDocumentationSync.teamcity.operations.task

import com.marmatsan.figmaDocumentationSync.teamcity.operations.auth.EnvironmentTeamCityAutomationCredentialsProvider
import com.marmatsan.figmaDocumentationSync.teamcity.operations.sync.TeamCityFigmaSyncRerunner
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityCliClient
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityCompositeRunClient
import com.marmatsan.figmaDocumentationSync.teamcityAdapter.TeamCityRestRunStarter
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/** Gradle entry point for validating or rerunning the canonical TeamCity Figma Sync pipeline. */
@DisableCachingByDefault(
    because = "Reads and mutates external TeamCity run state"
)
abstract class RerunTeamCityFigmaSyncTask : DefaultTask() {
    /** Public TeamCity HTTPS origin protected by Cloudflare Access. */
    @get:Input
    abstract val serverUrl: Property<String>

    /** Whether to validate access without queueing a run. */
    @get:Input
    abstract val validateOnly: Property<Boolean>

    /** Whether to wait for and require successful run completion. */
    @get:Input
    abstract val waitForCompletion: Property<Boolean>

    /** Bounded interval between TeamCity status polls. */
    @get:Input
    abstract val pollIntervalSeconds: Property<Int>

    /** Bounded maximum number of minutes to await completion. */
    @get:Input
    abstract val timeoutMinutes: Property<Int>

    /** Canonical Figma Sync TeamCity build configuration id. */
    @get:Input
    abstract val buildTypeId: Property<String>

    /** Canonical branch on which the pipeline may be queued. */
    @get:Input
    abstract val branch: Property<String>

    /** Resolves credentials and performs one idempotent validation or rerun. */
    @TaskAction
    fun rerun() {
        val credentials = EnvironmentTeamCityAutomationCredentialsProvider().load(serverUrl.get())
        val cliClient =
            TeamCityCliClient(
                environment =
                    mapOf(
                        "TEAMCITY_URL" to credentials.serverUrl,
                        "TEAMCITY_TOKEN" to credentials.teamCityToken,
                        "TEAMCITY_HEADER_CF_ACCESS_TOKEN" to credentials.cloudflareAccessToken,
                        "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID" to null,
                        "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET" to null
                    )
            )
        val client =
            TeamCityCompositeRunClient(
                readClient = cliClient,
                runStarter =
                    TeamCityRestRunStarter(
                        serverUrl = credentials.serverUrl,
                        teamCityToken = credentials.teamCityToken,
                        cloudflareAccessToken = credentials.cloudflareAccessToken
                    )
            )
        val result =
            TeamCityFigmaSyncRerunner(
                teamCityClient = client,
                buildTypeId = buildTypeId.get(),
                branch = branch.get()
            ).rerun(
                request =
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
