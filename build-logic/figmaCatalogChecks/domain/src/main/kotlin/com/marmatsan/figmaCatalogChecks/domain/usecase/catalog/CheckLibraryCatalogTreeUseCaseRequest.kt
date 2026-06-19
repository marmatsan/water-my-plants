package com.marmatsan.figmaCatalogChecks.domain.usecase.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.figma.FigmaNodeReference
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.port.usage.ProjectCatalogUsageScope

data class CheckLibraryCatalogTreeUseCaseRequest(
    val page: FigmaNodeReference,
    val section: FigmaNodeReference,
    val projectSource: ProjectCatalogTreeSource,
    val token: String,
    val projectRootPath: String? = null,
    val projectUsageScope: ProjectCatalogUsageScope = ProjectCatalogUsageScope.Main
)
