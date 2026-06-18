package com.marmatsan.figmaCatalogChecks.domain.port.modules

import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference

data class FigmaModuleComponentSource(
    val component: FigmaNodeReference,
    val token: String
)
