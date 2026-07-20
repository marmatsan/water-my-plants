package com.marmatsan.figmaDocumentationSync.projectConfig

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.net.URI

internal class CloudflareHttpAccessTokenProviderTest :
    FunSpec(
        {
            test("extracts the short-lived token from the Cloudflare authorization cookie") {
                var requestedUri: URI? = null
                var requestedHeaders: Map<String, String>? = null
                val provider =
                    CloudflareHttpAccessTokenProvider { uri, headers ->
                        requestedUri = uri
                        requestedHeaders = headers
                        CloudflareAccessResponse(
                            statusCode = 200,
                            setCookieHeaders =
                                listOf(
                                    "CF_Authorization=access-jwt; Path=/; Secure; HttpOnly",
                                ),
                        )
                    }

                provider.exchange(
                    serverUrl = "https://teamcity.example/",
                    teamCityToken = "teamcity-token",
                    clientId = "client-id",
                    clientSecret = "client-secret",
                ) shouldBe "access-jwt"
                requestedUri shouldBe URI.create("https://teamcity.example/app/rest/server")
                requestedHeaders shouldBe
                    mapOf(
                        "Accept" to "application/json",
                        "Authorization" to "Bearer teamcity-token",
                        "CF-Access-Client-Id" to "client-id",
                        "CF-Access-Client-Secret" to "client-secret",
                    )
            }

            test("rejects a response without a Cloudflare authorization cookie") {
                val provider =
                    CloudflareHttpAccessTokenProvider { _, _ ->
                        CloudflareAccessResponse(
                            statusCode = 200,
                            setCookieHeaders = emptyList(),
                        )
                    }

                val exception =
                    shouldThrow<IllegalArgumentException> {
                        provider.exchange(
                            serverUrl = "https://teamcity.example",
                            teamCityToken = "teamcity-token",
                            clientId = "client-id",
                            clientSecret = "client-secret",
                        )
                    }

                exception.message shouldBe "Cloudflare Access did not return a CF_Authorization token."
            }
        },
    )
