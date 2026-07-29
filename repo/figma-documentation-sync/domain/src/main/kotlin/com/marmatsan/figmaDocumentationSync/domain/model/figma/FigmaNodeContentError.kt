package com.marmatsan.figmaDocumentationSync.domain.model.figma

/** Expected failure while reading the narrow Figma node content contract. */
sealed interface FigmaNodeContentError {
    /**
     * Figma rejected the node-content request.
     *
     * @property statusCode HTTP status returned by Figma.
     * @property responseBody bounded response detail.
     */
    data class RequestRejected(
        val statusCode: Int,
        val responseBody: String
    ) : FigmaNodeContentError

    /**
     * Figma did not complete the request within the configured timeout.
     *
     * @property timeoutMillis configured request timeout.
     */
    data class TimedOut(
        val timeoutMillis: Long
    ) : FigmaNodeContentError

    /**
     * The requested node was absent from a successful Figma response.
     *
     * @property nodeId requested Figma node identity.
     */
    data class NotFound(
        val nodeId: String
    ) : FigmaNodeContentError

    /** Figma returned a successful response that could not satisfy the content contract. */
    data object InvalidResponse : FigmaNodeContentError

    /**
     * The Figma endpoint could not be reached.
     *
     * @property detail non-sensitive transport detail.
     */
    data class Unavailable(
        val detail: String
    ) : FigmaNodeContentError
}
