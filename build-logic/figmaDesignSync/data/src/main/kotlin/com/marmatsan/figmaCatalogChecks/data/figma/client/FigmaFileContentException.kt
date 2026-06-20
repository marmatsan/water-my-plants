package com.marmatsan.figmaDesignSync.data.figma.client



class FigmaFileContentException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
