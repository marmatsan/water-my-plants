package com.marmatsan.ci.domain.model

/** Minimal result returned after TeamCity accepts a run into its queue. */
data class TeamCityQueuedRun(
    val id: Long,
    val state: String,
    val branch: String,
    val webUrl: String?
)
