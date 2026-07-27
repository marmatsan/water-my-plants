package com.marmatsan.figmaDocumentationSync.plugin.task.mcp

import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterRuntimeConfigJson
import com.marmatsan.figmaDocumentationSync.data.mcp.McpRunnerExecutor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/** Probes the local MCP endpoint through the official Kotlin SDK transport. */
@DisableCachingByDefault(
    because = "Connects to a local MCP endpoint",
)
abstract class ProbeFigmaMcpTask
    @Inject
    constructor() : DefaultTask() {
        /** Streamable HTTP endpoint whose advertised tools are inspected. */
        @get:Input
        abstract val endpoint: Property<String>

        /** Runtime project contract providing the MCP client identity. */
        @get:InputFile
        @get:PathSensitive(PathSensitivity.RELATIVE)
        abstract val writerProjectConfigFile: RegularFileProperty

        /** Connects to the endpoint and prints its normalized Figma capabilities as JSON. */
        @TaskAction
        fun probe() {
            val config = FigmaWriterRuntimeConfigJson.read(writerProjectConfigFile.get().asFile.absolutePath)
            val capabilities =
                McpRunnerExecutor().probe(
                    endpoint = endpoint.get(),
                    clientName = config.mcpClientName,
                )
            val output =
                buildJsonObject {
                    put(
                        "toolNames",
                        JsonArray(
                            capabilities.toolNames.map(
                                transform = ::JsonPrimitive,
                            ),
                        ),
                    )
                    put(
                        "canUseFigma",
                        capabilities.canUseFigma,
                    )
                    put(
                        "canUploadAssets",
                        capabilities.canUploadAssets,
                    )
                    put(
                        "writeCapable",
                        capabilities.writeCapable,
                    )
                }
            logger.lifecycle(
                prettyJson.encodeToString(
                    JsonObject.serializer(),
                    output,
                ),
            )
        }

        private companion object {
            val prettyJson = Json { prettyPrint = true }
        }
    }
