package com.marmatsan.waterMyPlants.projectConfig

/** Credential port used by the repository-specific TeamCity rerun task. */
fun interface TeamCityAutomationCredentialsProvider {
    fun load(
        serverUrl: String,
    ): TeamCityAutomationCredentials
}
