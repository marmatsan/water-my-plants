package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.dependencies.catalog.version.DependencyVersionAliasResolver
import com.marmatsan.dependencies.catalog.version.DependencyVersionResolver
import com.marmatsan.dependencies.catalog.version.PropertiesDependencyVersionResolver
import java.io.File

/** Builds the Water My Plants dependency catalog from resolved or symbolic versions. */
object WaterMyPlantsCatalog {
    /** Resolves version values from the repository rooted at [rootDir]. */
    fun resolved(
        rootDir: File
    ): DependencyCatalogTrees =
        catalogTrees(
            versionResolver =
                PropertiesDependencyVersionResolver(
                    source =
                        WaterMyPlantsVersionsFile.resolve(
                            rootDir = rootDir
                        )
                )
        )

    /** Builds the same catalog with property names as version aliases for documentation scanning. */
    fun withVersionAliases(): DependencyCatalogTrees =
        catalogTrees(
            versionResolver = DependencyVersionAliasResolver
        )

    private fun catalogTrees(
        versionResolver: DependencyVersionResolver
    ): DependencyCatalogTrees =
        waterMyPlantsCatalogTrees(
            versionResolver = versionResolver
        )
}
