package com.marmatsan.waterMyPlants.projectConfig.catalog.version

import com.marmatsan.dependencies.catalog.version.DependencyVersionResolver
import com.marmatsan.dependencies.catalog.version.PropertiesDependencyVersionResolver
import java.io.File

/** Water My Plants product versions shared by catalog and Settings composition adapters. */
class WaterMyPlantsVersionProperties private constructor(
    private val versionResolver: DependencyVersionResolver
) {
    /**
     * Returns the required value for the exact, case-sensitive [key].
     *
     * @throws IllegalStateException when [key] is absent from the product registry.
     */
    fun required(
        key: String
    ): String = versionResolver.resolve(key)

    /** Loads the product version registry visible from a supported consumer root. */
    companion object {
        /**
         * Locates and lazily reads the product `versions.properties` below [rootDirectory].
         *
         * Repository composition uses the nested project-config path. A staged standalone
         * consumer owns the same file at its root.
         *
         * @throws IllegalStateException when neither supported source file exists.
         */
        fun load(
            rootDirectory: File
        ): WaterMyPlantsVersionProperties =
            WaterMyPlantsVersionProperties(
                versionResolver =
                    PropertiesDependencyVersionResolver(
                        source =
                            locate(
                                rootDirectory = rootDirectory
                            )
                    )
            )

        private fun locate(
            rootDirectory: File
        ): File =
            listOf(
                rootDirectory.resolve("repo/water-my-plants-project-config/versions.properties"),
                rootDirectory.resolve("versions.properties")
            ).firstOrNull(File::isFile)
                ?: error("Water My Plants versions.properties not found from ${rootDirectory.path}")
    }
}
