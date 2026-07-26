package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleCatalogUsageReader
import java.io.File

/** Reads direct product-module catalog usage through the reusable Gradle source reader. */
internal class GradleMainCatalogUsageSource(
    private val reader: GradleCatalogUsageReader,
) : MainCatalogUsageSource {
    override fun libraryUsages(
        rootDir: File,
    ): MainLibraryUsages {
        val usages =
            reader.readMainLibraryUsages(
                rootDir = rootDir,
            )
        return MainLibraryUsages(
            coordinates = usages.coordinates,
            bundles = usages.bundles,
            aliases = usages.aliases,
        )
    }

    override fun pluginUsages(
        rootDir: File,
    ): Map<String, Set<String>> =
        reader.readMainPluginUsages(
            rootDir = rootDir,
        )
}
