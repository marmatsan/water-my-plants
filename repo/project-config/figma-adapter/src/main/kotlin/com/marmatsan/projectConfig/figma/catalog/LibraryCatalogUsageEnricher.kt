package com.marmatsan.projectConfig.figma.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree

/** Enriches a library tree without reading repository state. */
internal interface LibraryCatalogUsageEnricher {
    /** Returns [tree] annotated with direct and convention-plugin usage. */
    fun enrich(
        tree: LibraryCatalogTree,
        mainUsages: MainLibraryUsages,
        conventionPluginUsages: ConventionPluginLibraryUsages
    ): LibraryCatalogTree
}
