package com.marmatsan.figmaDocumentationSync.plugin.task.ci

import com.marmatsan.figmaDocumentationSync.plugin.di.FigmaDocumentationSyncComponent
import com.marmatsan.figmaDocumentationSync.plugin.di.create
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Emits a non-blocking warning when Windows CI runtime validation is stale.
 */
@DisableCachingByDefault(
    because = "The warning depends on the current UTC date"
)
abstract class CheckCiWindowsRuntimeFreshnessTask : DefaultTask() {
    /** Optional versioned Windows runtime contract whose validation date is checked. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciWindowsRuntimeFile: RegularFileProperty

    /** Emits a warning only after the runtime contract's declared freshness interval. */
    @TaskAction
    fun checkFreshness() {
        val runtimeFile = ciWindowsRuntimeFile.orNull?.asFile ?: return
        val result =
            FigmaDocumentationSyncComponent::class
                .create()
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
