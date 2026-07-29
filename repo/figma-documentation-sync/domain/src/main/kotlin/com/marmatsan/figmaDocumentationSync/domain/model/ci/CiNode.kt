package com.marmatsan.figmaDocumentationSync.domain.model.ci

/**
 * External entity rendered in the CI documentation model.
 *
 * @property id stable topology node identity.
 * @property type semantic node family.
 * @property name human-readable node name.
 * @property description operational responsibility of the node.
 */
data class CiNode(
    val id: String,
    val type: Type,
    val name: String,
    val description: String
) {
    /**
     * Semantic family used to render an external topology node.
     *
     * @property serializedName stable value used by the design model.
     */
    enum class Type(
        val serializedName: String
    ) {
        Actor("actor"),
        System("system"),
        GitReference("git reference"),
        Pipeline("pipeline"),
        Job("job"),
        Artifact("artifact"),
        Check("check"),
        Gate("gate")
    }
}
