package com.marmatsan.figmaDocumentationSync.teamcity.operations.gradle

import com.marmatsan.figmaDocumentationSync.teamcity.operations.task.PrepareTeamCityFigmaSyncHandoffTask
import com.marmatsan.figmaDocumentationSync.teamcity.operations.task.RerunTeamCityFigmaSyncTask
import com.marmatsan.figmaDocumentationSync.teamcity.operations.task.UploadCanonicalFigmaPayloadTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.io.File

/** Registers operational tasks for supervised TeamCity and Figma coordination. */
internal class FigmaTeamCityOperationsTasksRegistrar(
    private val project: Project,
    private val extension: FigmaTeamCityOperationsExtension
) {
    /** Registers handoff preparation, canonical upload, and rerun tasks. */
    fun register() {
        project.tasks.register<PrepareTeamCityFigmaSyncHandoffTask>("prepareTeamCityFigmaSyncHandoff") {
            group = "documentation"
            description = "Downloads or opens canonical TeamCity artifacts and prepares the Figma MCP handoff."
            buildId.convention(
                project.providers.gradleProperty("figmaTeamCityBuildId").map(
                    String::toLong
                )
            )
            artifactDirectory.set(
                project.layout.dir(
                    project.providers.gradleProperty("figmaArtifactDirectory").map(
                        ::File
                    )
                )
            )
            destinationRoot.convention(
                project.layout
                    .dir(
                        project.providers.gradleProperty("figmaHandoffDestinationRoot").map(
                            ::File
                        )
                    ).orElse(extension.destinationRoot)
            )
            expectedGitSha.convention(project.providers.gradleProperty("figmaExpectedGitSha"))
            mainBranchAliases.set(extension.mainBranchAliases)
            requiredBuildTypeName.set(extension.requiredBuildTypeName)
        }

        project.tasks.register<UploadCanonicalFigmaPayloadTask>("uploadCanonicalFigmaPayload") {
            group = "documentation"
            description = "Uploads the verified PNG from one successful main TeamCity Figma artifact set."
            buildId.convention(
                project.providers.gradleProperty("figmaTeamCityBuildId").map(
                    String::toLong
                )
            )
            artifactDirectory.set(
                project.layout.dir(
                    project.providers.gradleProperty("figmaArtifactDirectory").map(
                        ::File
                    )
                )
            )
            uploadUrl.convention(project.providers.gradleProperty("figmaMcpUploadUrl"))
            destinationRoot.convention(
                project.layout
                    .dir(
                        project.providers.gradleProperty("figmaHandoffDestinationRoot").map(
                            ::File
                        )
                    ).orElse(extension.destinationRoot)
            )
            projectDirectory.set(project.layout.projectDirectory)
            expectedGitSha.convention(project.providers.gradleProperty("figmaExpectedGitSha"))
            mainBranchAliases.set(extension.mainBranchAliases)
            requiredBuildTypeName.set(extension.requiredBuildTypeName)
        }

        project.tasks.register<RerunTeamCityFigmaSyncTask>("rerunTeamCityFigmaSync") {
            group = "documentation"
            description = "Validates or reruns the canonical TeamCity Figma Sync pipeline."
            serverUrl.convention(
                project.providers
                    .gradleProperty("figmaTeamCityServerUrl")
                    .orElse(extension.serverUrl)
            )
            validateOnly.convention(
                project.providers
                    .gradleProperty("figmaTeamCityValidateOnly")
                    .map(
                        String::toBoolean
                    ).orElse(false)
            )
            waitForCompletion.convention(
                project.providers
                    .gradleProperty("figmaTeamCityWait")
                    .map(
                        String::toBoolean
                    ).orElse(false)
            )
            pollIntervalSeconds.convention(
                project.providers
                    .gradleProperty("figmaTeamCityPollIntervalSeconds")
                    .map(
                        String::toInt
                    ).orElse(10)
            )
            timeoutMinutes.convention(
                project.providers
                    .gradleProperty("figmaTeamCityTimeoutMinutes")
                    .map(
                        String::toInt
                    ).orElse(60)
            )
            buildTypeId.set(extension.buildTypeId)
            branch.set(extension.branch)
        }
    }
}
