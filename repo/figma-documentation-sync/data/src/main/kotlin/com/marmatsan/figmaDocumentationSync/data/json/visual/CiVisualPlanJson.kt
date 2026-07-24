package com.marmatsan.figmaDocumentationSync.data.json.visual

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConnection
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiJob
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiNode
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiTrigger
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiVcsRoot
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlan
import com.marmatsan.figmaDocumentationSync.domain.model.visual.CiVisualPlanConfig
import com.marmatsan.figmaDocumentationSync.domain.service.visual.CiVisualPlanner
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

/** JSON boundary between the canonical design model and the Kotlin CI visual planner. */
object CiVisualPlanJson {
    private val prettyJson = Json { prettyPrint = true }

    fun create(
        designModel: JsonObject,
        config: CiVisualPlanConfig,
        target: String? = null,
    ): JsonObject {
        val ci =
            designModel["content"]
                ?.jsonObject
                ?.get(
                    key = "ci",
                )?.jsonObject
                ?: throw IllegalArgumentException("designModel.content.ci is required for CI visual sync.")
        val plan =
            CiVisualPlanner().create(
                externalTopology =
                    ci
                        .requiredObject(
                            name = "externalTopology",
                        ).toExternalTopology(),
                windowsRuntime =
                    ci
                        .requiredObject(
                            name = "windowsRuntime",
                        ).toWindowsRuntime(),
                configuration =
                    ci
                        .requiredObject(
                            name = config.configurationModelName,
                        ).toCiConfiguration(),
                config = config,
            )
        val selected =
            target?.let { requested ->
                plan
                    .copy(
                        sections = plan.sections.filter { section -> section.target == requested },
                    ).also { filtered ->
                        require(
                            filtered.sections.size == 1,
                        ) { "CI visual plan has no section for target '$requested'." }
                    }
            } ?: plan
        return selected.toJson()
    }

    fun write(
        designModelPath: String,
        config: CiVisualPlanConfig,
        target: String? = null,
        outputPath: String,
    ) {
        val designModel =
            Json
                .parseToJsonElement(
                    Files
                        .readString(
                            Path.of(
                                designModelPath,
                            ),
                        ).removePrefix(UTF8_BOM),
                ).jsonObject
        val destination =
            Path.of(
                outputPath,
            )
        destination.parent?.let(Files::createDirectories)
        Files.writeString(
            destination,
            prettyJson.encodeToString(
                JsonObject.serializer(),
                create(
                    designModel = designModel,
                    config = config,
                    target = target,
                ),
            ) +
                System.lineSeparator(),
        )
    }

    private fun JsonObject.toExternalTopology(): CiExternalTopology =
        CiExternalTopology(
            schemaVersion =
                requiredInt(
                    name = "schemaVersion",
                ),
            validation =
                requiredObject(
                    name = "validation",
                ).let { validation ->
                    CiExternalTopology.Validation(
                        lastValidatedOn =
                            LocalDate.parse(
                                validation.requiredString("lastValidatedOn"),
                            ),
                        warnAfterDays = validation.requiredInt("warnAfterDays"),
                    )
                },
            nodes =
                requiredArray(
                    name = "nodes",
                ).map { element ->
                    val node = element.jsonObject
                    CiNode(
                        id = node.requiredString("id"),
                        type =
                            CiNode.Type.entries.single { type ->
                                type.serializedName == node.requiredString("type")
                            },
                        name = node.requiredString("name"),
                        description = node.requiredString("description"),
                    )
                },
            connections =
                requiredArray(
                    name = "connections",
                ).map { element ->
                    val connection = element.jsonObject
                    CiConnection(
                        id = connection.requiredString("id"),
                        sourceNodeId = connection.requiredString("source"),
                        targetNodeId = connection.requiredString("target"),
                        label = connection.requiredString("label"),
                        description = connection.requiredString("description"),
                        protocol = connection.optionalString("protocol"),
                        authentication =
                            connection
                                .requiredArray(
                                    name = "authentication",
                                ).map { it.jsonPrimitive.content },
                        policy = connection.optionalString("policy"),
                        path = connection.optionalString("path"),
                        automation =
                            CiConnection.Automation.entries.single { automation ->
                                automation.serializedName == connection.requiredString("automation")
                            },
                        annotation = connection.optionalString("annotation"),
                    )
                },
        )

