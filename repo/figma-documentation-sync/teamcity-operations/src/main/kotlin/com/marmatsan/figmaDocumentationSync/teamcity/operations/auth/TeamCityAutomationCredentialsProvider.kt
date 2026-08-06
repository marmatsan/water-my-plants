package com.marmatsan.figmaDocumentationSync.teamcity.operations.auth

/** Credential port used by the repository-specific TeamCity rerun task. */
fun interface TeamCityAutomationCredentialsProvider {
    /** Loads the credentials required to automate the TeamCity origin at [serverUrl]. */
    fun load(
        serverUrl: String
    ): TeamCityAutomationCredentials
}
