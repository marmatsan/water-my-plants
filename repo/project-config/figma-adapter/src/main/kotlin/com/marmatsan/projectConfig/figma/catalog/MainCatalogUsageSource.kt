package com.marmatsan.projectConfig.figma.catalog

import java.io.File

/** Supplies catalog usage found in the consuming repository's production modules. */
internal interface MainCatalogUsageSource {
    /** Reads library coordinates, bundles, and aliases used below [rootDir]. */
    fun libraryUsages(
        rootDir: File
    ): MainLibraryUsages

    /** Reads plugin ids and the product modules that apply them below [rootDir]. */
    fun pluginUsages(
        rootDir: File
    ): Map<String, Set<String>>
}
