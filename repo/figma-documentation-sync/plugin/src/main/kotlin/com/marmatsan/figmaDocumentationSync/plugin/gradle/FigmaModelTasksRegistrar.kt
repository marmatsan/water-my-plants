package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.figmaDocumentationSync.plugin.task.generate.GenerateFigmaDesignModelTask
import com.marmatsan.figmaDocumentationSync.plugin.task.visual.GenerateCiVisualPlanTask
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import java.io.File

/** Registers tasks that materialize portable Figma and CI visual models. */
internal class FigmaModelTasksRegistrar(
    private val context: FigmaPluginContext,
) {
    fun register(): TaskProvider<GenerateFigmaDesignModelTask> {
        registerCiVisualPlan()
        return registerDesignModel()
    }

    private fun registerCiVisualPlan() {
        val project = context.project
        project.tasks.register<GenerateCiVisualPlanTask>("generateFigmaCiVisualPlan") {
            group = "documentation"
            description = "Generates the Kotlin-owned CI visual plan consumed by the Figma adapter."
            designModelFile.set(
                project.layout
                    .file(
                        project.providers.gradleProperty("figmaCiVisualDesignModel").map(::File),
                    ).orElse(context.extension.designModelFile),
            )
            writerProjectConfigFile.set(
                project.layout.file(
                    project.providers.gradleProperty("figmaWriterProjectConfig").map(::File),
                ),
            )
            target.convention(project.providers.gradleProperty("figmaCiVisualTarget"))
            outputFile.set(
                project.layout
                    .file(
                        project.providers.gradleProperty("figmaCiVisualPlanOutput").map(::File),
                    ).orElse(project.layout.buildDirectory.file("reports/figma-sync/ci-visual-plan.json")),
            )
        }
    }

    private fun registerDesignModel(): TaskProvider<GenerateFigmaDesignModelTask> =
        context.project.tasks.register<GenerateFigmaDesignModelTask>("generateFigmaDesignModel") {
            group = "documentation"
            description = "Generates the design model JSON consumed by the Figma MCP sync step."
            configureDesignModelInputs(context)
            outputFile.set(context.extension.designModelFile)
        }
}
