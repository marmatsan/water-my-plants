package com.marmatsan.figmaDesignSync.projectConfig

/** Short-lived credentials supplied to the TeamCity CLI child process. */
data class TeamCityAutomationCredentials(
    val serverUrl: String,
    val teamCityToken: String,
    val cloudflareAccessToken: String
)
