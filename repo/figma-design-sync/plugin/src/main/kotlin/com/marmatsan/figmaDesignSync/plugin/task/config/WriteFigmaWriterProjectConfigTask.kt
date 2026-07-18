package com.marmatsan.figmaDesignSync.plugin.task.config

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
    @get:Input
    abstract val configurationJson: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun write() {
        val destination = outputFile.get().asFile
        destination.parentFile.mkdirs()
        destination.writeText(configurationJson.get())
        logger.lifecycle("Wrote Figma writer project config to ${destination.path}")
    }
}
