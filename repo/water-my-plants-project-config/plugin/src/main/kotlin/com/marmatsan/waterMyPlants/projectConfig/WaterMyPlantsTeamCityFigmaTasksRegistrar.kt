package com.marmatsan.waterMyPlants.projectConfig

import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.io.File

/** Registers Water My Plants operational tasks for supervised TeamCity/Figma coordination. */
internal class WaterMyPlantsTeamCityFigmaTasksRegistrar(
    private val project: Project,
) {
    /** Registers handoff preparation, canonical upload, and rerun tasks. */
    fun register() {
        project.tasks.register<PrepareTeamCityFigmaSyncHandoffTask>("prepareTeamCityFigmaSyncHandoff") {
            group = "documentation"
            description = "Downloads or opens canonical TeamCity artifacts and prepares the Figma MCP handoff."
            buildId.convention(
                project.providers.gradleProperty("figmaTeamCityBuildId").map(
                    String::toLong,
                ),
            )
            artifactDirectory.set(
                project.layout.dir(
                    project.providers.gradleProperty("figmaArtifactDirectory").map(
                        ::File,
                    ),
                ),
            )
            destinationRoot.convention(
                project.layout
                    .dir(
                        project.providers.gradleProperty("figmaHandoffDestinationRoot").map(
                            ::File,
                        ),
                    ).orElse(project.layout.projectDirectory.dir("tmp/teamcity")),
            )
            expectedGitSha.convention(project.providers.gradleProperty("figmaExpectedGitSha"))
            mainBranchAliases.set(MAIN_BRANCH_ALIASES)
            requiredBuildTypeName.set("Generate main design model")
        }

        project.tasks.register<UploadCanonicalFigmaPayloadTask>("uploadCanonicalFigmaPayload") {
            group = "documentation"
            description = "Uploads the verified PNG from one successful main TeamCity Figma artifact set."
            buildId.convention(
                project.providers.gradleProperty("figmaTeamCityBuildId").map(
                    String::toLong,
                ),
            )
            artifactDirectory.set(
                project.layout.dir(
                    project.providers.gradleProperty("figmaArtifactDirectory").map(
                        ::File,
                    ),
                ),
            )
            uploadUrl.convention(project.providers.gradleProperty("figmaMcpUploadUrl"))
            destinationRoot.convention(
                project.layout
                    .dir(
                        project.providers.gradleProperty("figmaHandoffDestinationRoot").map(
                            ::File,
                        ),
                    ).orElse(project.layout.projectDirectory.dir("tmp/teamcity")),
            )
            projectDirectory.set(project.layout.projectDirectory)
            expectedGitSha.convention(project.providers.gradleProperty("figmaExpectedGitSha"))
            mainBranchAliases.set(MAIN_BRANCH_ALIASES)
            requiredBuildTypeName.set("Generate main design model")
        }

        project.tasks.register<RerunTeamCityFigmaSyncTask>("rerunTeamCityFigmaSync") {
            group = "documentation"
            description = "Validates or reruns the canonical TeamCity Figma Sync pipeline."
            serverUrl.convention(
                project.providers
                    .gradleProperty("figmaTeamCityServerUrl")
                    .orElse("https://teamcity.marmatsan.dev"),
            )
            validateOnly.convention(
                project.providers
                    .gradleProperty("figmaTeamCityValidateOnly")
                    .map(
                        String::toBoolean,
                    ).orElse(false),
            )
            waitForCompletion.convention(
                project.providers
                    .gradleProperty("figmaTeamCityWait")
                    .map(
                        String::toBoolean,
                    ).orElse(false),
            )
            pollIntervalSeconds.convention(
                project.providers
                    .gradleProperty("figmaTeamCityPollIntervalSeconds")
                    .map(
                        String::toInt,
                    ).orElse(10),
            )
            timeoutMinutes.convention(
                project.providers
                    .gradleProperty("figmaTeamCityTimeoutMinutes")
                    .map(
                        String::toInt,
                    ).orElse(60),
            )
            buildTypeId.set("WaterMyPlants_WaterMyPlantsFigmaSync")
            branch.set("main")
        }
    }

    private companion object {
        val MAIN_BRANCH_ALIASES =
            listOf(
                "main",
                "<default>",
                "refs/heads/main",
            )
    }
}
