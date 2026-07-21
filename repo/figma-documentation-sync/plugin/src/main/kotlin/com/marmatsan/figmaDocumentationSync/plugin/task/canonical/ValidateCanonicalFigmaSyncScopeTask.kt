package com.marmatsan.figmaDocumentationSync.plugin.task.canonical

import com.marmatsan.figmaDocumentationSync.domain.model.impact.FigmaVerificationScope
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.di.figmaDocumentationSyncComponent
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream

/** Validates that the consumed sync scope belongs to the current checkout. */
@DisableCachingByDefault(
    because = "The current Git revision is runtime state",
)
abstract class ValidateCanonicalFigmaSyncScopeTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val scopeFile: RegularFileProperty

    @get:Internal
    abstract val designModelFile: RegularFileProperty

    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @get:OutputFile
    abstract val verifiedScopeFile: RegularFileProperty

    /** Rejects stale scope/model artifacts and publishes the validated scope for the conditional checker. */
    @TaskAction
    fun validate() {
        val scope =
            figmaDocumentationSyncComponent::class
                .create()
                .canonicalFigmaSyncScopeJson
                .read(scopeFile.get().asFile.absolutePath)
        val currentGitSha =
            git(
                "rev-parse",
                "HEAD",
            )
        if (scope.gitSha != currentGitSha) {
            throw GradleException(
                "Figma Sync scope artifact belongs to '${scope.gitSha}', not '$currentGitSha'.",
            )
        }
        if (scope.scope == FigmaVerificationScope.FULL_VERIFICATION && !designModelFile.get().asFile.isFile) {
            throw GradleException(
                "Missing canonical Figma design model artifact: ${designModelFile.get().asFile.path}",
            )
        }

        val output = verifiedScopeFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(scope.scope.wireValue + System.lineSeparator())
        if (scope.scope == FigmaVerificationScope.FULL_VERIFICATION) {
            logger.lifecycle("Validated full canonical Figma Sync scope for $currentGitSha.")
        } else {
            logger.lifecycle("${scope.scope.wireValue} main change; Figma model and metadata are unchanged.")
        }
    }

    private fun git(
        vararg arguments: String,
    ): String {
        val root = projectRootDirectory.get().asFile
        val safeDirectory =
            root.absolutePath.replace(
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
            ).directory(root)
                .start()
        val output = ByteArrayOutputStream()
        val error = ByteArrayOutputStream()
        process.inputStream.use { input -> input.copyTo(output) }
        process.errorStream.use { input -> input.copyTo(error) }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw GradleException("Failed to run git: ${error.toString().trim()}")
        }
        return output.toString().trim()
    }
}
