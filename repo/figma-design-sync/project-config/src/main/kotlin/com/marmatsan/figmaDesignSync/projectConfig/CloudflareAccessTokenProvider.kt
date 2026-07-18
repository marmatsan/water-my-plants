package com.marmatsan.figmaDesignSync.projectConfig

/** Exchanges Cloudflare service credentials for a short-lived Access token. */
fun interface CloudflareAccessTokenProvider {
    fun exchange(
        serverUrl: String,
        teamCityToken: String,
        clientId: String,
        clientSecret: String
    ): String
}
