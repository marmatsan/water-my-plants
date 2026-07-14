package com.marmatsan.figmaDesignSync.domain.model.ci

/**
 * Effective TeamCity configuration used as the CI visual model input.
 */
data class TeamCityConfiguration(
    val pipelines: List<TeamCityPipeline>,
    val vcsRoots: List<TeamCityVcsRoot>
) {
    init {
        require(pipelines.map(TeamCityPipeline::id).let { ids -> ids.size == ids.toSet().size }) {
            "TeamCity pipeline ids must be unique"
        }
        require(vcsRoots.map(TeamCityVcsRoot::id).let { ids -> ids.size == ids.toSet().size }) {
            "TeamCity VCS root ids must be unique"
        }
    }
}
