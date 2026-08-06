package com.marmatsan.figmaDocumentationSync.plugin.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

/** Composes the focused task registrars that expose the Figma documentation-sync Gradle API. */
@Suppress("unused")
class FigmaDocumentationSyncGradlePlugin : Plugin<Project> {
    /** Creates the extension and delegates task registration to focused collaborators. */
    override fun apply(
        project: Project
    ) {
        project.pluginManager.apply("base")
        val context =
            FigmaPluginContext(
                project = project,
                extension = project.extensions.create<figmaDocumentationSyncExtension>("figmaDocumentationSync")
            )

        FigmaModelTasksRegistrar(context).register()
        FigmaMcpTasksRegistrar(context).register()
        FigmaVerificationTasksRegistrar(context).register()
        CanonicalFigmaSyncTasksRegistrar(context).register()
        FigmaWriterTasksRegistrar(context).register()
    }
}
