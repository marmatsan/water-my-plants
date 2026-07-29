package com.marmatsan.verificationPlatform.domain.model.teamcity

/**
 * Identifies the TeamCity build configuration and branch that must be queued.
 *
 * @property buildTypeId stable TeamCity build configuration identifier.
 * @property branch branch specification sent to the TeamCity queue API.
 */
data class TeamCityRunRequest(
    val buildTypeId: String,
    val branch: String
)
