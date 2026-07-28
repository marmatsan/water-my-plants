package com.marmatsan.waterMyPlants.projectConfig.catalog.configuration

import java.io.File
import java.util.Properties

/** Repository-owned version properties shared by auxiliary catalog configurators. */
internal class WaterMyPlantsVersionProperties private constructor(
    private val source: File,
    private val properties: Properties,
) {
    /** Returns the required value for [key]. */
    fun required(
        key: String,
    ): String =
        properties.getProperty(key)
            ?: error("Missing $key in ${source.path}")

    /** Loads the Water My Plants version properties visible from a consumer root. */
    companion object {
        /** Resolves and parses the product `versions.properties` from [rootDirectory]. */
        fun load(
            rootDirectory: File,
        ): WaterMyPlantsVersionProperties {
            val source =
                listOf(
                    rootDirectory.resolve("repo/water-my-plants-project-config/versions.properties"),
                    rootDirectory.resolve("versions.properties"),
                ).firstOrNull(File::isFile)
                    ?: error("Water My Plants versions.properties not found from ${rootDirectory.path}")
            val properties =
                Properties().apply {
                    source.inputStream().use(::load)
                }
            return WaterMyPlantsVersionProperties(
                source = source,
                properties = properties,
            )
        }
    }
}
