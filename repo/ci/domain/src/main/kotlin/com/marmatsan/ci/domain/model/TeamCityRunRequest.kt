package com.marmatsan.ci.domain.model

/** Identifies the TeamCity build configuration and branch that must be queued. */
data class TeamCityRunRequest(
    val buildTypeId: String,
    val branch: String
)
