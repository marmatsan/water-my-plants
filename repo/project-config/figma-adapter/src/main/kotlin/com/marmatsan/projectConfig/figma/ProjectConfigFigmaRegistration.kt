package com.marmatsan.projectConfig.figma

import com.marmatsan.figmaDocumentationSync.data.json.catalog.DependencyCatalogTreesJson
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import com.marmatsan.figmaDocumentationSync.plugin.gradle.FigmaDocumentationSyncIncludedBuild
import com.marmatsan.figmaDocumentationSync.plugin.gradle.figmaDocumentationSyncExtension
import com.marmatsan.projectConfig.catalog.ProjectConfigCatalogs
import com.marmatsan.projectConfig.figma.catalog.ProjectConfigFigmaCatalogAdapter
import org.gradle.api.Action
import org.gradle.api.Project

/** Materializes the configured dependency catalog after the consumer DSL is complete. */
internal class ProjectConfigFigmaRegistration(
    private val project: Project
) : Action<Project> {
    /** Registers catalog materialization at the end of project evaluation. */
    fun register() {
        project.afterEvaluate(this)
    }

    /** Serializes the consumer-owned catalog into the Figma task input contract. */
    override fun execute(
        evaluatedProject: Project
    ) {
        if (evaluatedProject != project) {
            return
        }
        val projectConfigCatalogs = project.extensions.getByType(ProjectConfigCatalogs::class.java)
        val figmaExtension = project.extensions.getByType(figmaDocumentationSyncExtension::class.java)
        val conventionPluginIncludedBuilds =
            figmaExtension.includedBuilds
                .filter { includedBuild -> includedBuild.publishesConventionPlugins.get() }
                .map(FigmaDocumentationSyncIncludedBuild::toDomainSource)
        val trees =
            ProjectConfigFigmaCatalogAdapter(
                dependencyCatalog = projectConfigCatalogs.withVersionAliases()
            ).readTrees(
                rootDirectory = project.rootDir,
                conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
            )

        figmaExtension.dependencyCatalogTreesJson.set(
            DependencyCatalogTreesJson.encode(trees)
        )
    }
}

private fun FigmaDocumentationSyncIncludedBuild.toDomainSource(): IncludedBuildSource =
    IncludedBuildSource(
        settingsFilePath = settingsFile.get().asFile.absolutePath,
        rootDirPath = rootDirectory.get().asFile.absolutePath,
        modulePathPrefix = modulePathPrefix.get(),
        publishesCatalogs = publishesCatalogs.get(),
        publishesConventionPlugins = publishesConventionPlugins.get()
    )
