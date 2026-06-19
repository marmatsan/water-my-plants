package com.marmatsan.figmaCatalogChecks.domain.port.usage

data class ProjectCatalogUsageSource(
    val rootDirPath: String,
    val scope: ProjectCatalogUsageScope = ProjectCatalogUsageScope.Main
)
