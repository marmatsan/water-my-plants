package com.marmatsan.figmaCatalogChecks.domain.model.catalog

sealed interface LibraryCatalogEntry {
    data class Artifact(
        val artifact: String,
        val version: CatalogVersion,
        val requiredByModules: List<String> = emptyList()
    ) : LibraryCatalogEntry

    data class ArtifactsBundle(
        val alias: String,
        val artifacts: List<String>,
        val version: CatalogVersion,
        val requiredByModules: List<String> = emptyList()
    ) : LibraryCatalogEntry
}
