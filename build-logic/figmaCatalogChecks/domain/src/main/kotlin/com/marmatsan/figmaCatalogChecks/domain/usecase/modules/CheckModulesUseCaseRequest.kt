package com.marmatsan.figmaCatalogChecks.domain.usecase.modules

import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference

data class CheckModulesUseCaseRequest(
    val page: FigmaNodeReference,
    val moduleComponent: FigmaNodeReference,
    val rootSettingsFilePath: String,
    val buildLogicSettingsFilePath: String,
    val token: String
)
