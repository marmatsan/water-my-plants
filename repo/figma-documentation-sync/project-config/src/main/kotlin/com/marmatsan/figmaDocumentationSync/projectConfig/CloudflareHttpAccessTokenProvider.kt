package com.marmatsan.figmaDocumentationSync.projectConfig

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/** JDK HTTP adapter for the Cloudflare service-auth token exchange. */
class CloudflareHttpAccessTokenProvider(
    private val send: (URI, Map<String, String>) -> CloudflareAccessResponse = ::sendRequest
) : CloudflareAccessTokenProvider {
    override fun exchange(
        serverUrl: String,
        teamCityToken: String,
        clientId: String,
        clientSecret: String
    ): String {
        val endpoint = URI.create("${serverUrl.trimEnd('/')}/app/rest/server")
        require(
            endpoint.scheme.equals(
                "https",
                ignoreCase = true
            )
        ) {
            "The public TeamCity automation endpoint must use HTTPS."
        }
        val response = send(
            endpoint,
            mapOf(
                "Accept" to "application/json",
                "Authorization" to "Bearer $teamCityToken",
                "CF-Access-Client-Id" to clientId,
                "CF-Access-Client-Secret" to clientSecret
            )
        )
        require(response.statusCode in 200..299) {
            "Cloudflare Access validation failed with HTTP ${response.statusCode}."
        }
        return response.setCookieHeaders
            .asSequence()
            .mapNotNull { header -> accessCookie.find(header)?.groupValues?.get(
                index = 1
            ) }
            .firstOrNull()
            ?: throw IllegalArgumentException(
                "Cloudflare Access did not return a CF_Authorization token."
            )
    }

    private companion object {
        val accessCookie = Regex(
            pattern = "(?:^|;\\s*)CF_Authorization=([^;]+)",
            option = RegexOption.IGNORE_CASE
        )

        fun sendRequest(
            uri: URI,
            headers: Map<String, String>
        ): CloudflareAccessResponse {
            val requestBuilder = HttpRequest.newBuilder(uri).GET()
            headers.forEach(requestBuilder::header)
            val response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.discarding()
                )
            return CloudflareAccessResponse(
                statusCode = response.statusCode(),
                setCookieHeaders = response.headers().allValues("set-cookie")
            )
        }
    }
}
