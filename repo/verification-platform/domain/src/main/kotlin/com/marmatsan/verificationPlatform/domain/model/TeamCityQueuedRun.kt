package com.marmatsan.verificationPlatform.domain.model

/**
 * Minimal result returned after TeamCity accepts a run into its queue.
 *
 * @property id TeamCity numeric build identifier.
 * @property state queue state reported by TeamCity.
 * @property branch logical branch assigned to the queued build.
 * @property webUrl browser URL for the run, or `null` when TeamCity omits it.
 */
data class TeamCityQueuedRun(
    val id: Long,
    val state: String,
    val branch: String,
    val webUrl: String?
)
