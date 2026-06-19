package com.marmatsan.figmaCatalogChecks.domain.model.usage

data class ProjectCatalogUsage(
    val libraryKeysByModule: Map<String, Set<LibraryCatalogUsageKey>>,
    val libraryAccessorsByModule: Map<String, Set<String>>,
    val pluginIdsByModule: Map<String, Set<String>>
)
