package com.marmatsan.figmaDocumentationSync.teamcityAdapter

/** Expected provider failure that prevents a TeamCity run from being queued. */
sealed interface TeamCityRunStartError {
    /**
     * The TeamCity REST endpoint rejected the queue request.
     *
     * @property statusCode HTTP status returned by TeamCity.
     * @property responseBody bounded response detail.
     */
    data class RequestRejected(
        val statusCode: Int,
        val responseBody: String,
    ) : TeamCityRunStartError

    /**
     * The TeamCity CLI command failed before returning a run.
     *
     * @property exitCode process exit code.
     * @property detail bounded process error detail.
     */
    data class CommandFailed(
        val exitCode: Int,
        val detail: String,
    ) : TeamCityRunStartError

    /** TeamCity reported success but returned a response outside the public contract. */
    data object InvalidResponse : TeamCityRunStartError

    /**
     * The configured TeamCity transport could not be reached or started.
     *
     * @property detail non-sensitive transport detail.
     */
    data class Unavailable(
        val detail: String,
    ) : TeamCityRunStartError
}
