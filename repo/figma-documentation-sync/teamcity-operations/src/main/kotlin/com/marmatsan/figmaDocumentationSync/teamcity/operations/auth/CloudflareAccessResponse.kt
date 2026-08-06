package com.marmatsan.figmaDocumentationSync.teamcity.operations.auth

/**
 * Minimal HTTP response needed by the Cloudflare service-auth exchange.
 *
 * @property statusCode HTTP status returned by the Access-protected endpoint.
 * @property setCookieHeaders response cookies from which the short-lived token is extracted.
 */
data class CloudflareAccessResponse(
    val statusCode: Int,
    val setCookieHeaders: List<String>
)
