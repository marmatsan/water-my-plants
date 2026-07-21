package com.marmatsan.figmaDocumentationSync.plugin.task.generate

import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.di.figmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelGenerationRequest
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
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
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream
import java.time.Instant

/**
 * Gradle task that writes the canonical `main` branch `design-model.json` artifact.
 *
 * The artifact is the source consumed by the Figma MCP sync step. Git branch
 * and SHA are captured at execution time so the generated metadata identifies
 * the exact repository snapshot. The artifact is intentionally restricted to
 * the configured CI Figma Sync adapter on `main` so local or short-lived
 * branch models cannot be mistaken for the canonical Figma publication input.
 */
@DisableCachingByDefault(
    because = "Generation records Git, environment, and current-time runtime state",
)
abstract class GenerateFigmaDesignModelTask : DefaultTask() {
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

    @get:Input
    @get:Optional
    abstract val ciDefaultBranchAlias: Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootSettingsFile: RegularFileProperty

    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciExternalTopologyFile: RegularFileProperty

    @get:InputFile
    @get:Optional
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

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * Generates the model and writes pretty-printed JSON to [outputFile].
     */
    @TaskAction
    fun generate() {
        val branch = canonicalBranch()
        requireMainBranch(
            branch = branch,
        )
        requireCompatibleGitCheckout(
            branch = branch,
        )

        val result =
            figmaDocumentationSyncComponent::class.create().designModelGenerator.generate(
                request =
                    FigmaDesignModelGenerationRequest(
                        branch = branch,
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

        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            prettyJson.encodeToString(
                JsonElement.serializer(),
                result.model,
            ) + System.lineSeparator(),
        )

        logger.lifecycle("Generated Figma design model at ${file.path} (${result.modelHash}).")
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

    private fun canonicalBranch(): String {
        val canonicalGeneration = System.getenv(CANONICAL_GENERATION_ENVIRONMENT_VARIABLE)
        if (canonicalGeneration != "true") {
            throw GradleException(
                "generateFigmaDesignModel may only create the canonical design-model.json from " +
                    "the configured CI Figma Sync adapter. Missing " +
                    "$CANONICAL_GENERATION_ENVIRONMENT_VARIABLE=true.",
            )
        }

        val rawBranch = System.getenv(BRANCH_ENVIRONMENT_VARIABLE)?.trim().orEmpty()
        if (rawBranch.isBlank()) {
            throw GradleException(
                "generateFigmaDesignModel may only create the canonical design-model.json when " +
                    "$BRANCH_ENVIRONMENT_VARIABLE identifies the CI checkout branch.",
            )
        }

        return normalizeBranch(
            branch = rawBranch,
        )
    }

    private fun requireMainBranch(
        branch: String,
    ) {
        if (branch != MAIN_BRANCH) {
            throw GradleException(
                "generateFigmaDesignModel may only create the canonical design-model.json from " +
                    "'$MAIN_BRANCH'. Current branch is '$branch'. Use the configured CI Figma Sync " +
                    "adapter on '$MAIN_BRANCH' to produce the artifact consumed by the Figma sync.",
            )
        }
    }

    private fun requireCompatibleGitCheckout(
        branch: String,
    ) {
        val gitBranch =
            normalizeBranch(
                branch =
                    git(
                        "rev-parse",
                        "--abbrev-ref",
                        "HEAD",
                    ),
            )
        if (gitBranch != DETACHED_HEAD && gitBranch != branch) {
            throw GradleException(
                "CI declared Figma design model branch '$branch', but the Git checkout is " +
                    "'$gitBranch'. Fix the CI checkout before generating design-model.json.",
            )
        }
    }

    private fun normalizeBranch(
        branch: String,
    ): String {
        val normalized =
            branch
                .removePrefix("refs/heads/")
                .removePrefix("refs/remotes/")
                .removePrefix("origin/")

        return if (normalized == ciDefaultBranchAlias.orNull) MAIN_BRANCH else normalized
    }

    private companion object {
        const val MAIN_BRANCH = "main"
        const val DETACHED_HEAD = "HEAD"
        const val CANONICAL_GENERATION_ENVIRONMENT_VARIABLE = "FIGMA_DOCUMENTATION_SYNC_CANONICAL"
        const val BRANCH_ENVIRONMENT_VARIABLE = "FIGMA_DOCUMENTATION_SYNC_BRANCH"

        val prettyJson =
            Json {
                prettyPrint = true
                explicitNulls = true
            }
    }
}