    private fun JsonObject.toWindowsRuntime(): CiWindowsRuntime =
        CiWindowsRuntime(
            schemaVersion =
                requiredInt(
                    name = "schemaVersion",
                ),
            validation =
                requiredObject(
                    name = "validation",
                ).let { validation ->
                    CiWindowsRuntime.Validation(
                        lastValidatedOn =
                            LocalDate.parse(
                                validation.requiredString("lastValidatedOn"),
                            ),
                        warnAfterDays = validation.requiredInt("warnAfterDays"),
                    )
                },
            platform =
                requiredString(
                    name = "platform",
                ),
            services =
                requiredArray(
                    name = "services",
                ).map { element ->
                    val service = element.jsonObject
                    CiWindowsRuntime.Service(
                        id = service.requiredString("id"),
                        name = service.requiredString("name"),
                        description = service.requiredString("description"),
                        service = service.requiredString("service"),
                        startup = service.requiredString("startup"),
                        identity = service.requiredString("identity"),
                    )
                },
        )

    private fun JsonObject.toCiConfiguration(): CiConfiguration =
        CiConfiguration(
            pipelines =
                requiredArray(
                    name = "pipelines",
                ).map { element -> element.jsonObject.toCiPipeline() },
            vcsRoots =
                requiredArray(
                    name = "vcsRoots",
                ).map { element ->
                    val root = element.jsonObject
                    CiVcsRoot(
                        id = root.requiredString("id"),
                        name = root.requiredString("name"),
                        url = root.requiredString("url"),
                        defaultBranchRef = root.requiredString("defaultBranchRef"),
                        branchSpec =
                            root
                                .requiredArray(
                                    name = "branchSpec",
                                ).map { it.jsonPrimitive.content },
                    )
                },
        )

    private fun JsonObject.toCiPipeline(): CiPipeline =
        CiPipeline(
            id =
                requiredString(
                    name = "id",
                ),
            name =
                requiredString(
                    name = "name",
                ),
            triggers =
                requiredArray(
                    name = "triggers",
                ).map { element ->
                    val trigger = element.jsonObject
                    CiTrigger(
                        type =
                            CiTrigger.Type.entries.single { type ->
                                type.serializedName ==
                                    trigger.requiredString("type")
                            },
                        branchFilter = trigger.optionalString("branchFilter"),
                        dependencyPipelineId = trigger.optionalString("dependencyPipelineId"),
                        afterSuccessfulBuildOnly = trigger["afterSuccessfulBuildOnly"]?.jsonPrimitive?.booleanOrNull,
                    )
                },
            jobs =
                requiredArray(
                    name = "jobs",
                ).map { element -> element.jsonObject.toCiJob() },
        )

    private fun JsonObject.toCiJob(): CiJob =
        CiJob(
            id =
                requiredString(
                    name = "id",
                ),
            name =
                requiredString(
                    name = "name",
                ),
            steps =
                requiredArray(
                    name = "steps",
                ).map { element ->
                    val step = element.jsonObject
                    CiJob.Step(
                        id = step.requiredString("id"),
                        name = step.requiredString("name"),
                        command = step.requiredString("command"),
                    )
                },
            repositoryIds =
                requiredArray(
                    name = "repositoryIds",
                ).map { it.jsonPrimitive.content },
            artifacts =
                requiredArray(
                    name = "artifacts",
                ).map { element ->
                    val artifact = element.jsonObject
                    CiJob.Artifact(
                        artifact.requiredString("path"),
                        artifact.requiredBoolean("publish"),
                        artifact.requiredBoolean("shareWithJobs"),
                    )
                },
            dependencies =
                requiredArray(
                    name = "dependencies",
                ).map { element ->
                    val dependency = element.jsonObject
                    CiJob.Dependency(
                        jobId = dependency.requiredString("jobId"),
                        artifactPaths =
                            dependency
                                .requiredArray(
                                    name = "artifactPaths",
                                ).map { it.jsonPrimitive.content },
                    )
                },
            publishedChecks =
                requiredArray(
                    name = "publishedChecks",
                ).map { element ->
                    CiJob.PublishedCheck(
                        name = element.jsonObject.requiredString("name"),
                    )
                },
        )

    private fun CiVisualPlan.toJson(): JsonObject =
        buildJsonObject {
            put(
                "schemaVersion",
                4,
            )
            put(
                "parentName",
                parentName,
            )
            put(
                "sections",
                JsonArray(sections.map { section -> section.toJson() }),
            )
        }

