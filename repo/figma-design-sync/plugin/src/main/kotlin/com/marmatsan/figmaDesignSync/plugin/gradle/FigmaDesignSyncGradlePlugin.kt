package com.marmatsan.figmaDesignSync.plugin.gradle

import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import com.marmatsan.figmaDesignSync.plugin.task.catalog.CheckFigmaCatalogUsageTask
import com.marmatsan.figmaDesignSync.plugin.task.ci.CheckCiExternalTopologyFreshnessTask
import com.marmatsan.figmaDesignSync.plugin.task.ci.CheckCiWindowsRuntimeFreshnessTask
import com.marmatsan.figmaDesignSync.plugin.task.generate.GenerateFigmaDesignModelTask
import com.marmatsan.figmaDesignSync.plugin.task.impact.ClassifyFigmaChangeImpactTask
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

        project.tasks.register<ClassifyFigmaChangeImpactTask>("classifyFigmaChangeImpact") {
            group = "verification"
            description = "Classifies the current repository change for Figma verification and sync."

            policyFile.set(extension.changeImpactPolicyFile)
            projectRootDirectory.set(project.layout.projectDirectory)
            outputFile.set(extension.changeImpactFile)
            changedPathsOverride.convention(
                project.providers.gradleProperty("figmaChangedPaths")
                    .map { value -> value.split(',').map(String::trim).filter(String::isNotEmpty) }
                    .orElse(emptyList())
            )
            project.providers.gradleProperty("figmaComparisonBase").orNull?.let(comparisonBaseOverride::set)
            outputs.upToDateWhen { false }
        }

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
        val checkCiExternalTopologyFreshness =
            project.tasks.register<CheckCiExternalTopologyFreshnessTask>("checkCiExternalTopologyFreshness") {
                group = "verification"
                description = "Warns when the external CI topology has not been validated recently."

                ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
            }
        val checkCiWindowsRuntimeFreshness =
            project.tasks.register<CheckCiWindowsRuntimeFreshnessTask>("checkCiWindowsRuntimeFreshness") {
                group = "verification"
                description = "Warns when the Windows CI runtime has not been validated recently."

                ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
            }

        project.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
            dependsOn(checkFigmaCatalogUsage)
            dependsOn(checkFigmaVersionNaming)
            dependsOn(checkCiExternalTopologyFreshness)
            dependsOn(checkCiWindowsRuntimeFreshness)
        }

        project.tasks.register<GenerateFigmaDesignModelTask>("generateFigmaDesignModel") {
            group = "documentation"
            description = "Generates the design model JSON consumed by the Figma MCP sync step."

            versionsFile.set(extension.versionsFile)
            rootSettingsFile.set(extension.rootSettingsFile)
            ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
            ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
            teamCityGeneratedConfigurationDirectory.set(extension.teamCityGeneratedConfigurationDirectory)
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
            ciExternalTopologyFile.set(extension.ciExternalTopologyFile)
            ciWindowsRuntimeFile.set(extension.ciWindowsRuntimeFile)
            teamCityGeneratedConfigurationDirectory.set(extension.teamCityGeneratedConfigurationDirectory)
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
