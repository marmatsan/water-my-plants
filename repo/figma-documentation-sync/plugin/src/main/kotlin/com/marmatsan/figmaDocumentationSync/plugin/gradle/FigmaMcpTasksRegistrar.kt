package com.marmatsan.figmaDocumentationSync.plugin.gradle

import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.ProbeFigmaMcpTask
import com.marmatsan.figmaDocumentationSync.plugin.task.mcp.RunFigmaMcpTask
import org.gradle.kotlin.dsl.register
import java.io.File

/** Registers local MCP execution and endpoint-probe tasks. */
internal class FigmaMcpTasksRegistrar(
    private val context: FigmaPluginContext,
) {
    /** Registers the local MCP runner and endpoint capability probe. */
    fun register() {
        registerRunner()
        registerProbe()
    }

    private fun registerRunner() {
        val project = context.project
        project.tasks.register<RunFigmaMcpTask>("runFigmaMcp") {
            group = "documentation"
            description = "Inspects, records, or executes a generated runner through the Kotlin MCP client."
            manifestPath.convention(project.providers.gradleProperty("figmaMcpManifest"))
            planPath.convention(project.providers.gradleProperty("figmaMcpPlan"))
            statePath.convention(project.providers.gradleProperty("figmaMcpState"))
            visualStatePath.convention(project.providers.gradleProperty("figmaMcpVisualState"))
            endpoint.convention(
                project.providers.gradleProperty("figmaMcpEndpoint").orElse(DEFAULT_MCP_ENDPOINT),
            )
            resume.convention(context.booleanProperty("figmaMcpResume"))
            retryFailed.convention(context.booleanProperty("figmaMcpRetryFailed"))
            reuseStaging.convention(context.booleanProperty("figmaMcpReuseStaging"))
            dryRun.convention(context.booleanProperty("figmaMcpDryRun"))
            next.convention(context.booleanProperty("figmaMcpNext"))
            from.convention(project.providers.gradleProperty("figmaMcpFrom"))
            recordSuccess.convention(project.providers.gradleProperty("figmaMcpRecordSuccess"))
            recordFailure.convention(project.providers.gradleProperty("figmaMcpRecordFailure"))
            summary.convention(project.providers.gradleProperty("figmaMcpSummary"))
            writerProjectConfigFile.set(
                project.layout.file(
                    project.providers.gradleProperty("figmaWriterProjectConfig").map(::File),
                ),
            )
        }
    }

    private fun registerProbe() {
        val project = context.project
        project.tasks.register<ProbeFigmaMcpTask>("probeFigmaMcp") {
            group = "verification"
            description = "Probes the local MCP endpoint with the official Kotlin SDK client."
            endpoint.convention(
                project.providers.gradleProperty("figmaMcpEndpoint").orElse(DEFAULT_MCP_ENDPOINT),
            )
            writerProjectConfigFile.set(
                project.layout.file(
                    project.providers.gradleProperty("figmaWriterProjectConfig").map(::File),
                ),
            )
        }
    }

    private companion object {
        const val DEFAULT_MCP_ENDPOINT = "http://127.0.0.1:3845/mcp"
    }
}
