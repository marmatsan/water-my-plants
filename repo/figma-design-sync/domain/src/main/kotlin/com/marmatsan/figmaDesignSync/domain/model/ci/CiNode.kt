package com.marmatsan.figmaDesignSync.domain.model.ci

/**
 * External entity rendered in the CI documentation model.
 */
data class CiNode(
    val id: String,
    val type: Type,
    val name: String,
    val description: String
) {
    enum class Type(val serializedName: String) {
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
