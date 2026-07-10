package com.marmatsan.figmaDesignSync.plugin.gradle

import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import com.marmatsan.figmaDesignSync.plugin.task.catalog.CheckFigmaCatalogUsageTask
import com.marmatsan.figmaDesignSync.plugin.task.generate.GenerateFigmaDesignModelTask
import com.marmatsan.figmaDesignSync.plugin.task.sync.CheckFigmaTrunkSyncTask
import com.marmatsan.figmaDesignSync.plugin.task.versions.CheckFigmaVersionNamingTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * Registers Gradle tasks that generate and verify the Figma design model.
 *
 * Apply plugin id `com.marmatsan.figmaDesignSync` on the repository root. The
 * plugin exposes the `figmaDesignSync` extension and creates:
 *
 * - `generateFigmaDesignModel`
 * - `checkFigmaCatalogUsage`
 * - `checkFigmaVersionNaming`
 * - `checkFigmaTrunkSync`
 */
@Suppress("unused")
class FigmaDesignSyncGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("base")

        val extension = project.extensions.create<figmaDesignSyncExtension>("figmaDesignSync")

        extension.designModelMetadataNodeUrl.convention(FIGMA_PAGE_URL)
        val includedBuildSources = extension.includedBuildSources(project)

        val checkFigmaCatalogUsage = project.tasks.register<CheckFigmaCatalogUsageTask>("checkFigmaCatalogUsage") {
            group = "verification"
            description = "Checks that dependency catalog entries rendered in Figma are used by the repository."

            rootSettingsFile.set(extension.rootSettingsFile)
            includedBuildSettingsFiles.from(
                includedBuildSources.map { sources -> sources.map { source -> source.settingsFile } }
            )
            includedBuildModelNames.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modelName } }
            )
            includedBuildModulePathPrefixes.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modulePathPrefix } }
            )
            includedBuildPublishesCatalogs.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesCatalogs } }
            )
            includedBuildPublishesConventionPlugins.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesConventionPlugins } }
            )
            projectRootDirectory.set(project.layout.projectDirectory)
            includedBuildSourcesProvider = includedBuildSources
        }
        val checkFigmaVersionNaming = project.tasks.register<CheckFigmaVersionNamingTask>("checkFigmaVersionNaming") {
            group = "verification"
            description = "Checks that dependency version keys follow the Figma section naming contract."

            versionsFile.set(extension.versionsFile)
        }

        project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
            dependsOn(checkFigmaCatalogUsage)
            dependsOn(checkFigmaVersionNaming)
        }

        project.tasks.register<GenerateFigmaDesignModelTask>("generateFigmaDesignModel") {
            group = "documentation"
            description = "Generates the design model JSON consumed by the Figma MCP sync step."

            versionsFile.set(extension.versionsFile)
            rootSettingsFile.set(extension.rootSettingsFile)
            includedBuildSettingsFiles.from(
                includedBuildSources.map { sources -> sources.map { source -> source.settingsFile } }
            )
            includedBuildModelNames.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modelName } }
            )
            includedBuildModulePathPrefixes.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modulePathPrefix } }
            )
            includedBuildPublishesCatalogs.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesCatalogs } }
            )
            includedBuildPublishesConventionPlugins.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesConventionPlugins } }
            )
            projectRootDirectory.set(project.layout.projectDirectory)
            includedBuildSourcesProvider = includedBuildSources
            outputFile.set(extension.designModelFile)
        }

        project.tasks.register<CheckFigmaTrunkSyncTask>("checkFigmaTrunkSync") {
            group = "verification"
            description = "Checks that Figma sync metadata matches the design model generated from the current checkout."

            metadataNodeUrl.set(extension.designModelMetadataNodeUrl)
            versionsFile.set(extension.versionsFile)
            rootSettingsFile.set(extension.rootSettingsFile)
            includedBuildSettingsFiles.from(
                includedBuildSources.map { sources -> sources.map { source -> source.settingsFile } }
            )
            includedBuildModelNames.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modelName } }
            )
            includedBuildModulePathPrefixes.set(
                includedBuildSources.map { sources -> sources.map { source -> source.modulePathPrefix } }
            )
            includedBuildPublishesCatalogs.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesCatalogs } }
            )
            includedBuildPublishesConventionPlugins.set(
                includedBuildSources.map { sources -> sources.map { source -> source.publishesConventionPlugins } }
            )
            projectRootDirectory.set(project.layout.projectDirectory)
            includedBuildSourcesProvider = includedBuildSources
            figmaToken.set(project.providers.environmentVariable("FIGMA_FILE_CONTENT_ACCESS_TOKEN"))
        }
    }

    private companion object {
        const val FIGMA_PAGE_URL =
            "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=62934-908"
    }
}

private fun figmaDesignSyncExtension.includedBuildSources(project: Project) =
    project.provider {
        includedBuilds
            .toList()
            .sortedBy(FigmaDesignSyncIncludedBuild::getName)
            .map { includedBuild ->
                FigmaDesignModelIncludedBuildSource(
                    modelName = includedBuild.modelName.get(),
                    settingsFile = includedBuild.settingsFile.get().asFile,
                    rootDirectory = includedBuild.rootDirectory.get().asFile,
                    modulePathPrefix = includedBuild.modulePathPrefix.get(),
                    publishesCatalogs = includedBuild.publishesCatalogs.get(),
                    publishesConventionPlugins = includedBuild.publishesConventionPlugins.get()
                )
            }
    }
