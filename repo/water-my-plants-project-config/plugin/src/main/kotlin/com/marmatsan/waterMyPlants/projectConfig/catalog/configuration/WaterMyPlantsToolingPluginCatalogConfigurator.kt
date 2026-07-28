package com.marmatsan.waterMyPlants.projectConfig.catalog.configuration

import org.gradle.api.initialization.Settings

/**
 * Registers repository tooling plugins separately from the production plugin catalog.
 *
 * @property versions Product version properties used by the auxiliary catalog.
 */
internal class WaterMyPlantsToolingPluginCatalogConfigurator(
    private val versions: WaterMyPlantsVersionProperties,
) {
    /** Creates the consumer-owned `toolPlugins` catalog used by root verification tooling. */
    fun configure(
        settings: Settings,
    ) {
        settings.dependencyResolutionManagement.versionCatalogs.create("toolPlugins") {
            plugin(
                "com.marmatsan.verificationPlatform",
                "com.marmatsan.verificationPlatform",
            ).version(
                versions.required(
                    key = "verificationPlatformPluginVersion",
                ),
            )
        }
    }
}
