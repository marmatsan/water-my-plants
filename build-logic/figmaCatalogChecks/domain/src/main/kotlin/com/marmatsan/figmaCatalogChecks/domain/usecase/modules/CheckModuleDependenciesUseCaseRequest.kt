package com.marmatsan.figmaCatalogChecks.domain.usecase.modules

import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModuleDependenciesScope

data class CheckModuleDependenciesUseCaseRequest(
    val page: FigmaNodeReference,
    val section: FigmaNodeReference,
    val rootDirPath: String,
    val scope: ProjectModuleDependenciesScope,
    val token: String
)
