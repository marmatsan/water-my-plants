package com.marmatsan.figmaDesignSync.domain.model.ci

/**
 * Directed external connection between two [CiNode] values.
 */
data class CiConnection(
    val id: String,
    val sourceNodeId: String,
    val targetNodeId: String,
    val label: String,
    val description: String,
    val protocol: String?,
    val authentication: List<String>,
    val policy: String?,
    val path: String?,
    val automation: Automation,
    val annotation: String?
) {
    enum class Automation(val serializedName: String) {
        Manual("manual"),
        Automatic("automatic"),
        OperatorAssisted("operator-assisted")
    }
}
