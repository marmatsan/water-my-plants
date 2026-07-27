package com.marmatsan.figmaDocumentationSync.plugin.task.visual

import com.marmatsan.figmaDocumentationSync.data.json.visual.CiVisualPlanJson
import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterRuntimeConfigJson
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
    /** Canonical design model containing CI configuration and topology. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val designModelFile: RegularFileProperty

    /** Runtime project contract selecting CI visual layout and model names. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val writerProjectConfigFile: RegularFileProperty

    /** Optional single CI visual target to select from the complete plan. */
    @get:Input
    @get:Optional
    abstract val target: Property<String>

    /** Serialized visual plan consumed by the Figma writer adapter. */
    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    /** Materializes the deterministic CI visual plan selected by the project contract. */
    @TaskAction
    fun generate() {
        val runtimeConfig =
            FigmaWriterRuntimeConfigJson.read(
                writerProjectConfigFile.get().asFile.absolutePath,
            )
        val visualConfig =
            runtimeConfig.ciVisualPlanConfig
                ?: throw GradleException("The writer project config does not declare CI visual targets.")
        val destination = outputFile.get().asFile
        CiVisualPlanJson.write(
            designModelPath = designModelFile.get().asFile.absolutePath,
            config = visualConfig,
            target = target.orNull,
            outputPath = destination.absolutePath,
        )
        logger.lifecycle("Wrote Kotlin CI visual plan to ${destination.path}")
    }
}
