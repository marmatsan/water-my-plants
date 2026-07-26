package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginConfigurationUsage
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogEntry.ConventionPluginUsage
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogNode
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import java.io.File
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode as SourceLibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode as SourcePluginCatalogNode

/** Maps the public dependency-catalog model into Figma-owned domain trees. */
internal interface CatalogTreeMapper {
    fun libraryTree(
        roots: List<SourceLibraryCatalogNode>,
    ): LibraryCatalogTree

    fun pluginTree(
        roots: List<SourcePluginCatalogNode>,
    ): PluginCatalogTree
}

/** Supplies catalog usage found in the consuming repository's production modules. */
internal interface MainCatalogUsageSource {
    fun libraryUsages(
        rootDir: File,
    ): MainLibraryUsages

    fun pluginUsages(
        rootDir: File,
    ): Map<String, Set<String>>
}

/** Supplies catalog usage contributed through convention-plugin included builds. */
internal interface ConventionPluginCatalogUsageSource {
    fun libraryUsages(
        rootDir: File,
        includedBuilds: List<IncludedBuildSource>,
    ): ConventionPluginLibraryUsages

    fun pluginUsages(
        rootDir: File,
        includedBuilds: List<IncludedBuildSource>,
    ): Map<String, List<PluginCatalogNode.ConventionPluginUsage>>
}

/** Enriches a library tree without reading repository state. */
internal interface LibraryCatalogUsageEnricher {
    fun enrich(
        tree: LibraryCatalogTree,
        mainUsages: MainLibraryUsages,
        conventionPluginUsages: ConventionPluginLibraryUsages,
    ): LibraryCatalogTree
}

/** Enriches a plugin tree without reading repository state. */
internal interface PluginCatalogUsageEnricher {
    fun enrich(
        tree: PluginCatalogTree,
        mainUsages: Map<String, Set<String>>,
        conventionPluginUsages: Map<String, List<PluginCatalogNode.ConventionPluginUsage>>,
    ): PluginCatalogTree
}

internal data class MainLibraryUsages(
    val coordinates: Map<String, Set<String>> = emptyMap(),
    val bundles: Map<String, Set<String>> = emptyMap(),
    val aliases: Map<String, Set<String>> = emptyMap(),
)

internal data class ConventionPluginLibraryUsages(
    val coordinates: Map<String, List<ConventionPluginUsage>> = emptyMap(),
    val bundles: Map<String, List<ConventionPluginUsage>> = emptyMap(),
    val configuredCoordinates: Map<String, List<ConventionPluginConfigurationUsage>> = emptyMap(),
)
