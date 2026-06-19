package com.marmatsan.figmaCatalogChecks.domain.comparison.usage

data class CatalogUsageDifference(
    val repositoryModules: List<String>,
    val figmaModules: List<String>
)
