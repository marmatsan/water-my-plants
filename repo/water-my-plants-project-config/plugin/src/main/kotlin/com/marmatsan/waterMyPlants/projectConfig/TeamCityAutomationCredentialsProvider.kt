package com.marmatsan.waterMyPlants.projectConfig

/** Credential port used by the repository-specific TeamCity rerun task. */
fun interface TeamCityAutomationCredentialsProvider {
    /** Loads the credentials required to automate the TeamCity origin at [serverUrl]. */
    fun load(
        serverUrl: String,
    ): TeamCityAutomationCredentials
}
