package com.marmatsan.figmaCatalogChecks.domain.port.usage

import com.marmatsan.figmaCatalogChecks.domain.model.usage.ProjectCatalogUsage

interface ProjectCatalogUsagePort {
    fun readProjectUsage(source: ProjectCatalogUsageSource): ProjectCatalogUsage
}
