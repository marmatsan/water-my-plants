package com.marmatsan.waterMyPlants.projectConfig.catalog

import com.marmatsan.dependencies.catalog.api.DependencyCatalog
import com.marmatsan.dependencies.catalog.api.DependencyCatalogProvider
import com.marmatsan.dependencies.catalog.mapping.toDependencyCatalog
import java.io.File

/** Water My Plants implementation of the portable dependency catalog contract. */
class WaterMyPlantsCatalogProvider : DependencyCatalogProvider {
    /** Returns the catalog with versions resolved from [rootDir]. */
    override fun resolved(
        rootDir: File
    ): DependencyCatalog =
        WaterMyPlantsCatalog
            .resolved(
                rootDir = rootDir
            ).toDependencyCatalog()

    /** Returns the catalog with symbolic aliases used to map entries to version properties. */
    override fun withVersionAliases(): DependencyCatalog =
        WaterMyPlantsCatalog.withVersionAliases().toDependencyCatalog()
}
