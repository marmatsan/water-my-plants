package com.marmatsan.figmaDesignSync.plugin.task.sync

import com.marmatsan.figmaDesignSync.plugin.checker.sync.FigmaDevelopSyncCheckRequest
import com.marmatsan.figmaDesignSync.plugin.di.figmaDesignSyncComponent
import com.marmatsan.figmaDesignSync.plugin.di.create
import java.io.ByteArrayOutputStream
import java.time.Instant
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

abstract class CheckFigmaDevelopSyncTask : DefaultTask() {
    @get:Input
    abstract val metadataNodeUrl: Property<String>

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

    @get:Internal
    abstract val figmaToken: Property<String>

    @TaskAction
    fun checkSync() {
        val token = figmaToken.orNull
            ?: throw GradleException("Missing FIGMA_FILE_CONTENT_ACCESS_TOKEN environment variable")
        val result = figmaDesignSyncComponent::class.create().developSyncChecker.check(
            FigmaDevelopSyncCheckRequest(
                metadataNodeUrl = metadataNodeUrl.get(),
                token = token,
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

        logger.lifecycle("Figma is synced at ${result.gitSha} (${result.modelHash}).")
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
}
