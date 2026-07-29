package com.marmatsan.waterMyPlants.projectConfig.catalog

import java.io.File

/** Locates the Water My Plants product version registry from a supported consumer root. */
internal object WaterMyPlantsVersionsFile {
    /**
     * Returns the first supported `versions.properties` below [rootDir].
     *
     * Repository composition uses the nested project-config path. A staged standalone consumer
     * owns the same file at its root.
     */
    fun resolve(
        rootDir: File
    ): File {
        val candidates =
            listOf(
                rootDir.resolve(
                    relative = "repo/water-my-plants-project-config/versions.properties"
                ),
                rootDir.resolve(
                    relative = "versions.properties"
                )
            )
        return candidates.firstOrNull(File::isFile)
            ?: error("versions.properties not found in repo/water-my-plants-project-config or root directory")
    }
}
