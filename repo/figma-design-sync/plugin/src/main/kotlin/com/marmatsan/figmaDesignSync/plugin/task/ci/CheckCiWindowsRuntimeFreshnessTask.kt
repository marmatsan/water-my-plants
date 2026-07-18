package com.marmatsan.figmaDesignSync.plugin.task.ci

import com.marmatsan.figmaDesignSync.plugin.di.create
import com.marmatsan.figmaDesignSync.plugin.di.figmaDesignSyncComponent
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Emits a non-blocking warning when Windows CI runtime validation is stale.
 */
abstract class CheckCiWindowsRuntimeFreshnessTask : DefaultTask() {
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciWindowsRuntimeFile: RegularFileProperty

    @TaskAction
    fun checkFreshness() {
        val runtimeFile = ciWindowsRuntimeFile.orNull?.asFile ?: return
        val result = figmaDesignSyncComponent::class.create()
            .ciWindowsRuntimeFreshnessChecker
            .check(
                runtimeFile = runtimeFile,
                currentDate = LocalDate.now(ZoneOffset.UTC)
            )

        if (result.warningRequired) {
            logger.warn(
                "Windows CI runtime was last validated on ${result.lastValidatedOn}. " +
                    "Validate docs/ci/windows-runtime.yaml against the installed services " +
                    "and update validation.lastValidatedOn."
            )
        } else {
            logger.lifecycle("Windows CI runtime validation is current until ${result.warningDate}.")
        }
    }
}
