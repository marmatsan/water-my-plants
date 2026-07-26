package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.dependencies.catalog.api.VersionAliasedDependencyCatalogProvider
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import java.io.File
import com.marmatsan.dependencies.catalog.api.LibraryCatalogNode as SourceLibraryCatalogNode
import com.marmatsan.dependencies.catalog.api.PluginCatalogNode as SourcePluginCatalogNode

/** Orchestrates catalog mapping and usage enrichment through consumer-specific ports. */
class DependenciesCatalogTreesReader internal constructor(
    private val dependencyCatalogProvider: VersionAliasedDependencyCatalogProvider,
    private val catalogTreeMapper: CatalogTreeMapper,
    private val mainCatalogUsageSource: MainCatalogUsageSource,
    private val conventionPluginCatalogUsageSource: ConventionPluginCatalogUsageSource,
    private val libraryCatalogUsageEnricher: LibraryCatalogUsageEnricher,
    private val pluginCatalogUsageEnricher: PluginCatalogUsageEnricher,
) {
    /** Reads an aliased library tree enriched with direct and convention-plugin usage. */
    fun readLibraryTreeWithVersionAliases(
        rootDir: File,
        conventionPluginIncludedBuilds: List<IncludedBuildSource> = emptyList(),
    ): LibraryCatalogTree =
        libraryCatalogUsageEnricher.enrich(
            tree =
                readLibraryTree(
                    roots = dependencyCatalogProvider.withVersionAliases().libraries,
                ),
            mainUsages = mainCatalogUsageSource.libraryUsages(rootDir),
            conventionPluginUsages =
                conventionPluginCatalogUsageSource.libraryUsages(
                    rootDir = rootDir,
                    includedBuilds = conventionPluginIncludedBuilds,
                ),
        )

    /** Reads an aliased plugin tree enriched with direct and convention-plugin usage. */
    fun readPluginTreeWithVersionAliases(
        rootDir: File,
        conventionPluginIncludedBuilds: List<IncludedBuildSource> = emptyList(),
    ): PluginCatalogTree =
        pluginCatalogUsageEnricher.enrich(
            tree =
                readPluginTree(
                    roots = dependencyCatalogProvider.withVersionAliases().plugins,
                ),
            mainUsages = mainCatalogUsageSource.pluginUsages(rootDir),
            conventionPluginUsages =
                conventionPluginCatalogUsageSource.pluginUsages(
                    rootDir = rootDir,
                    includedBuilds = conventionPluginIncludedBuilds,
                ),
        )

    internal fun readLibraryTree(
        roots: List<SourceLibraryCatalogNode>,
    ): LibraryCatalogTree = catalogTreeMapper.libraryTree(roots)

    internal fun readPluginTree(
        roots: List<SourcePluginCatalogNode>,
    ): PluginCatalogTree = catalogTreeMapper.pluginTree(roots)
}
