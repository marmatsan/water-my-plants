package com.marmatsan.figmaDocumentationSync.plugin.task.sync

import com.marmatsan.figmaDocumentationSync.plugin.checker.sync.FigmaTrunkSyncCheckRequest
import com.marmatsan.figmaDocumentationSync.plugin.di.FigmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.di.create
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
    /** Figma metadata node whose shared plugin data records the published identity. */
    @get:Input
    abstract val metadataNodeUrl: Property<String>

    /** Shared plugin-data namespace containing synchronization metadata. */
    @get:Input
    abstract val metadataNamespace: Property<String>

    /** Design-model name of the main dependency catalog. */
    @get:Input
    abstract val primaryCatalogModelName: Property<String>

    /** Provider class used to expose the main dependency catalog. */
    @get:Input
    abstract val dependencyCatalogProviderClassName: Property<String>

    /** Whether CI documentation contributes to the expected model. */
    @get:Input
    abstract val ciDocumentationEnabled: Property<Boolean>

    /** Optional design-model name for generated CI configuration. */
    @get:Input
    @get:Optional
    abstract val ciConfigurationModelName: Property<String>

    /** Optional provider that loads generated CI configuration. */
    @get:Input
    @get:Optional
    abstract val ciConfigurationProviderClassName: Property<String>

    /** Repository version sections used to reproduce the expected model. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionsFile: RegularFileProperty

    /** Main settings script used to reproduce production modules. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootSettingsFile: RegularFileProperty

    /** Versioned topology contract for external CI services. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciExternalTopologyFile: RegularFileProperty

    /** Versioned contract for the Windows CI runtime. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciWindowsRuntimeFile: RegularFileProperty

    /** Optional generated CI configuration used in model reproduction. */
    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciGeneratedConfigurationDirectory: DirectoryProperty

    /** Settings scripts of included builds participating in the model. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val includedBuildSettingsFiles: ConfigurableFileCollection

    /** Design-model identities aligned by index with included-build settings. */
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

    /** Repository root used to capture the expected Git identity. */
    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @get:Internal
    internal var includedBuildSourcesProvider: Provider<List<FigmaDesignModelIncludedBuildSource>>? = null

    /** Secret runtime token read from the environment and excluded from cache inputs. */
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
            FigmaDocumentationSyncComponent::class.create().trunkSyncChecker.check(
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
