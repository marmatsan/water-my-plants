package com.marmatsan.figmaDesignSync.domain.model.ci

/**
 * Effective TeamCity pipeline assembled from generated YAML and XML.
 */
data class TeamCityPipeline(
    val id: String,
    val name: String,
    val triggers: List<TeamCityTrigger>,
    val jobs: List<TeamCityJob>
)
