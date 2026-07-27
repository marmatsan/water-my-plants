package com.marmatsan.figmaDocumentationSync.plugin.task.config

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/** Writes the repository-owned writer configuration as a transient JSON build input. */
@CacheableTask
abstract class WriteFigmaWriterProjectConfigTask : DefaultTask() {
    /** Canonical project configuration JSON produced by the repository adapter. */
    @get:Input
    abstract val configurationJson: Property<String>

    /** Transient JSON file consumed by the Figma writer toolchain. */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /** Materializes [configurationJson] without interpreting repository-specific fields. */
    @TaskAction
    fun write() {
        val destination = outputFile.get().asFile
        destination.parentFile.mkdirs()
        destination.writeText(configurationJson.get())
        logger.lifecycle("Wrote Figma writer project config to ${destination.path}")
    }
}
