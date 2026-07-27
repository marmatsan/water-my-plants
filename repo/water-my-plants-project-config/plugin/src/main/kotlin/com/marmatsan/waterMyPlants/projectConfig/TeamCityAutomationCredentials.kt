package com.marmatsan.waterMyPlants.projectConfig

/**
 * Short-lived credentials supplied to the TeamCity CLI and REST adapters.
 *
 * @property serverUrl normalized public TeamCity HTTPS origin.
 * @property teamCityToken bearer token accepted by TeamCity automation.
 * @property cloudflareAccessToken short-lived token accepted by the Access proxy.
 */
data class TeamCityAutomationCredentials(
    val serverUrl: String,
    val teamCityToken: String,
    val cloudflareAccessToken: String,
)
