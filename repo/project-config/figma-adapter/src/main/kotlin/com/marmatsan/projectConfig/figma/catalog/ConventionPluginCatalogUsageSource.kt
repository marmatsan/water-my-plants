package com.marmatsan.projectConfig.figma.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import java.io.File

/** Supplies catalog usage contributed through convention-plugin included builds. */
internal interface ConventionPluginCatalogUsageSource {
    /** Reads library usage supplied or configured by [includedBuilds]. */
    fun libraryUsages(
        rootDir: File,
        includedBuilds: List<IncludedBuildSource>
    ): ConventionPluginLibraryUsages

    /** Reads plugin usage supplied by convention plugins from [includedBuilds]. */
    fun pluginUsages(
        rootDir: File,
        includedBuilds: List<IncludedBuildSource>
    ): Map<String, List<PluginCatalogNode.ConventionPluginUsage>>
}
