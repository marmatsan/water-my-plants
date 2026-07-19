package com.marmatsan.ci.data.teamcity

/** Formats TeamCity service messages without exposing arbitrary commands. */
class TeamCityServiceMessageFormatter {
    fun setParameters(parameters: Map<String, String>): List<String> =
        parameters.toSortedMap().map { (name, value) ->
            "##teamcity[setParameter name='${escape(name)}' value='${escape(value)}']"
        }

    private fun escape(value: String): String = value
        .replace("|", "||")
        .replace("'", "|'")
        .replace("\n", "|n")
        .replace("\r", "|r")
        .replace("[", "|[")
        .replace("]", "|]")
}
