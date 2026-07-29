package com.marmatsan.waterMyPlants.projectConfig.gradle

import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterProjectConfigJson
import com.marmatsan.figmaDocumentationSync.domain.model.writer.FigmaWriterProjectConfig
import com.marmatsan.figmaDocumentationSync.plugin.task.canonical.PrepareCanonicalFigmaSyncTask
import com.marmatsan.figmaDocumentationSync.plugin.task.config.WriteFigmaWriterProjectConfigTask
import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.ProbeFigmaMcpTask
import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.RunFigmaMcpTask
import com.marmatsan.figmaDocumentationSync.plugin.task.visual.GenerateCiVisualPlanTask
import com.marmatsan.waterMyPlants.projectConfig.platform.HostOperatingSystem
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/** Registers and wires the product-owned tasks that materialize and test the Figma writer. */
internal class WaterMyPlantsFigmaWriterTasksRegistrar(
    private val project: Project,
    private val writerConfig: FigmaWriterProjectConfig
) {
    /** Registers writer configuration, build, test, canonical, visual, and MCP task bindings. */
    fun register() {
        val writeWriterProjectConfig =
            project.tasks.register<WriteFigmaWriterProjectConfigTask>("writeFigmaWriterProjectConfig") {
                group = "figma design sync"
                description = "Writes the Water My Plants Figma writer configuration as transient JSON."
                configurationJson.set(FigmaWriterProjectConfigJson.encode(writerConfig))
                outputFile.set(
                    project.layout.buildDirectory.file(
                        "generated/figma-documentation-sync/writer-project-config.json"
                    )
                )
            }

        val generatedWriterProjectConfigFile = writeWriterProjectConfig.flatMap { task -> task.outputFile }
        val toolsDirectory = project.layout.projectDirectory.dir("repo/figma-documentation-sync/tools")

        project.tasks.register<Exec>("buildFigmaDocumentationSyncTools") {
            group = "figma design sync"
            description = "Builds the TypeScript Figma boundary from the Kotlin project configuration."
            dependsOn(writeWriterProjectConfig)
            inputs.file(generatedWriterProjectConfigFile)
            workingDir(toolsDirectory)
            doFirst {
                commandLine(
                    "node",
                    "bin/build.mjs",
                    "--project-config-json=${generatedWriterProjectConfigFile.get().asFile.absolutePath}",
                    "--output-dir=."
                )
            }
        }

        project.tasks.register<Exec>("testFigmaDocumentationSyncTools") {
            group = "verification"
            description = "Tests the TypeScript Figma boundary against the Kotlin project configuration."
            dependsOn(writeWriterProjectConfig)
            inputs.file(generatedWriterProjectConfigFile)
            workingDir(toolsDirectory)
            commandLine(
                if (HostOperatingSystem.isWindows) "npm.cmd" else "npm",
                "test"
            )
            doFirst {
                environment(
                    "FIGMA_DOCUMENTATION_SYNC_PROJECT_CONFIG",
                    generatedWriterProjectConfigFile.get().asFile.absolutePath
                )
            }
        }

        project.tasks.named<PrepareCanonicalFigmaSyncTask>("prepareCanonicalFigmaSync") {
            dependsOn(writeWriterProjectConfig)
            writerProjectConfigFile.set(generatedWriterProjectConfigFile)
        }

        project.tasks.named<GenerateCiVisualPlanTask>("generateFigmaCiVisualPlan") {
            dependsOn(writeWriterProjectConfig)
            writerProjectConfigFile.set(generatedWriterProjectConfigFile)
        }

        project.tasks.named<RunFigmaMcpTask>("runFigmaMcp") {
            dependsOn(writeWriterProjectConfig)
            writerProjectConfigFile.set(generatedWriterProjectConfigFile)
        }

        project.tasks.named<ProbeFigmaMcpTask>("probeFigmaMcp") {
            dependsOn(writeWriterProjectConfig)
            writerProjectConfigFile.set(generatedWriterProjectConfigFile)
        }
    }
}
