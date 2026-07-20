package com.marmatsan.figmaDocumentationSync.projectConfig

import com.marmatsan.dependencies.WaterMyPlantsCatalog
import com.marmatsan.dependencies.catalog.DependencyCatalogTrees
import com.marmatsan.figmaDocumentationSync.data.dependencies.catalog.DependencyCatalogProvider
import java.io.File

/** Water My Plants adapter for the portable dependency catalog contract. */
class WaterMyPlantsDependencyCatalogProvider : DependencyCatalogProvider {
    override fun resolved(
        rootDir: File,
    ): DependencyCatalogTrees =
        WaterMyPlantsCatalog.resolved(
            rootDir = rootDir,
        )

    override fun withVersionAliases(): DependencyCatalogTrees =
        WaterMyPlantsCatalog.withVersionAliases()
}
