package com.marmatsan.figmaDocumentationSync.plugin.task.versions

import com.marmatsan.figmaDocumentationSync.plugin.checker.versions.VersionNamingCheckRequest
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import com.marmatsan.figmaDocumentationSync.plugin.di.figmaDocumentationSyncComponent
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Gradle verification task that fails when version keys do not match the
 * Figma dependency version naming contract.
 */
@DisableCachingByDefault(
    because = "The verification task has no reusable output artifact"
)
abstract class CheckFigmaVersionNamingTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionsFile: RegularFileProperty

    @TaskAction
    fun checkVersionNaming() {
        val result = figmaDocumentationSyncComponent::class.create().versionNamingChecker.check(
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
