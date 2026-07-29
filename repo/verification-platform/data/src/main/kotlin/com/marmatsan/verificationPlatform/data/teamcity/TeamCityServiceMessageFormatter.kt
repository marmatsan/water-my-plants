package com.marmatsan.verificationPlatform.data.teamcity

/** Formats TeamCity service messages without exposing arbitrary commands. */
class TeamCityServiceMessageFormatter {
    /**
     * Produces one escaped `setParameter` message per entry in [parameters].
     *
     * Parameters are sorted by name to keep logs deterministic. TeamCity
     * control characters are escaped according to the service-message format.
     */
    fun setParameters(
        parameters: Map<String, String>
    ): List<String> =
        parameters.toSortedMap().map { (name, value) ->
            "##teamcity[setParameter name='${escape(
                value = name
            )}' value='${escape(
                value = value
            )}']"
        }

    private fun escape(
        value: String
    ): String =
        value
            .replace(
                "|",
                "||"
            ).replace(
                "'",
                "|'"
            ).replace(
                "\n",
                "|n"
            ).replace(
                "\r",
                "|r"
            ).replace(
                "[",
                "|["
            ).replace(
                "]",
                "|]"
            )
}
