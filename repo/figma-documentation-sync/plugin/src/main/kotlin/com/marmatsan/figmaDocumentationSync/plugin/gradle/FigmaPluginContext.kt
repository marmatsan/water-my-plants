package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/** Shared Gradle providers passed from the plugin composition root to focused registrars. */
internal class FigmaPluginContext(
    val project: Project,
    val extension: figmaDocumentationSyncExtension,
) {
    val includedBuildSources: Provider<List<FigmaDesignModelIncludedBuildSource>> =
        project.provider {
            extension.includedBuilds
                .toList()
                .sortedBy(FigmaDocumentationSyncIncludedBuild::getName)
                .map { includedBuild ->
                    FigmaDesignModelIncludedBuildSource(
                        modelName = includedBuild.modelName.get(),
                        settingsFile = includedBuild.settingsFile.get().asFile,
                        rootDirectory = includedBuild.rootDirectory.get().asFile,
                        modulePathPrefix = includedBuild.modulePathPrefix.get(),
                        publishesCatalogs = includedBuild.publishesCatalogs.get(),
                        publishesConventionPlugins = includedBuild.publishesConventionPlugins.get(),
                    )
                }
        }

    fun booleanProperty(
        name: String,
    ): Provider<Boolean> =
        project.providers
            .gradleProperty(name)
            .map(String::toBoolean)
            .orElse(false)
}
