package com.marmatsan.verificationPlatform.plugin

import com.marmatsan.verificationPlatform.plugin.extension.VerificationPlatformExtension
import com.marmatsan.verificationPlatform.plugin.registration.CiVerificationTasksRegistrar
import com.marmatsan.verificationPlatform.plugin.registration.ConfigurableVerificationTasksRegistrar
import com.marmatsan.verificationPlatform.plugin.registration.RepositoryVerificationTasksRegistrar
import com.marmatsan.verificationPlatform.plugin.registration.TeamCityVerificationTasksRegistrar
import com.marmatsan.verificationPlatform.plugin.registration.VerificationLifecycleConfigurer
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Registers the reviewed Gradle entry points exposed by the verification platform. */
class VerificationPlatformPlugin : Plugin<Project> {
    /**
     * Applies the verification-platform composition root to [project].
     *
     * The plugin must be applied to the root project because module-graph
     * discovery and report locations are repository-wide concerns.
     */
    override fun apply(
        project: Project,
    ) {
        require(project == project.rootProject) {
            "com.marmatsan.verificationPlatform must be applied to the root project."
        }

        val configurableTasks = ConfigurableVerificationTasksRegistrar(project).register()
        val extension =
            VerificationPlatformExtension(
                project = project,
                generateCiPlan = configurableTasks.generateCiPlan,
                checkIncludedBuildVersions = configurableTasks.checkIncludedBuildVersions,
                checkModuleBoundaries = configurableTasks.checkModuleBoundaries,
                checkTypedResultUsage = configurableTasks.checkTypedResultUsage,
            )
        project.extensions.add(
            "verificationPlatform",
            extension,
        )

        val checkDocumentation =
            RepositoryVerificationTasksRegistrar(
                project = project,
                generateCiPlan = configurableTasks.generateCiPlan,
            ).register()
        CiVerificationTasksRegistrar(
            project = project,
            generateCiPlan = configurableTasks.generateCiPlan,
        ).register()
        val checkTeamCityDsl =
            TeamCityVerificationTasksRegistrar(
                project = project,
                extension = extension,
                generateCiPlan = configurableTasks.generateCiPlan,
                checkDocumentation = checkDocumentation,
            ).register()
        VerificationLifecycleConfigurer(
            project = project,
            configurableTasks = configurableTasks,
            checkDocumentation = checkDocumentation,
            checkTeamCityDsl = checkTeamCityDsl,
        ).configure()
    }
}
