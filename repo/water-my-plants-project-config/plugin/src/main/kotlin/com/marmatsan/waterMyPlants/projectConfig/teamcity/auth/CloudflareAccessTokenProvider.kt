package com.marmatsan.waterMyPlants.projectConfig.teamcity.auth

/** Exchanges Cloudflare service credentials for a short-lived Access token. */
fun interface CloudflareAccessTokenProvider {
    /** Validates service credentials against [serverUrl] and returns a short-lived Access token. */
    fun exchange(
        serverUrl: String,
        teamCityToken: String,
        clientId: String,
        clientSecret: String,
    ): String
}
