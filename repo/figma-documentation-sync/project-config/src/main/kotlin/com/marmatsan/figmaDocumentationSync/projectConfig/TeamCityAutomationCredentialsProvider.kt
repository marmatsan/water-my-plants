package com.marmatsan.figmaDocumentationSync.projectConfig

/** Credential port used by the repository-specific TeamCity rerun task. */
fun interface TeamCityAutomationCredentialsProvider {
    fun load(
        serverUrl: String
    ): TeamCityAutomationCredentials
}
