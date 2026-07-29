package com.marmatsan.figmaDocumentationSync.domain.model.artifact

/**
 * Typed identity contract shared by the canonical Figma artifact set.
 *
 * @property model canonical design-model identity.
 * @property scope canonical generation and verification scope identity.
 * @property plan canonical visual synchronization plan identity.
 * @property manifests executable runner manifest identities.
 */
data class CanonicalFigmaArtifactContract(
    val model: Model,
    val scope: Scope,
    val plan: Plan,
    val manifests: List<Manifest>
) {
    /**
     * Canonical design-model identity.
     *
     * @property branch branch represented by the model.
     * @property gitSha Git revision represented by the model.
     * @property modelHash canonical model hash.
     */
    data class Model(
        val branch: String,
        val gitSha: String,
        val modelHash: String
    )

    /**
     * Canonical generation scope and writer identity.
     *
     * @property scope verification scope wire value.
     * @property gitSha canonical Git revision.
     * @property modelHash canonical design-model hash.
     * @property writerHash writer behavior hash.
     * @property transportHash MCP transport behavior hash.
     * @property visualRunnerManifestHash visual runner manifest hash.
     * @property metadataRunnerManifestHash metadata runner manifest hash.
     * @property visualSyncDecision selected visual synchronization decision.
     */
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

    /**
     * Canonical visual plan identity.
     *
     * @property decision visual synchronization decision wire value.
     * @property manifestHash executable runner manifest hash.
     * @property identity model, writer, and transport identity evaluated by the plan.
     */
    data class Plan(
        val decision: String,
        val manifestHash: String,
        val identity: Identity
    )

    /**
     * Model, writer, and transport identity shared by canonical artifacts.
     *
     * @property modelHash canonical design-model hash.
     * @property writerHash writer behavior hash.
     * @property transportHash MCP transport behavior hash.
     */
    data class Identity(
        val modelHash: String,
        val writerHash: String,
        val transportHash: String
    )

    /**
     * Identity and write permissions declared by one runner manifest.
     *
     * @property mode runner execution mode.
     * @property gitSha canonical Git revision.
     * @property modelHash canonical design-model hash.
     * @property manifestHash complete runner manifest hash.
     * @property writerHash writer behavior hash.
     * @property transportHash MCP transport behavior hash.
     * @property fullVisualSync whether the runner performs the complete visual write.
     * @property writeMetadata whether the runner writes canonical metadata.
     */
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

    /**
     * Supported canonical visual synchronization decision.
     *
     * @property wireValue stable serialized decision value.
     */
    enum class Decision(
        val wireValue: String
    ) {
        NONE("none"),
        PARTIAL("partial"),
        FULL("full")
        ;

        /** Converts serialized decision values at the artifact boundary. */
        companion object {
            /** Returns the decision represented by [value], or `null` for an unsupported value. */
            fun fromWireValue(
                value: String
            ): Decision? = entries.firstOrNull { it.wireValue == value }
        }
    }
}
