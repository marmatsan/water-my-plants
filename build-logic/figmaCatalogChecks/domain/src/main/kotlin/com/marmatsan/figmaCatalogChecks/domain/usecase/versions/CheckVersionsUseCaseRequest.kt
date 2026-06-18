package com.marmatsan.figmaCatalogChecks.domain.usecase.versions

import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference

data class CheckVersionsUseCaseRequest(
    val page: FigmaNodeReference,
    val section: FigmaNodeReference,
    val versionComponent: FigmaNodeReference,
    val versionsFilePath: String,
    val token: String
)
