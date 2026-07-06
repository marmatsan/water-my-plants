package com.marmatsan.figmaDesignSync.data.figma.client


/**
 * Failure raised when the Figma file API cannot provide the node content needed
 * by the sync checker.
 */
class FigmaFileContentException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
