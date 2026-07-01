package com.marmatsan.figmaDesignSync.plugin.task.generate

import com.marmatsan.figmaDesignSync.plugin.di.figmaDesignSyncComponent
import com.marmatsan.figmaDesignSync.plugin.di.create
import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerationRequest
import java.io.ByteArrayOutputStream
import java.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
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

/**
 * Gradle task that writes the local `design-model.json` artifact.
 *
 * The artifact is the source consumed by the Figma MCP sync step. Git branch
 * and SHA are captured at execution time so the generated metadata identifies
 * the exact repository snapshot.
 */
abstract class GenerateFigmaDesignModelTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootSettingsFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val buildLogicSettingsFile: RegularFileProperty

    @get:Internal
    abstract val projectRootDirectory: DirectoryProperty

    @get:Internal
    abstract val buildLogicRootDirectory: DirectoryProperty

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /**
     * Generates the model and writes pretty-printed JSON to [outputFile].
     */
    @TaskAction
    fun generate() {
        val result = figmaDesignSyncComponent::class.create().designModelGenerator.generate(
            FigmaDesignModelGenerationRequest(
                branch = git("rev-parse", "--abbrev-ref", "HEAD"),
                gitSha = git("rev-parse", "HEAD"),
                generatedAt = Instant.now(),
                versionsFile = versionsFile.get().asFile,
                rootSettingsFile = rootSettingsFile.get().asFile,
                buildLogicSettingsFile = buildLogicSettingsFile.get().asFile,
                projectRootDirectory = projectRootDirectory.get().asFile,
                buildLogicRootDirectory = buildLogicRootDirectory.get().asFile
            )
        )

        val file = outputFile.get().asFile
        file.parentFile.mkdirs()
        file.writeText(prettyJson.encodeToString(JsonElement.serializer(), result.model) + System.lineSeparator())

        logger.lifecycle("Generated Figma design model at ${file.path} (${result.modelHash}).")
    }

    private fun git(vararg arguments: String): String {
        val process = ProcessBuilder(listOf("git") + arguments)
            .directory(projectRootDirectory.get().asFile)
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

    private companion object {
        val prettyJson = Json {
            prettyPrint = true
            explicitNulls = true
        }
    }
}
