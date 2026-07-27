package com.marmatsan.waterMyPlants.projectConfig

/** Environment adapter that keeps secret-store choice outside the portable module. */
class EnvironmentTeamCityAutomationCredentialsProvider(
    private val environment: (String) -> String? = System::getenv,
    private val cloudflareAccessTokenProvider: CloudflareAccessTokenProvider =
        CloudflareHttpAccessTokenProvider(),
) : TeamCityAutomationCredentialsProvider {
    /** Loads TeamCity credentials and obtains a short-lived Access token for [serverUrl]. */
    override fun load(
        serverUrl: String,
    ): TeamCityAutomationCredentials {
        require(
            serverUrl.startsWith(
                "https://",
                ignoreCase = true,
            ),
        ) {
            "The public TeamCity automation endpoint must use HTTPS."
        }
        val teamCityToken =
            required(
                name = "TEAMCITY_TOKEN",
            )
        val cloudflareAccessToken =
            environment("TEAMCITY_HEADER_CF_ACCESS_TOKEN")
                ?.takeUnless(String::isBlank)
                ?: cloudflareAccessTokenProvider.exchange(
                    serverUrl = serverUrl,
                    teamCityToken = teamCityToken,
                    clientId =
                        required(
                            name = "TEAMCITY_HEADER_CF_ACCESS_CLIENT_ID",
                        ),
                    clientSecret =
                        required(
                            name = "TEAMCITY_HEADER_CF_ACCESS_CLIENT_SECRET",
                        ),
                )
        return TeamCityAutomationCredentials(
            serverUrl = serverUrl.trimEnd('/'),
            teamCityToken = teamCityToken,
            cloudflareAccessToken = cloudflareAccessToken,
        )
    }

    private fun required(
        name: String,
    ): String =
        environment(name)?.takeUnless(String::isBlank)
            ?: throw IllegalArgumentException("Required environment variable '$name' is not set.")
}
