package com.marmatsan.figmaDesignSync.plugin.task.ci

import com.marmatsan.figmaDesignSync.plugin.di.create
import com.marmatsan.figmaDesignSync.plugin.di.figmaDesignSyncComponent
import java.time.LocalDate
import java.time.ZoneOffset
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Emits a non-blocking warning when external CI topology validation is stale.
 */
abstract class CheckCiExternalTopologyFreshnessTask : DefaultTask() {
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val ciExternalTopologyFile: RegularFileProperty

    @TaskAction
    fun checkFreshness() {
        val topologyFile = ciExternalTopologyFile.orNull?.asFile ?: return
        val result = figmaDesignSyncComponent::class.create()
            .ciExternalTopologyFreshnessChecker
            .check(
                topologyFile = topologyFile,
                currentDate = LocalDate.now(ZoneOffset.UTC)
            )

        if (result.warningRequired) {
            logger.warn(
                "External CI topology was last validated on ${result.lastValidatedOn}. " +
                    "Validate docs/ci/external-topology.yaml against the active services " +
                    "and update validation.lastValidatedOn."
            )
        } else {
            logger.lifecycle("External CI topology validation is current until ${result.warningDate}.")
        }
    }
}
