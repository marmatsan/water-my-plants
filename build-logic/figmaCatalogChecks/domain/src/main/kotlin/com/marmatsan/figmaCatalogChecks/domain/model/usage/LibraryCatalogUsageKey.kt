package com.marmatsan.figmaCatalogChecks.domain.model.usage

sealed interface LibraryCatalogUsageKey {
    data class Artifact(
        val group: String,
        val artifact: String
    ) : LibraryCatalogUsageKey

    data class Bundle(
        val alias: String
    ) : LibraryCatalogUsageKey
}
