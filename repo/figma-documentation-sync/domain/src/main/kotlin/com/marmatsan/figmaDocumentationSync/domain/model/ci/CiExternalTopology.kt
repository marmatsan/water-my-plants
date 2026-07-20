package com.marmatsan.figmaDocumentationSync.domain.model.ci

import java.time.LocalDate

/**
 * Versioned snapshot of external CI systems and their directed connections.
 */
data class CiExternalTopology(
    val schemaVersion: Int,
    val validation: Validation,
    val nodes: List<CiNode>,
    val connections: List<CiConnection>,
) {
    init {
        require(schemaVersion > 0) { "CI topology schemaVersion must be positive" }
        require(validation.warnAfterDays > 0) { "CI topology warnAfterDays must be positive" }

        val nodeIds =
            nodes.map(
                transform = CiNode::id,
            )
        require(nodeIds.size == nodeIds.toSet().size) { "CI topology node ids must be unique" }

        val connectionIds =
            connections.map(
                transform = CiConnection::id,
            )
        require(connectionIds.size == connectionIds.toSet().size) {
            "CI topology connection ids must be unique"
        }

        connections.forEach { connection ->
            require(connection.sourceNodeId in nodeIds) {
                "Unknown CI connection source '${connection.sourceNodeId}'"
            }
            require(connection.targetNodeId in nodeIds) {
                "Unknown CI connection target '${connection.targetNodeId}'"
            }
        }
    }

    data class Validation(
        val lastValidatedOn: LocalDate,
        val warnAfterDays: Int,
    )
}
