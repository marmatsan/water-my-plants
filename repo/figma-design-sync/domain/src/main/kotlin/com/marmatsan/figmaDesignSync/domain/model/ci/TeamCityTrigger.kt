package com.marmatsan.figmaDesignSync.domain.model.ci

/**
 * Effective TeamCity pipeline trigger extracted from generated configuration.
 */
data class TeamCityTrigger(
    val type: Type,
    val branchFilter: String?,
    val dependencyPipelineId: String?,
    val afterSuccessfulBuildOnly: Boolean?
) {
    enum class Type {
        Vcs,
        PipelineFinish
    }
}
