package com.marmatsan.figmaDocumentationSync.plugin.task.generate

import com.marmatsan.figmaDocumentationSync.plugin.di.FigmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.generator.FigmaDesignModelGenerationRequest
import com.marmatsan.figmaDocumentationSync.plugin.task.input.IncludedBuildTaskInputs
import com.marmatsan.figmaDocumentationSync.plugin.task.input.resolveIncludedBuildSources
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
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
    because = "Generation records Git, environment, and current-time runtime state"
)
abstract class GenerateFigmaDesignModelTask :
    DefaultTask(),
    IncludedBuildTaskInputs {
    /** Design-model name of the repository's primary dependency catalog. */
    @get:Input
    abstract val primaryCatalogModelName: Property<String>

    /** Provider class used to expose the main dependency catalog. */
    @get:Input
    abstract val dependencyCatalogProviderClassName: Property<String>

    /** Whether the optional CI documentation adapter contributes model content. */
    @get:Input
    abstract val ciDocumentationEnabled: Property<Boolean>

    /** Optional model name for the generated CI configuration. */
    @get:Input
    @get:Optional
    abstract val ciConfigurationModelName: Property<String>

    /** Optional provider that loads generated CI configuration. */
    @get:Input
    @get:Optional
    abstract val ciConfigurationProviderClassName: Property<String>

    /** Optional CI alias normalized to the canonical `main` branch. */
    @get:Input
    @get:Optional
    abstract val ciDefaultBranchAlias: Property<String>

    /** Repository version sections rendered into the model. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionsFile: RegularFileProperty

    /** Main settings script used to discover production modules. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootSettingsFile: RegularFileProperty

    /** Optional versioned topology contract for external CI services. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciExternalTopologyFile: RegularFileProperty

    /** Optional versioned contract for the Windows CI runtime. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciWindowsRuntimeFile: RegularFileProperty

    /** Optional generated CI configuration materialized by the project adapter. */
    @get:InputDirectory
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciGeneratedConfigurationDirectory: DirectoryProperty

    /** Repository root used for module resolution and Git identity checks. */
    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    /** Canonical `design-model.json` artifact consumed by Figma synchronization. */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * Generates the model and writes pretty-printed JSON to [outputFile].
     */
    @TaskAction
    fun generate() {
        val branch = canonicalBranch()
        requireMainBranch(
            branch = branch
        )
        requireCompatibleGitCheckout(
            branch = branch
        )

        val result =
            FigmaDocumentationSyncComponent::class.create().designModelGenerator.generate(
                request =
                    FigmaDesignModelGenerationRequest(
                        branch = branch,
                        gitSha =
                            git(
                                "rev-parse",
                                "HEAD"
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
                        includedBuilds = resolveIncludedBuildSources()
                    )
            )

        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(
            prettyJson.encodeToString(
                JsonElement.serializer(),
                result.model
            ) + System.lineSeparator()
        )

        logger.lifecycle("Generated Figma design model at ${file.path} (${result.modelHash}).")
    }

    private fun git(
        vararg arguments: String
    ): String {
        val rootDirectory = projectRootDirectory.get().asFile
        val safeDirectory =
            rootDirectory.absolutePath.replace(
                '\\',
                '/'
            )
        val process =
            ProcessBuilder(
                listOf(
                    "git",
                    "-c",
                    "safe.directory=$safeDirectory"
                ) + arguments
            ).directory(rootDirectory)
                .start()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        process.inputStream.use { input -> input.copyTo(output) }
        process.errorStream.use { input -> input.copyTo(error) }
        val exitValue = process.waitFor()

        if (exitValue != 0) {
            throw GradleException(
                "Failed to run git ${arguments.joinToString(" ")}: ${error.toString().trim()}"
            )
        }

        return output.toString().trim()
    }

    private fun canonicalBranch(): String {
        val canonicalGeneration = System.getenv(CANONICAL_GENERATION_ENVIRONMENT_VARIABLE)
        if (canonicalGeneration != "true") {
            throw GradleException(
                "generateFigmaDesignModel may only create the canonical design-model.json from " +
                    "the configured CI Figma Sync adapter. Missing " +
                    "$CANONICAL_GENERATION_ENVIRONMENT_VARIABLE=true."
            )
        }

        val rawBranch = System.getenv(BRANCH_ENVIRONMENT_VARIABLE)?.trim().orEmpty()
        if (rawBranch.isBlank()) {
            throw GradleException(
                "generateFigmaDesignModel may only create the canonical design-model.json when " +
                    "$BRANCH_ENVIRONMENT_VARIABLE identifies the CI checkout branch."
            )
        }

        return normalizeBranch(
            branch = rawBranch
        )
    }

    private fun requireMainBranch(
        branch: String
    ) {
        if (branch != MAIN_BRANCH) {
            throw GradleException(
                "generateFigmaDesignModel may only create the canonical design-model.json from " +
                    "'$MAIN_BRANCH'. Current branch is '$branch'. Use the configured CI Figma Sync " +
                    "adapter on '$MAIN_BRANCH' to produce the artifact consumed by the Figma sync."
            )
        }
    }

    private fun requireCompatibleGitCheckout(
        branch: String
    ) {
        val gitBranch =
            normalizeBranch(
                branch =
                    git(
                        "rev-parse",
                        "--abbrev-ref",
                        "HEAD"
                    )
            )
        if (gitBranch != DETACHED_HEAD && gitBranch != branch) {
            throw GradleException(
                "CI declared Figma design model branch '$branch', but the Git checkout is " +
                    "'$gitBranch'. Fix the CI checkout before generating design-model.json."
            )
        }
    }

    private fun normalizeBranch(
        branch: String
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
