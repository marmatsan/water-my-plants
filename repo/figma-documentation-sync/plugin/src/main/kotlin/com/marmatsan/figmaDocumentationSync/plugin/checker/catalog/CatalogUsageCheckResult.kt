package com.marmatsan.figmaDocumentationSync.plugin.checker.catalog

/**
 * Result of checking declared dependency catalogs for entries that are not used.
 */
internal data class CatalogUsageCheckResult(
    val unusedEntries: List<UnusedCatalogEntry>,
) {
    val isSuccessful: Boolean = unusedEntries.isEmpty()
}

/**
 * One declared catalog entry that has no module, convention plugin, or tooling
 * usage in the repository.
 */
internal data class UnusedCatalogEntry(
    val catalogName: String,
    val entry: String,
)
