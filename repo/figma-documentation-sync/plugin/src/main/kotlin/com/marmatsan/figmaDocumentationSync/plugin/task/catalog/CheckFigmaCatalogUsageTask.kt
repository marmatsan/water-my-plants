package com.marmatsan.figmaDocumentationSync.plugin.task.catalog

import com.marmatsan.figmaDocumentationSync.plugin.checker.catalog.CatalogUsageCheckRequest
import com.marmatsan.figmaDocumentationSync.plugin.di.FigmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSourceFactory
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Gradle verification task that fails when dependency catalogs declare entries
 * not used by any module, convention plugin, or tool configuration.
 */
@DisableCachingByDefault(
    because = "The check inspects repository sources outside its declared settings inputs",
)
abstract class CheckFigmaCatalogUsageTask : DefaultTask() {
    /** Design-model name of the main dependency catalog. */
    @get:Input
    abstract val primaryCatalogModelName: Property<String>

    /** Provider class that exposes the main catalog to the scanner. */
    @get:Input
    abstract val dependencyCatalogProviderClassName: Property<String>

    /** Main settings script used to discover project modules. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootSettingsFile: RegularFileProperty

    /** Settings scripts of included builds that may publish catalogs or conventions. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val includedBuildSettingsFiles: ConfigurableFileCollection

    /** Settings-file paths aligned by index with included-build identities. */
    @get:Input
    abstract val includedBuildSettingsFilePaths: ListProperty<String>

    /** Root-directory paths aligned by index with included-build identities. */
    @get:Input
    abstract val includedBuildRootDirectoryPaths: ListProperty<String>

    /** Stable design-model names aligned by index with included-build settings. */
    @get:Input
    abstract val includedBuildModelNames: ListProperty<String>

    /** Module path prefixes aligned by index with included-build settings. */
    @get:Input
    abstract val includedBuildModulePathPrefixes: ListProperty<String>

    /** Catalog publication flags aligned by index with included-build settings. */
    @get:Input
    abstract val includedBuildPublishesCatalogs: ListProperty<Boolean>

    /** Convention-plugin publication flags aligned by index with included-build settings. */
    @get:Input
    abstract val includedBuildPublishesConventionPlugins: ListProperty<Boolean>

    /** Repository root used to locate catalog consumers. */
    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    /** Fails when a declared dependency catalog entry has no supported consumer. */
    @TaskAction
    fun checkCatalogUsage() {
        val result =
            FigmaDocumentationSyncComponent::class.create().catalogUsageChecker.check(
                CatalogUsageCheckRequest(
                    projectRootDirectory = projectRootDirectory.get().asFile,
                    primaryCatalogModelName = primaryCatalogModelName.get(),
                    dependencyCatalogProviderClassName = dependencyCatalogProviderClassName.get(),
                    includedBuilds = includedBuildSources(),
                ),
            )

        if (!result.isSuccessful) {
            throw GradleException(
                buildString {
                    appendLine("Unused dependency catalog entries found.")
                    appendLine(
                        "Remove each entry or make it used by a module, convention plugin, or tool configuration:",
                    )
                    result.unusedEntries.forEach { entry ->
                        appendLine("- ${entry.catalogName}: ${entry.entry}")
                    }
                }.trimEnd(),
            )
        }

        logger.lifecycle("All dependency catalog entries are used.")
    }

    private fun includedBuildSources(): List<FigmaDesignModelIncludedBuildSource> =
        FigmaDesignModelIncludedBuildSourceFactory().create(
            settingsFilePaths = includedBuildSettingsFilePaths.get(),
            rootDirectoryPaths = includedBuildRootDirectoryPaths.get(),
            modelNames = includedBuildModelNames.get(),
            modulePathPrefixes = includedBuildModulePathPrefixes.get(),
            publishesCatalogs = includedBuildPublishesCatalogs.get(),
            publishesConventionPlugins = includedBuildPublishesConventionPlugins.get(),
        )
}
