package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.figmaDocumentationSync.plugin.gradle.platform.HostOperatingSystem
import com.marmatsan.figmaDocumentationSync.plugin.task.canonical.PrepareCanonicalFigmaSyncTask
import com.marmatsan.figmaDocumentationSync.plugin.task.config.WriteFigmaWriterProjectConfigTask
import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.ProbeFigmaMcpTask
import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.RunFigmaMcpTask
import com.marmatsan.figmaDocumentationSync.plugin.task.visual.GenerateCiVisualPlanTask
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/** Registers and wires the tasks that materialize and test the configured Figma writer. */
internal class FigmaWriterTasksRegistrar(
    private val context: FigmaPluginContext
) {
    /** Registers writer configuration, build, test, canonical, visual, and MCP task bindings. */
    fun register() {
        val project = context.project
        val writeWriterProjectConfig =
            project.tasks.register<WriteFigmaWriterProjectConfigTask>("writeFigmaWriterProjectConfig") {
                group = "figma design sync"
                description = "Writes the consumer's Figma writer configuration as transient JSON."
                configurationJson.set(context.extension.writerProjectConfigJson)
                outputFile.set(
                    project.layout.buildDirectory.file(
                        "generated/figma-documentation-sync/writer-project-config.json"
                    )
                )
            }

        val generatedWriterProjectConfigFile = writeWriterProjectConfig.flatMap { task -> task.outputFile }
        val toolsDirectory = context.extension.toolsDirectory
        val writerToolsInstallationMarker =
            project.layout.buildDirectory.file(
                "figma-documentation-sync/tools-installation/package-lock.marker"
            )
        val installWriterTools =
            project.tasks.register<Exec>("installFigmaDocumentationSyncTools") {
                group = "figma design sync"
                description = "Installs the locked Node.js dependencies required by the Figma writer."
                inputs.files(
                    toolsDirectory.file("package.json"),
                    toolsDirectory.file("package-lock.json")
                )
                outputs.file(writerToolsInstallationMarker)
                outputs.upToDateWhen {
                    toolsDirectory
                        .file("node_modules/esbuild/package.json")
                        .get()
                        .asFile.isFile
                }
                workingDir(toolsDirectory)
                commandLine(
                    if (HostOperatingSystem.isWindows) "npm.cmd" else "npm",
                    "ci"
                )
                doLast {
                    writerToolsInstallationMarker
                        .get()
                        .asFile
                        .apply {
                            parentFile.mkdirs()
                            writeText("installed from package-lock.json")
                        }
                }
            }

        project.tasks.register<Exec>("buildFigmaDocumentationSyncTools") {
            group = "figma design sync"
            description = "Builds the TypeScript Figma boundary from the Kotlin project configuration."
            dependsOn(
                installWriterTools,
                writeWriterProjectConfig
            )
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
            dependsOn(
                installWriterTools,
                writeWriterProjectConfig
            )
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

        project.afterEvaluate {
            if (context.extension.writerProjectConfigJson.isPresent) {
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
    }
}
