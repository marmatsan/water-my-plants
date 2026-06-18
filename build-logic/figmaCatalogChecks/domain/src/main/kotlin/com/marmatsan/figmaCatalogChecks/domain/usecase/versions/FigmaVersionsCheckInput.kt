package com.marmatsan.figmaCatalogChecks.domain.usecase.versions

import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference

data class FigmaVersionsCheckInput(
    val page: FigmaNodeReference,
    val section: FigmaNodeReference,
    val versionComponent: FigmaNodeReference,
    val repositoryVersions: Map<String, String>,
    val figmaVersions: Map<String, String>
)
