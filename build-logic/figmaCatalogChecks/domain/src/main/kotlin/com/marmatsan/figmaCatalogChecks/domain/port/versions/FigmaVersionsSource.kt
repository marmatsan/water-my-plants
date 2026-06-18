package com.marmatsan.figmaCatalogChecks.domain.port.versions

import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference

data class FigmaVersionsSource(
    val section: FigmaNodeReference,
    val versionComponent: FigmaNodeReference,
    val token: String
)
