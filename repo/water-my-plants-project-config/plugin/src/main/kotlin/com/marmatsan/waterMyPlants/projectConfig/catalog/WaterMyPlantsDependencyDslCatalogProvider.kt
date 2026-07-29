package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleConventionCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.data.gradle.catalog.GradleMainCatalogUsageReader
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDocumentationSync.domain.port.catalog.DependencyDslCatalogProvider
import com.marmatsan.figmaDocumentationSync.domain.port.gradle.IncludedBuildSource
import java.io.File

/** Water My Plants adapter between Dependency Catalog API and the Figma-owned catalog port. */
class WaterMyPlantsDependencyDslCatalogProvider : DependencyDslCatalogProvider {
    private val reader =
        DependenciesCatalogTreesReader(
            dependencyCatalogProvider = WaterMyPlantsCatalogProvider(),
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

    /** Builds the Water My Plants library tree and enriches it with repository usage. */
    override fun readLibraryTreeWithVersionAliases(
        rootDirPath: String,
        conventionPluginIncludedBuilds: List<IncludedBuildSource>
    ): LibraryCatalogTree =
        reader.readLibraryTreeWithVersionAliases(
            rootDir = File(rootDirPath),
            conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
        )

    /** Builds the Water My Plants plugin tree and enriches it with repository usage. */
    override fun readPluginTreeWithVersionAliases(
        rootDirPath: String,
        conventionPluginIncludedBuilds: List<IncludedBuildSource>
    ): PluginCatalogTree =
        reader.readPluginTreeWithVersionAliases(
            rootDir = File(rootDirPath),
            conventionPluginIncludedBuilds = conventionPluginIncludedBuilds
        )
}
