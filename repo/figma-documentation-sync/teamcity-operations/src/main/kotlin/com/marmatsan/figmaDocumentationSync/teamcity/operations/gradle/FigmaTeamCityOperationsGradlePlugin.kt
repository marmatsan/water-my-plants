package com.marmatsan.figmaDocumentationSync.teamcity.operations.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/** Gradle entry point for optional, supervised TeamCity Figma operations. */
class FigmaTeamCityOperationsGradlePlugin : Plugin<Project> {
    /** Creates the consumer configuration and delegates task registration. */
    override fun apply(
        project: Project
    ) {
        val extension =
            project.extensions.create<FigmaTeamCityOperationsExtension>(
                "figmaTeamCityOperations"
            )
        FigmaTeamCityOperationsTasksRegistrar(
            project = project,
            extension = extension
        ).register()
    }
}
