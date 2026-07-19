package com.marmatsan.figmaDocumentationSync.projectConfig

/** Short-lived credentials supplied to the TeamCity CLI and REST adapters. */
data class TeamCityAutomationCredentials(
    val serverUrl: String,
    val teamCityToken: String,
    val cloudflareAccessToken: String
)