    private fun CiVisualPlan.Section.toJson(): JsonObject =
        buildJsonObject {
            put(
                "target",
                target,
            )
            put(
                "name",
                name,
            )
            put(
                "description",
                description,
            )
            put(
                "orientation",
                orientation.wireValue,
            )
            put(
                "headerSources",
                JsonArray(
                    headerSources.map { source ->
                        buildJsonObject {
                            put(
                                "label",
                                source.label,
                            )
                            put(
                                "url",
                                source.url,
                            )
                        }
                    },
                ),
            )
            put(
                "nodes",
                JsonArray(nodes.map { node -> node.toJson() }),
            )
            put(
                "connections",
                JsonArray(
                    connections.map { connection ->
                        buildJsonObject {
                            put(
                                "id",
                                connection.id,
                            )
                            put(
                                "source",
                                connection.source,
                            )
                            put(
                                "target",
                                connection.target,
                            )
                            put(
                                "label",
                                connection.label,
                            )
                            put(
                                "kind",
                                connection.kind.wireValue,
                            )
                        }
                    },
                ),
            )
        }

    private fun CiVisualPlan.Node.toJson(): JsonObject =
        buildJsonObject {
            put(
                "id",
                id,
            )
            put(
                "type",
                type.wireValue,
            )
            put(
                "environment",
                environment.wireValue,
            )
            put(
                "name",
                name,
            )
            put(
                "description",
                description,
            )
            put(
                "phases",
                JsonArray(phases.map { phase -> phase.toJson() }),
            )
            put(
                "outcomes",
                JsonArray(outcomes.map { outcome -> outcome.toJson() }),
            )
            runtime?.let { value ->
                put(
                    "runtime",
                    buildJsonObject {
                        put(
                            "platform",
                            value.platform,
                        )
                        put(
                            "service",
                            value.service,
                        )
                        put(
                            "startup",
                            value.startup,
                        )
                        put(
                            "identity",
                            value.identity,
                        )
                    },
                )
            }
            put(
                "source",
                source,
            )
            put(
                "sourceUrl",
                sourceUrl,
            )
            put(
                "row",
                row,
            )
            put(
                "column",
                column,
            )
        }

    private fun CiVisualPlan.Phase.toJson(): JsonObject =
        buildJsonObject {
            put(
                "order",
                order,
            )
            put(
                "title",
                title,
            )
            technicalId?.let { value ->
                put(
                    "technicalId",
                    value,
                )
            }
            description?.let { value ->
                put(
                    "description",
                    value,
                )
            }
            put(
                "steps",
                JsonArray(steps.map { step -> step.toJson() }),
            )
        }

    private fun CiVisualPlan.Step.toJson(): JsonObject =
        buildJsonObject {
            put(
                "order",
                order,
            )
            put(
                "role",
                role.wireValue,
            )
            put(
                "title",
                title,
            )
            technicalId?.let { value ->
                put(
                    "technicalId",
                    value,
                )
            }
            description?.let { value ->
                put(
                    "description",
                    value,
                )
            }
            condition?.let { value ->
                put(
                    "condition",
                    value,
                )
            }
        }

    private fun CiVisualPlan.Outcome.toJson(): JsonObject =
        buildJsonObject {
            put(
                "order",
                order,
            )
            put(
                "kind",
                kind.wireValue,
            )
            put(
                "title",
                title,
            )
            technicalId?.let { value ->
                put(
                    "technicalId",
                    value,
                )
            }
            description?.let { value ->
                put(
                    "description",
                    value,
                )
            }
            condition?.let { value ->
                put(
                    "condition",
                    value,
                )
            }
        }

    private fun JsonObject.requiredObject(
        name: String,
    ): JsonObject =
        this[name]?.jsonObject ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private fun JsonObject.requiredArray(
        name: String,
    ): JsonArray =
        this[name]?.jsonArray ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private fun JsonObject.requiredString(
        name: String,
    ): String =
        this[name]?.jsonPrimitive?.contentOrNull
            ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private fun JsonObject.optionalString(
        name: String,
    ): String? =
        this[name]?.takeUnless { it is JsonNull }?.jsonPrimitive?.contentOrNull

    private fun JsonObject.requiredInt(
        name: String,
    ): Int =
        this[name]?.jsonPrimitive?.int ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private fun JsonObject.requiredBoolean(
        name: String,
    ): Boolean =
        this[name]?.jsonPrimitive?.booleanOrNull
            ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private const val UTF8_BOM = "\uFEFF"
}
