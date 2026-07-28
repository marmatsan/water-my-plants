package com.marmatsan.verificationPlatform.domain.model.teamcity

/** Expected failure that prevents one requested TeamCity run from being queued. */
sealed interface QueueTeamCityRunError {
    /** The requested build configuration identifier violates the domain allow-list. */
    data object UnsupportedBuildType : QueueTeamCityRunError

    /** The requested branch violates the domain allow-list. */
    data object UnsupportedBranch : QueueTeamCityRunError

    /**
     * TeamCity rejected an otherwise valid queue request.
     *
     * @property statusCode HTTP status returned by TeamCity.
     * @property responseBody bounded response detail safe to expose to the Gradle boundary.
     */
    data class RequestRejected(
        val statusCode: Int,
        val responseBody: String,
    ) : QueueTeamCityRunError

    /** TeamCity returned a successful HTTP response that did not satisfy the response contract. */
    data object InvalidResponse : QueueTeamCityRunError

    /**
     * The trusted TeamCity endpoint could not be reached.
     *
     * @property detail non-sensitive transport detail supplied by the adapter.
     */
    data class Unavailable(
        val detail: String,
    ) : QueueTeamCityRunError
}
