package com.marmatsan.figmaDesignSync.data.json.visual

import com.marmatsan.figmaDesignSync.domain.model.ci.CiConfiguration
import com.marmatsan.figmaDesignSync.domain.model.ci.CiConnection
import com.marmatsan.figmaDesignSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDesignSync.domain.model.ci.CiJob
import com.marmatsan.figmaDesignSync.domain.model.ci.CiNode
import com.marmatsan.figmaDesignSync.domain.model.ci.CiPipeline
import com.marmatsan.figmaDesignSync.domain.model.ci.CiTrigger
import com.marmatsan.figmaDesignSync.domain.model.ci.CiVcsRoot
import com.marmatsan.figmaDesignSync.domain.model.ci.CiWindowsRuntime
import com.marmatsan.figmaDesignSync.domain.model.visual.CiVisualPlan
import com.marmatsan.figmaDesignSync.domain.model.visual.CiVisualPlanConfig
import com.marmatsan.figmaDesignSync.domain.service.visual.CiVisualPlanner
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
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

/** JSON boundary between the official design model and the Kotlin CI visual planner. */
object CiVisualPlanJson {
    private val prettyJson = Json { prettyPrint = true }

    fun create(
        designModel: JsonObject,
        config: CiVisualPlanConfig,
        target: String? = null
    ): JsonObject {
        val ci = designModel["content"]?.jsonObject?.get("ci")?.jsonObject
            ?: throw IllegalArgumentException("designModel.content.ci is required for CI visual sync.")
        val plan = CiVisualPlanner().create(
            externalTopology = ci.requiredObject("externalTopology").toExternalTopology(),
            windowsRuntime = ci.requiredObject("windowsRuntime").toWindowsRuntime(),
            configuration = ci.requiredObject(config.configurationModelName).toCiConfiguration(),
            config = config
        )
        val selected = target?.let { requested ->
            plan.copy(sections = plan.sections.filter { section -> section.target == requested })
                .also { filtered ->
                    require(filtered.sections.size == 1) { "CI visual plan has no section for target '$requested'." }
                }
        } ?: plan
        return selected.toJson()
    }

    fun write(
        designModelPath: String,
        config: CiVisualPlanConfig,
        target: String? = null,
        outputPath: String
    ) {
        val designModel = Json.parseToJsonElement(
            Files.readString(Path.of(designModelPath)).removePrefix(UTF8_BOM)
        ).jsonObject
        val destination = Path.of(outputPath)
        destination.parent?.let(Files::createDirectories)
        Files.writeString(
            destination,
            prettyJson.encodeToString(JsonObject.serializer(), create(designModel, config, target)) +
                System.lineSeparator()
        )
    }

    private fun JsonObject.toExternalTopology(): CiExternalTopology = CiExternalTopology(
        schemaVersion = requiredInt("schemaVersion"),
        validation = requiredObject("validation").let { validation ->
            CiExternalTopology.Validation(
                lastValidatedOn = LocalDate.parse(validation.requiredString("lastValidatedOn")),
                warnAfterDays = validation.requiredInt("warnAfterDays")
            )
        },
        nodes = requiredArray("nodes").map { element ->
            val node = element.jsonObject
            CiNode(
                id = node.requiredString("id"),
                type = CiNode.Type.entries.single { type -> type.serializedName == node.requiredString("type") },
                name = node.requiredString("name"),
                description = node.requiredString("description")
            )
        },
        connections = requiredArray("connections").map { element ->
            val connection = element.jsonObject
            CiConnection(
                id = connection.requiredString("id"),
                sourceNodeId = connection.requiredString("source"),
                targetNodeId = connection.requiredString("target"),
                label = connection.requiredString("label"),
                description = connection.requiredString("description"),
                protocol = connection.optionalString("protocol"),
                authentication = connection.requiredArray("authentication").map { it.jsonPrimitive.content },
                policy = connection.optionalString("policy"),
                path = connection.optionalString("path"),
                automation = CiConnection.Automation.entries.single { automation ->
                    automation.serializedName == connection.requiredString("automation")
                },
                annotation = connection.optionalString("annotation")
            )
        }
    )

    private fun JsonObject.toWindowsRuntime(): CiWindowsRuntime = CiWindowsRuntime(
        schemaVersion = requiredInt("schemaVersion"),
        validation = requiredObject("validation").let { validation ->
            CiWindowsRuntime.Validation(
                lastValidatedOn = LocalDate.parse(validation.requiredString("lastValidatedOn")),
                warnAfterDays = validation.requiredInt("warnAfterDays")
            )
        },
        platform = requiredString("platform"),
        services = requiredArray("services").map { element ->
            val service = element.jsonObject
            CiWindowsRuntime.Service(
                id = service.requiredString("id"),
                name = service.requiredString("name"),
                description = service.requiredString("description"),
                service = service.requiredString("service"),
                startup = service.requiredString("startup"),
                identity = service.requiredString("identity")
            )
        }
    )

