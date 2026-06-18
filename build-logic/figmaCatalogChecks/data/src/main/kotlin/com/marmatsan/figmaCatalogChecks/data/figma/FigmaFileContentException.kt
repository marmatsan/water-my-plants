package com.marmatsan.figmaCatalogChecks.data.figma

import com.marmatsan.figmaCatalogChecks.domain.model.*

class FigmaFileContentException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)
