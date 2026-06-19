package com.marmatsan.figmaCatalogChecks.domain.port.modules

data class ProjectModuleDependenciesSource(
    val rootDirPath: String,
    val scope: ProjectModuleDependenciesScope
)