    private fun JsonObject.toCiConfiguration(): CiConfiguration = CiConfiguration(
        pipelines = requiredArray("pipelines").map { element -> element.jsonObject.toCiPipeline() },
        vcsRoots = requiredArray("vcsRoots").map { element ->
            val root = element.jsonObject
            CiVcsRoot(
                id = root.requiredString("id"),
                name = root.requiredString("name"),
                url = root.requiredString("url"),
                defaultBranchRef = root.requiredString("defaultBranchRef"),
                branchSpec = root.requiredArray("branchSpec").map { it.jsonPrimitive.content }
            )
        }
    )

    private fun JsonObject.toCiPipeline(): CiPipeline = CiPipeline(
        id = requiredString("id"),
        name = requiredString("name"),
        triggers = requiredArray("triggers").map { element ->
            val trigger = element.jsonObject
            CiTrigger(
                type = CiTrigger.Type.entries.single { type -> type.serializedName == trigger.requiredString("type") },
                branchFilter = trigger.optionalString("branchFilter"),
                dependencyPipelineId = trigger.optionalString("dependencyPipelineId"),
                afterSuccessfulBuildOnly = trigger["afterSuccessfulBuildOnly"]?.jsonPrimitive?.booleanOrNull
            )
        },
        jobs = requiredArray("jobs").map { element -> element.jsonObject.toCiJob() }
    )

    private fun JsonObject.toCiJob(): CiJob = CiJob(
        id = requiredString("id"),
        name = requiredString("name"),
        steps = requiredArray("steps").map { element ->
            val step = element.jsonObject
            CiJob.Step(step.requiredString("id"), step.requiredString("name"), step.requiredString("command"))
        },
        repositoryIds = requiredArray("repositoryIds").map { it.jsonPrimitive.content },
        artifacts = requiredArray("artifacts").map { element ->
            val artifact = element.jsonObject
            CiJob.Artifact(
                artifact.requiredString("path"),
                artifact.requiredBoolean("publish"),
                artifact.requiredBoolean("shareWithJobs")
            )
        },
        dependencies = requiredArray("dependencies").map { element ->
            val dependency = element.jsonObject
            CiJob.Dependency(
                dependency.requiredString("jobId"),
                dependency.requiredArray("artifactPaths").map { it.jsonPrimitive.content }
            )
        },
        publishedChecks = requiredArray("publishedChecks").map { element ->
            CiJob.PublishedCheck(element.jsonObject.requiredString("name"))
        }
    )

    private fun CiVisualPlan.toJson(): JsonObject = buildJsonObject {
        put("parentName", parentName)
        put("sections", JsonArray(sections.map { section -> section.toJson() }))
    }

    private fun CiVisualPlan.Section.toJson(): JsonObject = buildJsonObject {
        put("target", target)
        put("name", name)
        put("description", description)
        put("orientation", orientation.wireValue)
        put("headerSources", JsonArray(headerSources.map { source ->
            buildJsonObject {
                put("label", source.label)
                put("url", source.url)
            }
        }))
        put("nodes", JsonArray(nodes.map { node -> node.toJson() }))
        put("connections", JsonArray(connections.map { connection ->
            buildJsonObject {
                put("id", connection.id)
                put("source", connection.source)
                put("target", connection.target)
                put("label", connection.label)
            }
        }))
    }

    private fun CiVisualPlan.Node.toJson(): JsonObject = buildJsonObject {
        put("id", id)
        put("type", type.wireValue)
        put("environment", environment.wireValue)
        put("name", name)
        put("description", description)
        steps?.let { put("steps", it) }
        runtime?.let { value ->
            put("runtime", buildJsonObject {
                put("platform", value.platform)
                put("service", value.service)
                put("startup", value.startup)
                put("identity", value.identity)
            })
        }
        put("source", source)
        put("sourceUrl", sourceUrl)
        put("row", row)
        put("column", column)
    }

    private fun JsonObject.requiredObject(name: String): JsonObject =
        this[name]?.jsonObject ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private fun JsonObject.requiredArray(name: String): JsonArray =
        this[name]?.jsonArray ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private fun JsonObject.requiredString(name: String): String =
        this[name]?.jsonPrimitive?.contentOrNull ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private fun JsonObject.optionalString(name: String): String? =
        this[name]?.takeUnless { it is JsonNull }?.jsonPrimitive?.contentOrNull

    private fun JsonObject.requiredInt(name: String): Int =
        this[name]?.jsonPrimitive?.int ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private fun JsonObject.requiredBoolean(name: String): Boolean =
        this[name]?.jsonPrimitive?.booleanOrNull ?: throw IllegalArgumentException("CI visual model is missing '$name'.")

    private const val UTF8_BOM = "\uFEFF"
}
