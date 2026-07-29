package com.marmatsan.verificationPlatform.plugin.registration

import com.marmatsan.verificationPlatform.plugin.extension.VerificationPlatformExtension
import com.marmatsan.verificationPlatform.plugin.task.ci.GenerateCiPlanTask
import com.marmatsan.verificationPlatform.plugin.task.documentation.CheckDocumentationTask
import com.marmatsan.verificationPlatform.plugin.task.teamcity.CheckTeamCityDslTask
import com.marmatsan.verificationPlatform.plugin.task.teamcity.PrepareTeamCityCiPlanTask
import com.marmatsan.verificationPlatform.plugin.task.teamcity.RunTeamCityInfrastructureHealthTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Registers tasks that adapt repository verification to TeamCity. */
internal class TeamCityVerificationTasksRegistrar(
    private val project: Project,
    private val extension: VerificationPlatformExtension,
    private val generateCiPlan: TaskProvider<GenerateCiPlanTask>,
    private val checkDocumentation: TaskProvider<CheckDocumentationTask>
) {
    /** Registers TeamCity checks and operations, returning the DSL verification gate. */
    fun register(): TaskProvider<CheckTeamCityDslTask> {
        val checkTeamCityDsl =
            project.tasks.registerVerificationTask(
                "checkTeamCityDsl",
                CheckTeamCityDslTask::class.java,
                "Generates and validates the effective TeamCity Kotlin DSL."
            ) { task ->
                task.dependsOn(checkDocumentation)
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.teamCityPom.set(extension.teamCity.pom)
                task.generatedConfigurationDirectory.set(extension.teamCity.generatedConfigurationDirectory)
                task.pipelineBuildTypeId.set(extension.teamCity.pipelineBuildTypeId)
                task.gateBuildTypeId.set(extension.teamCity.gateBuildTypeId)
                task.authoritativeStatusName.set(extension.teamCity.authoritativeStatusName)
            }

        project.tasks.registerVerificationTask(
            "prepareTeamCityCiPlan",
            PrepareTeamCityCiPlanTask::class.java,
            "Generates the CI plan and exports its allow-listed TeamCity parameters."
        ) { task ->
            task.dependsOn(generateCiPlan)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
        }

        project.tasks.registerVerificationTask(
            "runTeamCityInfrastructureHealth",
            RunTeamCityInfrastructureHealthTask::class.java,
            "Queues the non-gating TeamCity Infrastructure Health pipeline."
        ) { task ->
            task.serverUrl.convention(
                project.providers
                    .gradleProperty("teamCityInfrastructureHealthServerUrl")
                    .orElse("http://127.0.0.1:8111")
            )
            task.buildTypeId.convention(
                project.providers
                    .gradleProperty("teamCityInfrastructureHealthBuildTypeId")
                    .orElse(extension.teamCity.infrastructureHealthBuildTypeId)
            )
            task.branch.convention(
                project.providers.gradleProperty("teamCityInfrastructureHealthBranch").orElse("main")
            )
            task.teamCityToken.convention(project.providers.environmentVariable("TEAMCITY_TOKEN"))
        }

        return checkTeamCityDsl
    }
}
