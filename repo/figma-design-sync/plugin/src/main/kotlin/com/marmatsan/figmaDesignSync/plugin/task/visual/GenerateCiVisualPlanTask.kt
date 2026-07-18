package com.marmatsan.figmaDesignSync.plugin.task.visual

import com.marmatsan.figmaDesignSync.data.json.visual.CiVisualPlanJson
import com.marmatsan.figmaDesignSync.data.json.writer.FigmaWriterRuntimeConfigJson
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Generates the deterministic CI visual plan consumed by the TypeScript Figma adapter. */
@CacheableTask
abstract class GenerateCiVisualPlanTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val designModelFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val writerProjectConfigFile: RegularFileProperty

    @get:Input
    @get:Optional
    abstract val target: Property<String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val runtimeConfig = FigmaWriterRuntimeConfigJson.read(
            writerProjectConfigFile.get().asFile.absolutePath
        )
        val visualConfig = runtimeConfig.ciVisualPlanConfig
            ?: throw GradleException("The writer project config does not declare CI visual targets.")
        val destination = outputFile.get().asFile
        CiVisualPlanJson.write(
            designModelPath = designModelFile.get().asFile.absolutePath,
            config = visualConfig,
            target = target.orNull,
            outputPath = destination.absolutePath
        )
        logger.lifecycle("Wrote Kotlin CI visual plan to ${destination.path}")
    }
}
