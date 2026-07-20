package com.marmatsan.figmaDocumentationSync.data.yaml.ci

import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiConnection
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiExternalTopology
import com.marmatsan.figmaDocumentationSync.domain.model.ci.CiNode
import me.tatarka.inject.annotations.Inject
import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import java.io.File
import java.time.LocalDate

/**
 * Parses the repository-owned external CI topology YAML.
 */
@Inject
class CiExternalTopologyYamlReader {
    fun read(
        file: File
    ): CiExternalTopology {
        val settings = LoadSettings.builder()
            .setLabel(file.path)
            .build()
        val root = file.inputStream().use { input ->
            Load(settings).loadFromInputStream(input)
        }.asStringMap("root")

        val validation = root.requiredMap("validation")

        return CiExternalTopology(
            schemaVersion = root.requiredInt("schemaVersion"),
            validation = CiExternalTopology.Validation(
                lastValidatedOn = LocalDate.parse(validation.requiredString("lastValidatedOn")),
                warnAfterDays = validation.requiredInt("warnAfterDays")
            ),
            nodes = root.requiredList("nodes").map(::readNode),
            connections = root.requiredList("connections").map(::readConnection)
        )
    }

    private fun readNode(
        value: Any?
    ): CiNode {
        val node = value.asStringMap("node")
        val serializedType = node.requiredString("type")
        val type = CiNode.Type.entries.singleOrNull { candidate ->
            candidate.serializedName == serializedType
        } ?: error("Unsupported CI node type '$serializedType'")

        return CiNode(
            id = node.requiredString("id"),
            type = type,
            name = node.requiredString("name"),
            description = node.requiredString("description")
        )
    }

    private fun readConnection(
        value: Any?
    ): CiConnection {
        val connection = value.asStringMap("connection")
        val serializedAutomation = connection.requiredString("automation")
        val automation = CiConnection.Automation.entries.singleOrNull { candidate ->
            candidate.serializedName == serializedAutomation
        } ?: error("Unsupported CI connection automation '$serializedAutomation'")

        return CiConnection(
            id = connection.requiredString("id"),
            sourceNodeId = connection.requiredString("source"),
            targetNodeId = connection.requiredString("target"),
            label = connection.requiredString("label"),
            description = connection.requiredString("description"),
            protocol = connection.optionalString("protocol"),
            authentication = connection.optionalStringList("authentication"),
            policy = connection.optionalString("policy"),
            path = connection.optionalString("path"),
            automation = automation,
            annotation = connection.optionalString("annotation")
        )
    }

    private fun Any?.asStringMap(
        context: String
    ): Map<String, Any?> {
        val source = this as? Map<*, *> ?: error("Expected YAML mapping for $context")
        return source.entries.associate { (key, value) ->
            val stringKey = key as? String ?: error("Expected string key in $context")
            stringKey to value
        }
    }

    private fun Map<String, Any?>.requiredMap(
        key: String
    ): Map<String, Any?> =
        get(key).asStringMap(key)

    private fun Map<String, Any?>.requiredList(
        key: String
    ): List<Any?> =
        get(key) as? List<*> ?: error("Expected YAML list '$key'")

    private fun Map<String, Any?>.requiredString(
        key: String
    ): String =
        get(key) as? String ?: error("Expected YAML string '$key'")

    private fun Map<String, Any?>.optionalString(
        key: String
    ): String? =
        get(key)?.let { value -> value as? String ?: error("Expected YAML string '$key'") }

    private fun Map<String, Any?>.requiredInt(
        key: String
    ): Int =
        (get(key) as? Number)?.toInt() ?: error("Expected YAML integer '$key'")

    private fun Map<String, Any?>.optionalStringList(
        key: String
    ): List<String> =
        when (val value = get(key)) {
            null -> emptyList()
            is List<*> -> value.map { item ->
                item as? String ?: error("Expected string value in YAML list '$key'")
            }

            else -> error("Expected YAML list '$key'")
        }
}
