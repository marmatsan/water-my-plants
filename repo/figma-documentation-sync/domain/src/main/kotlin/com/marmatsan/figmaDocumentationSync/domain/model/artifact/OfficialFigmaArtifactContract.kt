package com.marmatsan.figmaDocumentationSync.domain.model.artifact

/** Typed identity contract shared by the official Figma artifact set. */
data class OfficialFigmaArtifactContract(
    val model: Model,
    val scope: Scope,
    val plan: Plan,
    val manifests: List<Manifest>
) {
    data class Model(
        val branch: String,
        val gitSha: String,
        val modelHash: String
    )

    data class Scope(
        val scope: String,
        val gitSha: String,
        val modelHash: String,
        val writerHash: String,
        val transportHash: String,
        val visualRunnerManifestHash: String,
        val metadataRunnerManifestHash: String,
        val visualSyncDecision: String
    )

    data class Plan(
        val decision: String,
        val manifestHash: String,
        val identity: Identity
    )

    data class Identity(
        val modelHash: String,
        val writerHash: String,
        val transportHash: String
    )

    data class Manifest(
        val mode: String,
        val gitSha: String,
        val modelHash: String,
        val manifestHash: String,
        val writerHash: String,
        val transportHash: String,
        val fullVisualSync: Boolean,
        val writeMetadata: Boolean
    )

    enum class Decision(
        val wireValue: String
    ) {
        NONE("none"),
        PARTIAL("partial"),
        FULL("full");

        companion object {
            fun fromWireValue(
                value: String
            ): Decision? = entries.firstOrNull { it.wireValue == value }
        }
    }
}
