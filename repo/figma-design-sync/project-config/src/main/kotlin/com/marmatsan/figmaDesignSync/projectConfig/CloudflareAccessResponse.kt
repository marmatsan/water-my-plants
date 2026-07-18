package com.marmatsan.figmaDesignSync.projectConfig

/** Minimal HTTP response needed by the Cloudflare service-auth exchange. */
data class CloudflareAccessResponse(
    val statusCode: Int,
    val setCookieHeaders: List<String>
)
