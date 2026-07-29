package com.marmatsan.figmaDocumentationSync.domain.model.ci

/**
 * Directed external connection between two [CiNode] values.
 *
 * @property id stable connection identity.
 * @property sourceNodeId id of the connection origin.
 * @property targetNodeId id of the connection destination.
 * @property label concise relationship label rendered in Figma.
 * @property description operational meaning of the connection.
 * @property protocol optional transport protocol.
 * @property authentication authentication mechanisms used by the connection.
 * @property policy optional governing policy.
 * @property path optional endpoint or repository path.
 * @property automation degree of automation for the connection.
 * @property annotation optional diagram annotation.
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
    /**
     * Operational automation level of an external connection.
     *
     * @property serializedName stable value used by the design model.
     */
    enum class Automation(
        val serializedName: String
    ) {
        Manual("manual"),
        Automatic("automatic"),
        OperatorAssisted("operator-assisted")
    }
}
