package com.marmatsan.figmaDocumentationSync.plugin.task.input

import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSourceFactory
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

/**
 * Gradle input contract shared by tasks that inspect configured included builds.
 *
 * The parallel properties preserve Gradle's lazy, serializable task inputs. Values at the same
 * index describe one included build and are validated when they are converted to model sources.
 */
interface IncludedBuildTaskInputs {
    /** Settings scripts of included builds participating in the task. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    val includedBuildSettingsFiles: ConfigurableFileCollection

    /** Settings-file paths aligned by index with included-build identities. */
    @get:Input
    val includedBuildSettingsFilePaths: ListProperty<String>

    /** Root-directory paths aligned by index with included-build identities. */
    @get:Input
    val includedBuildRootDirectoryPaths: ListProperty<String>

    /** Design-model identities aligned by index with included-build settings. */
    @get:Input
    val includedBuildModelNames: ListProperty<String>

    /** Module path prefixes aligned by index with included-build settings. */
    @get:Input
    val includedBuildModulePathPrefixes: ListProperty<String>

    /** Catalog publication flags aligned by index with included-build settings. */
    @get:Input
    val includedBuildPublishesCatalogs: ListProperty<Boolean>

    /** Convention-plugin publication flags aligned by index with included-build settings. */
    @get:Input
    val includedBuildPublishesConventionPlugins: ListProperty<Boolean>
}

/** Reconstructs validated included-build sources from this task's aligned Gradle inputs. */
internal fun IncludedBuildTaskInputs.resolveIncludedBuildSources(): List<FigmaDesignModelIncludedBuildSource> =
    FigmaDesignModelIncludedBuildSourceFactory().create(
        settingsFilePaths = includedBuildSettingsFilePaths.get(),
        rootDirectoryPaths = includedBuildRootDirectoryPaths.get(),
        modelNames = includedBuildModelNames.get(),
        modulePathPrefixes = includedBuildModulePathPrefixes.get(),
        publishesCatalogs = includedBuildPublishesCatalogs.get(),
        publishesConventionPlugins = includedBuildPublishesConventionPlugins.get()
    )
