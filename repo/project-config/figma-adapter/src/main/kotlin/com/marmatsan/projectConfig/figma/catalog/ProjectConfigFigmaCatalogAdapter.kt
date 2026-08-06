package com.marmatsan.projectConfig.figma.catalog

import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleConventionCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleMainCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.DependencyCatalogTrees
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import java.io.File

/** Maps one consumer-owned dependency catalog into Figma trees with repository usage. */
internal class ProjectConfigFigmaCatalogAdapter(
    dependencyCatalog: DependencyCatalog
) {
    private val reader =
        DependenciesCatalogTreesReader(
            dependencyCatalog = dependencyCatalog,
            catalogTreeMapper = DependencyCatalogTreeMapper(),
            mainCatalogUsageSource = GradleMainCatalogUsageSource(GradleMainCatalogUsageReader()),
            conventionPluginCatalogUsageSource =
                GradleConventionPluginCatalogUsageSource(
                    reader = GradleConventionCatalogUsageReader(),
                    mainReader = GradleMainCatalogUsageReader()
                ),
            libraryCatalogUsageEnricher = DefaultLibraryCatalogUsageEnricher(),
            pluginCatalogUsageEnricher = DefaultPluginCatalogUsageEnricher()
        )

    /**
     * Builds both aliased catalog trees and annotates their repository usage.
     *
     * @param rootDirectory Consumer repository root scanned for direct usage.
     * @param conventionPluginIncludedBuilds Included builds scanned for usage
     * contributed by repository convention plugins.
     */
    fun readTrees(
        rootDirectory: File,
        conventionPluginIncludedBuilds: List<IncludedBuildSource> = emptyList()
    ): DependencyCatalogTrees =
        DependencyCatalogTrees(
            libraries =
                readLibraryTree(
                    rootDirectory = rootDirectory,
                    conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
                ),
            plugins =
                readPluginTree(
                    rootDirectory = rootDirectory,
                    conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
                )
        )

    /** Builds the aliased library tree and annotates its repository usage. */
    fun readLibraryTree(
        rootDirectory: File,
        conventionPluginIncludedBuilds: List<IncludedBuildSource> = emptyList()
    ): LibraryCatalogTree =
        reader.readLibraryTreeWithVersionAliases(
            rootDir = rootDirectory,
            conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
        )

    /** Builds the aliased plugin tree and annotates its repository usage. */
    fun readPluginTree(
        rootDirectory: File,
        conventionPluginIncludedBuilds: List<IncludedBuildSource> = emptyList()
    ): PluginCatalogTree =
        reader.readPluginTreeWithVersionAliases(
            rootDir = rootDirectory,
            conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
        )
}
