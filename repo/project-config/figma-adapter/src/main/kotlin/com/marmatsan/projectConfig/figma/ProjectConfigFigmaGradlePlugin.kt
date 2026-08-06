package com.marmatsan.projectConfig.figma

import com.marmatsan.figmaDocumentationSync.plugin.gradle.FigmaDocumentationSyncGradlePlugin
import com.marmatsan.projectConfig.project.ProjectConfigGradlePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Optional composition entry point from reusable project configuration to Figma sync. */
class ProjectConfigFigmaGradlePlugin : Plugin<Project> {
    /** Applies both portable capabilities and registers their catalog adapter. */
    override fun apply(
        project: Project
    ) {
        project.pluginManager.apply(ProjectConfigGradlePlugin::class.java)
        project.pluginManager.apply(FigmaDocumentationSyncGradlePlugin::class.java)
        ProjectConfigFigmaRegistration(project).register()
    }
}
