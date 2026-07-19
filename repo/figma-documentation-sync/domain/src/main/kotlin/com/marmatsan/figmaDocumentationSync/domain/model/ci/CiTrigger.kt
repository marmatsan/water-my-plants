package com.marmatsan.figmaDocumentationSync.domain.model.ci

/** Effective CI pipeline trigger supplied by a concrete adapter. */
data class CiTrigger(
    val type: Type,
    val branchFilter: String?,
    val dependencyPipelineId: String?,
    val afterSuccessfulBuildOnly: Boolean?
) {
    enum class Type(val serializedName: String) {
        Vcs("vcs"),
        PipelineFinish("pipeline finish"),
        Schedule("schedule")
    }
}
