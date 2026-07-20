package com.marmatsan.figmaDocumentationSync.plugin.task.sync

import com.marmatsan.figmaDocumentationSync.plugin.checker.sync.FigmaTrunkSyncCheckRequest
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.di.figmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream
import java.time.Instant

/**
 * Gradle verification task that fails when Figma does not contain the current
 * repository design model hash.
 *
 * The task reads `FIGMA_FILE_CONTENT_ACCESS_TOKEN` at execution time and does
 * not model it as a cacheable input because the token is secret runtime state.
 */
@DisableCachingByDefault(
    because = "The check reads Figma, Git, a secret token, and current-time runtime state",
)
abstract class CheckFigmaTrunkSyncTask : DefaultTask() {
    @get:Input
    abstract val metadataNodeUrl: Property<String>

    @get:Input
    abstract val metadataNamespace: Property<String>

    @get:Input
    abstract val primaryCatalogModelName: Property<String>

    @get:Input
    abstract val dependencyCatalogProviderClassName: Property<String>

    @get:Input
    abstract val ciDocumentationEnabled: Property<Boolean>

    @get:Input
    @get:Optional
    abstract val ciConfigurationModelName: Property<String>

    @get:Input
    @get:Optional
    abstract val ciConfigurationProviderClassName: Property<String>

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionsFile: RegularFileProperty

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootSettingsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciExternalTopologyFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciWindowsRuntimeFile: RegularFileProperty

    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciGeneratedConfigurationDirectory: DirectoryProperty

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

    @get:Internal
    abstract val figmaToken: Property<String>

    /**
     * Generates the expected model and compares it with the metadata stored in
     * Figma shared plugin data.
     */
    @TaskAction
    fun checkSync() {
        val token =
            figmaToken.orNull
                ?: throw GradleException("Missing FIGMA_FILE_CONTENT_ACCESS_TOKEN environment variable")
        val result =
            figmaDocumentationSyncComponent::class.create().trunkSyncChecker.check(
                FigmaTrunkSyncCheckRequest(
                    metadataNodeUrl = metadataNodeUrl.get(),
                    token = token,
                    metadataNamespace = metadataNamespace.get(),
                    branch =
                        git(
                            "rev-parse",
                            "--abbrev-ref",
                            "HEAD",
                        ),
                    gitSha =
                        git(
                            "rev-parse",
                            "HEAD",
                        ),
                    generatedAt = Instant.now(),
                    primaryCatalogModelName = primaryCatalogModelName.get(),
                    dependencyCatalogProviderClassName = dependencyCatalogProviderClassName.get(),
                    ciDocumentationEnabled = ciDocumentationEnabled.get(),
                    ciConfigurationModelName = ciConfigurationModelName.orNull,
                    ciConfigurationProviderClassName = ciConfigurationProviderClassName.orNull,
                    versionsFile = versionsFile.get().asFile,
                    rootSettingsFile = rootSettingsFile.get().asFile,
                    ciExternalTopologyFile = ciExternalTopologyFile.orNull?.asFile,
                    ciWindowsRuntimeFile = ciWindowsRuntimeFile.orNull?.asFile,
                    ciGeneratedConfigurationDirectory = ciGeneratedConfigurationDirectory.orNull?.asFile,
                    projectRootDirectory = projectRootDirectory.get().asFile,
                    includedBuilds = includedBuildSources(),
                ),
            )

        logger.lifecycle("Figma is synced at ${result.gitSha} (${result.modelHash}).")
    }

    private fun git(
        vararg arguments: String,
    ): String {
        val rootDirectory = projectRootDirectory.get().asFile
        val safeDirectory =
            rootDirectory.absolutePath.replace(
                '\\',
                '/',
            )
        val process =
            ProcessBuilder(
                listOf(
                    "git",
                    "-c",
                    "safe.directory=$safeDirectory",
                ) + arguments,
            ).directory(rootDirectory)
                .start()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        process.inputStream.use { input -> input.copyTo(output) }
        process.errorStream.use { input -> input.copyTo(error) }
        val exitValue = process.waitFor()

        if (exitValue != 0) {
            throw GradleException(
                "Failed to run git ${arguments.joinToString(" ")}: ${error.toString().trim()}",
            )
        }

        return output.toString().trim()
    }

    private fun includedBuildSources(): List<FigmaDesignModelIncludedBuildSource> =
        includedBuildSourcesProvider?.get().orEmpty()
}
