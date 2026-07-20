package com.marmatsan.figmaDocumentationSync.projectConfig

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class EnvironmentTeamCityAutomationCredentialsProviderTest : FunSpec(
    {
    test("uses an existing short-lived Cloudflare token without exchanging client credentials") {
        val values = mapOf(
            "TEAMCITY_TOKEN" to "teamcity-token",
            "TEAMCITY_HEADER_CF_ACCESS_TOKEN" to "cloudflare-token"
        )
        val provider = EnvironmentTeamCityAutomationCredentialsProvider(
            environment = values::get,
            cloudflareAccessTokenProvider = CloudflareAccessTokenProvider { _, _, _, _ ->
                error("Cloudflare exchange must not run")
            }
        )

        provider.load("https://teamcity.example/") shouldBe TeamCityAutomationCredentials(
            serverUrl = "https://teamcity.example",
            teamCityToken = "teamcity-token",
            cloudflareAccessToken = "cloudflare-token"
        )
    }

    test("exchanges service credentials when no short-lived token exists") {
        val values = mapOf(
            "TEAMCITY_TOKEN" to "teamcity-token",
            "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID" to "client-id",
            "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET" to "client-secret"
        )
        var exchangeArguments: List<String>? = null
        val provider = EnvironmentTeamCityAutomationCredentialsProvider(
            environment = values::get,
            cloudflareAccessTokenProvider = CloudflareAccessTokenProvider { url, token, id, secret ->
                exchangeArguments = listOf(
                    url,
                    token,
                    id,
                    secret
                )
                "exchanged-token"
            }
        )

        provider.load("https://teamcity.example") shouldBe TeamCityAutomationCredentials(
            serverUrl = "https://teamcity.example",
            teamCityToken = "teamcity-token",
            cloudflareAccessToken = "exchanged-token"
        )
        exchangeArguments shouldBe listOf(
            "https://teamcity.example",
            "teamcity-token",
            "client-id",
            "client-secret"
        )
    }

    test("rejects a non HTTPS TeamCity endpoint before reading secrets") {
        val exception = shouldThrow<IllegalArgumentException> {
            EnvironmentTeamCityAutomationCredentialsProvider(
                environment = { null }
            )
                .load("http://teamcity.example")
        }

        exception.message shouldBe "The public TeamCity automation endpoint must use HTTPS."
    }
}
)
