package com.marmatsan.projectConfig.project

import com.marmatsan.projectConfig.catalog.ProjectConfigCatalogState
import com.marmatsan.projectConfig.catalog.ProjectConfigCatalogs
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Project entry point that verifies the reusable Settings composition contract. */
class ProjectConfigGradlePlugin : Plugin<Project> {
    /** Verifies that the matching Settings plugin captured a catalog definition. */
    override fun apply(
        project: Project
    ) {
        val provider = ProjectConfigCatalogState.require(project.gradle)
        project.extensions.add(
            ProjectConfigCatalogs::class.java,
            "projectConfigCatalogs",
            ProjectConfigCatalogs(provider)
        )
    }
}
