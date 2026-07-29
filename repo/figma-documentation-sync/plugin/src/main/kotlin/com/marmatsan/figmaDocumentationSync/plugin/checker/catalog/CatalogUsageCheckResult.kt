package com.marmatsan.figmaDocumentationSync.plugin.checker.catalog

/**
 * Result of checking declared dependency catalogs for entries that are not used.
 *
 * @property unusedEntries deterministically ordered entries without a detected consumer.
 */
internal data class CatalogUsageCheckResult(
    val unusedEntries: List<UnusedCatalogEntry>
) {
    /** Whether every declared catalog entry has at least one repository consumer. */
    val isSuccessful: Boolean = unusedEntries.isEmpty()
}

/**
 * One declared catalog entry that has no module, convention plugin, or tooling
 * usage in the repository.
 *
 * @property catalogName design-model catalog and tree containing the unused entry.
 * @property entry human-readable coordinate or plugin id of the unused entry.
 */
internal data class UnusedCatalogEntry(
    val catalogName: String,
    val entry: String
)
