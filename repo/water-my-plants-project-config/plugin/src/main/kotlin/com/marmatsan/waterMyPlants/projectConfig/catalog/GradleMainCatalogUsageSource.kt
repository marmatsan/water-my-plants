package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleMainCatalogUsageReader
import java.io.File

/** Reads direct product-module catalog usage through the reusable Gradle source reader. */
internal class GradleMainCatalogUsageSource(
    private val reader: GradleMainCatalogUsageReader
) : MainCatalogUsageSource {
    /** Reads direct library usages from product Gradle sources below [rootDir]. */
    override fun libraryUsages(
        rootDir: File
    ): MainLibraryUsages {
        val usages =
            reader.readLibraryUsages(
                rootDir = rootDir
            )
        return MainLibraryUsages(
            coordinates = usages.coordinates,
            bundles = usages.bundles,
            aliases = usages.aliases
        )
    }

    /** Reads directly applied plugin ids from product Gradle sources below [rootDir]. */
    override fun pluginUsages(
        rootDir: File
    ): Map<String, Set<String>> =
        reader.readPluginUsages(
            rootDir = rootDir
        )
}
