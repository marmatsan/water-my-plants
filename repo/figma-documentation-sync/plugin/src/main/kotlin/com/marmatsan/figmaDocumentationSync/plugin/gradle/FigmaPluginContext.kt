package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import org.gradle.api.Project
import org.gradle.api.provider.Provider

/** Shared Gradle providers passed from the plugin composition root to focused registrars. */
internal class FigmaPluginContext(
    /** Project that owns the applied documentation-sync plugin. */
    val project: Project,
    /** Consumer-configured extension whose providers feed all task registrars. */
    val extension: figmaDocumentationSyncExtension,
) {
    /** Lazily normalized included-build sources shared by generation and verification tasks. */
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

    /** Reads Gradle property [name] as a lazy Boolean provider defaulting to `false`. */
    fun booleanProperty(
        name: String,
    ): Provider<Boolean> =
        project.providers
            .gradleProperty(name)
            .map(String::toBoolean)
            .orElse(false)
}
