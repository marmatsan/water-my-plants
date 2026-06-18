package com.marmatsan.figmaCatalogChecks.domain.port.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference

data class FigmaCatalogTreeSource(
    val section: FigmaNodeReference,
    val token: String
)
