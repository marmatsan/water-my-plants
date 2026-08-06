package com.marmatsan.figmaDocumentationSync.plugin.task.catalog

import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import com.marmatsan.figmaDocumentationSync.plugin.checker.catalog.CatalogUsageCheckRequest
import com.marmatsan.figmaDocumentationSync.plugin.di.FigmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.task.input.DependencyCatalogTaskInputs
import com.marmatsan.figmaDocumentationSync.plugin.task.input.IncludedBuildTaskInputs
import com.marmatsan.figmaDocumentationSync.plugin.task.input.resolveDependencyCatalogTreeSource
import com.marmatsan.figmaDocumentationSync.plugin.task.input.resolveIncludedBuildSources
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
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
    because = "The check inspects repository sources outside its declared settings inputs"
)
abstract class CheckFigmaCatalogUsageTask :
    DefaultTask(),
    IncludedBuildTaskInputs,
    DependencyCatalogTaskInputs {
    /** Design-model name of the main dependency catalog. */
    @get:Input
    abstract val primaryCatalogModelName: Property<String>

    /** Main settings script used to discover project modules. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootSettingsFile: RegularFileProperty

    /** Repository root used to locate catalog consumers. */
    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    /** Fails when a declared dependency catalog entry has no supported consumer. */
    @TaskAction
    fun checkCatalogUsage() {
        val includedBuildSources = resolveIncludedBuildSources()
        val conventionPluginIncludedBuilds =
            includedBuildSources
                .map { source -> source.toDomainSource() }
                .filter(IncludedBuildSource::publishesConventionPlugins)
        val result =
            FigmaDocumentationSyncComponent::class.create().catalogUsageChecker.check(
                CatalogUsageCheckRequest(
                    projectRootDirectory = projectRootDirectory.get().asFile,
                    primaryCatalogModelName = primaryCatalogModelName.get(),
                    primaryCatalogTreeSource =
                        resolveDependencyCatalogTreeSource(
                            projectRootDirectory = projectRootDirectory.get().asFile,
                            conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
                        ),
                    includedBuilds = includedBuildSources
                )
            )

        if (!result.isSuccessful) {
            throw GradleException(
                buildString {
                    appendLine("Unused dependency catalog entries found.")
                    appendLine(
                        "Remove each entry or make it used by a module, convention plugin, or tool configuration:"
                    )
                    result.unusedEntries.forEach { entry ->
                        appendLine("- ${entry.catalogName}: ${entry.entry}")
                    }
                }.trimEnd()
            )
        }

        logger.lifecycle("All dependency catalog entries are used.")
    }
}
