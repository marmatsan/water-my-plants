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
    /** Maps library [roots] without reading repository state. */
    fun libraryTree(
        roots: List<SourceLibraryCatalogNode>,
    ): LibraryCatalogTree

    /** Maps plugin [roots] without reading repository state. */
    fun pluginTree(
        roots: List<SourcePluginCatalogNode>,
    ): PluginCatalogTree
}

/** Supplies catalog usage found in the consuming repository's production modules. */
internal interface MainCatalogUsageSource {
    /** Reads library coordinates, bundles, and aliases used below [rootDir]. */
    fun libraryUsages(
        rootDir: File,
    ): MainLibraryUsages

    /** Reads plugin ids and the product modules that apply them below [rootDir]. */
    fun pluginUsages(
        rootDir: File,
    ): Map<String, Set<String>>
}

/** Supplies catalog usage contributed through convention-plugin included builds. */
internal interface ConventionPluginCatalogUsageSource {
    /** Reads library usage supplied or configured by [includedBuilds]. */
    fun libraryUsages(
        rootDir: File,
        includedBuilds: List<IncludedBuildSource>,
    ): ConventionPluginLibraryUsages

    /** Reads plugin usage supplied by convention plugins from [includedBuilds]. */
    fun pluginUsages(
        rootDir: File,
        includedBuilds: List<IncludedBuildSource>,
    ): Map<String, List<PluginCatalogNode.ConventionPluginUsage>>
}

/** Enriches a library tree without reading repository state. */
internal interface LibraryCatalogUsageEnricher {
    /** Returns [tree] annotated with direct and convention-plugin usage. */
    fun enrich(
        tree: LibraryCatalogTree,
        mainUsages: MainLibraryUsages,
        conventionPluginUsages: ConventionPluginLibraryUsages,
    ): LibraryCatalogTree
}

/** Enriches a plugin tree without reading repository state. */
internal interface PluginCatalogUsageEnricher {
    /** Returns [tree] annotated with direct and convention-plugin usage. */
    fun enrich(
        tree: PluginCatalogTree,
        mainUsages: Map<String, Set<String>>,
        conventionPluginUsages: Map<String, List<PluginCatalogNode.ConventionPluginUsage>>,
    ): PluginCatalogTree
}

/**
 * Direct product-module library usage indexed by catalog representation.
 *
 * @property coordinates modules using resolved group-artifact coordinates.
 * @property bundles modules using a catalog bundle alias.
 * @property aliases modules using a single-library catalog alias.
 */
internal data class MainLibraryUsages(
    val coordinates: Map<String, Set<String>> = emptyMap(),
    val bundles: Map<String, Set<String>> = emptyMap(),
    val aliases: Map<String, Set<String>> = emptyMap(),
)

/**
 * Library usage contributed by repository convention plugins.
 *
 * @property coordinates convention plugins providing resolved coordinates.
 * @property bundles convention plugins providing catalog bundles.
 * @property configuredCoordinates convention plugins configuring coordinates as tools.
 */
internal data class ConventionPluginLibraryUsages(
    val coordinates: Map<String, List<ConventionPluginUsage>> = emptyMap(),
    val bundles: Map<String, List<ConventionPluginUsage>> = emptyMap(),
    val configuredCoordinates: Map<String, List<ConventionPluginConfigurationUsage>> = emptyMap(),
)
