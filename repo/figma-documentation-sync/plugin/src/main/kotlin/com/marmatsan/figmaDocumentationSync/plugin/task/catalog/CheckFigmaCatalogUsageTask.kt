package com.marmatsan.figmaDocumentationSync.plugin.task.catalog

import com.marmatsan.figmaDocumentationSync.plugin.checker.catalog.CatalogUsageCheckRequest
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.di.figmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
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
@DisableCachingByDefault(because = "The check inspects repository sources outside its declared settings inputs")
abstract class CheckFigmaCatalogUsageTask : DefaultTask() {
    @get:Input
    abstract val primaryCatalogModelName: Property<String>

    @get:Input
    abstract val dependencyCatalogProviderClassName: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootSettingsFile: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val includedBuildSettingsFiles: ConfigurableFileCollection

    @get:Input
    abstract val includedBuildModelNames: ListProperty<String>

    @get:Input
    abstract val includedBuildModulePathPrefixes: ListProperty<String>

    @get:Input
    abstract val includedBuildPublishesCatalogs: ListProperty<Boolean>

    @get:Input
    abstract val includedBuildPublishesConventionPlugins: ListProperty<Boolean>

    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @get:Internal
    internal var includedBuildSourcesProvider: Provider<List<FigmaDesignModelIncludedBuildSource>>? = null

    @TaskAction
    fun checkCatalogUsage() {
        val result = figmaDocumentationSyncComponent::class.create().catalogUsageChecker.check(
            CatalogUsageCheckRequest(
                projectRootDirectory = projectRootDirectory.get().asFile,
                primaryCatalogModelName = primaryCatalogModelName.get(),
                dependencyCatalogProviderClassName = dependencyCatalogProviderClassName.get(),
                includedBuilds = includedBuildSources()
            )
        )

        if (!result.isSuccessful) {
            throw GradleException(
                buildString {
                    appendLine("Unused dependency catalog entries found.")
                    appendLine("Remove each entry or make it used by a module, convention plugin, or tool configuration:")
                    result.unusedEntries.forEach { entry ->
                        appendLine("- ${entry.catalogName}: ${entry.entry}")
                    }
                }.trimEnd()
            )
        }

        logger.lifecycle("All dependency catalog entries are used.")
    }

    private fun includedBuildSources(): List<FigmaDesignModelIncludedBuildSource> =
        includedBuildSourcesProvider?.get().orEmpty()
}
