package com.marmatsan.figmaCatalogChecks.domain.port.modules

import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference

data class FigmaModuleDependenciesSource(
    val section: FigmaNodeReference,
    val token: String
)
