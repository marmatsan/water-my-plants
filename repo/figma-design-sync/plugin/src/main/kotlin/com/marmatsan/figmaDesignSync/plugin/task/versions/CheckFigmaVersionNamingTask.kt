package com.marmatsan.figmaDesignSync.plugin.task.versions

import com.marmatsan.figmaDesignSync.plugin.checker.versions.VersionNamingCheckRequest
import com.marmatsan.figmaDesignSync.plugin.di.create
import com.marmatsan.figmaDesignSync.plugin.di.figmaDesignSyncComponent
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Gradle verification task that fails when version keys do not match the
 * Figma dependency version naming contract.
 */
abstract class CheckFigmaVersionNamingTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionsFile: RegularFileProperty

    @TaskAction
    fun checkVersionNaming() {
        val result = figmaDesignSyncComponent::class.create().versionNamingChecker.check(
            VersionNamingCheckRequest(
                versionsFile = versionsFile.get().asFile
            )
        )

        if (!result.isSuccessful) {
            throw GradleException(
                buildString {
                    appendLine("Invalid dependency version naming found.")
                    appendLine(
                        "Keep main project versions in the main section, library versions ending " +
                            "in LibraryVersion, and plugin versions ending in PluginVersion:"
                    )
                    result.violations.forEach { violation ->
                        appendLine("- ${violation.message}")
                    }
                }.trimEnd()
            )
        }

        logger.lifecycle("Dependency version naming follows the Figma contract.")
    }
}
