package com.marmatsan.figmaCatalogChecks.domain.usecase.catalog

import com.marmatsan.figmaCatalogChecks.domain.model.usage.ProjectCatalogUsage
import com.marmatsan.figmaCatalogChecks.domain.port.usage.ProjectCatalogUsagePort
import com.marmatsan.figmaCatalogChecks.domain.port.usage.ProjectCatalogUsageSource

internal class FakeProjectCatalogUsagePort(
    private val projectCatalogUsage: ProjectCatalogUsage = ProjectCatalogUsage(
        libraryKeysByModule = emptyMap(),
        libraryAccessorsByModule = emptyMap(),
        pluginIdsByModule = emptyMap()
    )
) : ProjectCatalogUsagePort {
    override fun readProjectUsage(source: ProjectCatalogUsageSource): ProjectCatalogUsage =
        projectCatalogUsage
}
