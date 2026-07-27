package com.marmatsan.figmaDocumentationSync.domain.model.ci

/**
 * Effective CI pipeline trigger supplied by a concrete adapter.
 *
 * @property type trigger family.
 * @property branchFilter optional branch-selection expression.
 * @property dependencyPipelineId upstream pipeline required by a finish trigger.
 * @property afterSuccessfulBuildOnly whether only successful upstream builds trigger execution.
 */
data class CiTrigger(
    val type: Type,
    val branchFilter: String?,
    val dependencyPipelineId: String?,
    val afterSuccessfulBuildOnly: Boolean?,
) {
    /**
     * Adapter-independent trigger family.
     *
     * @property serializedName stable value used by the design model.
     */
    enum class Type(
        val serializedName: String,
    ) {
        Vcs("vcs"),
        PipelineFinish("pipeline finish"),
        Schedule("schedule"),
    }
}
