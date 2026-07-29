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
    /** Reads and validates the external topology YAML [file]. */
    fun read(
        file: File
    ): CiExternalTopology {
        val settings =
            LoadSettings
                .builder()
                .setLabel(file.path)
                .build()
        val root =
            file
                .inputStream()
                .use { input ->
                    Load(settings).loadFromInputStream(input)
                }.asStringMap(
                    context = "root"
                )

        val validation =
            root.requiredMap(
                key = "validation"
            )

        return CiExternalTopology(
            schemaVersion = root.requiredInt("schemaVersion"),
            validation =
                CiExternalTopology.Validation(
                    lastValidatedOn =
                        LocalDate.parse(
                            validation.requiredString("lastValidatedOn")
                        ),
                    warnAfterDays = validation.requiredInt("warnAfterDays")
                ),
            nodes =
                root
                    .requiredList(
                        key = "nodes"
                    ).map(
                        transform = ::readNode
                    ),
            connections =
                root
                    .requiredList(
                        key = "connections"
                    ).map(
                        transform = ::readConnection
                    )
        )
    }

    private fun readNode(
        value: Any?
    ): CiNode {
        val node =
            value.asStringMap(
                context = "node"
            )
        val serializedType = node.requiredString("type")
        val type =
            CiNode.Type.entries.singleOrNull { candidate ->
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
        val connection =
            value.asStringMap(
                context = "connection"
            )
        val serializedAutomation = connection.requiredString("automation")
        val automation =
            CiConnection.Automation.entries.singleOrNull { candidate ->
                candidate.serializedName == serializedAutomation
            } ?: error("Unsupported CI connection automation '$serializedAutomation'")

        return CiConnection(
            id = connection.requiredString("id"),
            sourceNodeId = connection.requiredString("source"),
            targetNodeId = connection.requiredString("target"),
            label = connection.requiredString("label"),
            description = connection.requiredString("description"),
            protocol = connection.optionalString("protocol"),
            authentication =
                connection.optionalStringList(
                    key = "authentication"
                ),
            policy = connection.optionalString("policy"),
            path = connection.optionalString("path"),
            automation = automation,
            annotation = connection.optionalString("annotation")
        )
    }
}
