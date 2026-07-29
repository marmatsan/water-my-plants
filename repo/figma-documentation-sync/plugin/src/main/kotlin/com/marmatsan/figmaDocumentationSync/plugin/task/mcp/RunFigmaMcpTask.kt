package com.marmatsan.figmaDocumentationSync.plugin.task.mcp

import com.marmatsan.figmaDocumentationSync.data.json.writer.FigmaWriterRuntimeConfigJson
import com.marmatsan.figmaDocumentationSync.data.mcp.McpRunnerExecutor
import com.marmatsan.figmaDocumentationSync.domain.model.writer.McpExecutionOptions
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/** Inspects, records, or executes a canonical checkpointed runner through the Kotlin MCP client. */
@DisableCachingByDefault(
    because = "May invoke a local write-capable MCP endpoint"
)
abstract class RunFigmaMcpTask
    @Inject
    constructor() : DefaultTask() {
        /** Path to the executable runner manifest. */
        @get:Input
        abstract val manifestPath: Property<String>

        /** Optional visual sync plan used to select execution scopes. */
        @get:Input
        @get:Optional
        abstract val planPath: Property<String>

        /** Optional checkpoint path overriding the manifest-local default. */
        @get:Input
        @get:Optional
        abstract val statePath: Property<String>

        /** Optional previous visual state used for incremental planning. */
        @get:Input
        @get:Optional
        abstract val visualStatePath: Property<String>

        /** Streamable HTTP MCP endpoint used for execution. */
        @get:Input
        abstract val endpoint: Property<String>

        /** Whether an existing compatible checkpoint must be resumed. */
        @get:Input
        abstract val resume: Property<Boolean>

        /** Whether execution starts again from the last failed file. */
        @get:Input
        abstract val retryFailed: Property<Boolean>

        /** Whether compatible canonical staging may be reused. */
        @get:Input
        abstract val reuseStaging: Property<Boolean>

        /** Whether to inspect the planned execution without calling write tools. */
        @get:Input
        abstract val dryRun: Property<Boolean>

        /** Whether to print only the next pending runner file. */
        @get:Input
        abstract val next: Property<Boolean>

        /** Optional runner file from which execution begins. */
        @get:Input
        @get:Optional
        abstract val from: Property<String>

        /** Optional externally executed file to record as successful. */
        @get:Input
        @get:Optional
        abstract val recordSuccess: Property<String>

        /** Optional externally executed file to record as failed. */
        @get:Input
        @get:Optional
        abstract val recordFailure: Property<String>

        /** Operator summary stored with an externally recorded result. */
        @get:Input
        @get:Optional
        abstract val summary: Property<String>

        /** Runtime project contract providing Figma and MCP identities. */
        @get:InputFile
        @get:PathSensitive(PathSensitivity.RELATIVE)
        abstract val writerProjectConfigFile: RegularFileProperty

        /** Selects inspection, manual recording, or checkpointed MCP execution from task inputs. */
        @TaskAction
        fun runMcp() {
            val config = FigmaWriterRuntimeConfigJson.read(writerProjectConfigFile.get().asFile.absolutePath)
            val options =
                McpExecutionOptions(
                    resume = resume.get(),
                    retryFailed = retryFailed.get(),
                    reuseStaging = reuseStaging.get(),
                    from = from.orNull
                )
            val request =
                McpRunnerExecutor.Request(
                    manifestPath = manifestPath.get(),
                    endpoint = endpoint.get(),
                    fileKey = config.figmaFileKey,
                    clientName = config.mcpClientName,
                    projectDisplayName = config.projectDisplayName,
                    statePath = statePath.orNull,
                    visualStatePath = visualStatePath.orNull,
                    planPath = planPath.orNull,
                    options = options
                )
            val executor = McpRunnerExecutor()
            require(recordSuccess.orNull == null || recordFailure.orNull == null) {
                "Use only one of figmaMcpRecordSuccess or figmaMcpRecordFailure."
            }

            when {
                dryRun.get() || next.get() -> {
                    val inspection = executor.inspect(request)
                    if (next.get()) {
                        logger.lifecycle(inspection.executionFiles.firstOrNull() ?: "COMPLETE")
                    } else {
                        val output =
                            buildJsonObject {
                                put(
                                    "manifestHash",
                                    inspection.manifestHash
                                )
                                put(
                                    "statePath",
                                    inspection.statePath
                                )
                                put(
                                    "reuseStaging",
                                    inspection.reuseStaging
                                )
                                put(
                                    "decision",
                                    inspection.decision?.let(::JsonPrimitive) ?: JsonNull
                                )
                                put(
                                    "executionScopes",
                                    inspection.executionScopes
                                        ?.map(
                                            transform = ::JsonPrimitive
                                        )?.let(::JsonArray)
                                        ?: JsonNull
                                )
                                put(
                                    "executionFiles",
                                    JsonArray(
                                        inspection.executionFiles.map(
                                            transform = ::JsonPrimitive
                                        )
                                    )
                                )
                            }
                        logger.lifecycle(
                            prettyJson.encodeToString(
                                JsonObject.serializer(),
                                output
                            )
                        )
                    }
                }

                recordSuccess.orNull != null || recordFailure.orNull != null -> {
                    val successFile = recordSuccess.orNull
                    val file = successFile ?: recordFailure.get()
                    executor.record(
                        request = request,
                        file = file,
                        success = successFile != null,
                        summary = summary.orNull ?: "Recorded by MCP operator"
                    )
                    logger.lifecycle("${if (successFile != null) "Completed" else "Failed"}: $file")
                }

                else -> {
                    val result = executor.execute(request)
                    logger.lifecycle(
                        "Figma MCP complete: ${result.executionFiles.size} unit(s); " +
                            "${result.toolNames.size} advertised tool(s)."
                    )
                }
            }
        }

        private companion object {
            val prettyJson = Json { prettyPrint = true }
        }
    }
