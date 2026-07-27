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
 * Emits a non-blocking warning when external CI topology validation is stale.
 */
@DisableCachingByDefault(
    because = "The warning depends on the current UTC date",
)
abstract class CheckCiExternalTopologyFreshnessTask : DefaultTask() {
    /** Optional versioned topology contract whose validation date is checked. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciExternalTopologyFile: RegularFileProperty

    /** Emits a warning only after the topology contract's declared freshness interval. */
    @TaskAction
    fun checkFreshness() {
        val topologyFile = ciExternalTopologyFile.orNull?.asFile ?: return
        val result =
            FigmaDocumentationSyncComponent::class
                .create()
                .ciExternalTopologyFreshnessChecker
                .check(
                    topologyFile = topologyFile,
                    currentDate = LocalDate.now(ZoneOffset.UTC),
                )

        if (result.warningRequired) {
            logger.warn(
                "External CI topology was last validated on ${result.lastValidatedOn}. " +
                    "Validate docs/ci/external-topology.yaml against the active services " +
                    "and update validation.lastValidatedOn.",
            )
        } else {
            logger.lifecycle("External CI topology validation is current until ${result.warningDate}.")
        }
    }
}
