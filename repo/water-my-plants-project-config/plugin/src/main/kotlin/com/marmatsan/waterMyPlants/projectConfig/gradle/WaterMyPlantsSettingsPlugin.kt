package com.marmatsan.waterMyPlants.projectConfig.gradle

import com.marmatsan.dependencies.gradle.DependencyCatalogSettingsExtension
import com.marmatsan.dependencies.gradle.DependencyCatalogSettingsPlugin
import com.marmatsan.waterMyPlants.projectConfig.catalog.WaterMyPlantsCatalogProvider
import com.marmatsan.waterMyPlants.projectConfig.catalog.configuration.WaterMyPlantsTestCatalogConfigurator
import com.marmatsan.waterMyPlants.projectConfig.catalog.configuration.WaterMyPlantsToolingPluginCatalogConfigurator
import com.marmatsan.waterMyPlants.projectConfig.catalog.configuration.WaterMyPlantsVersionProperties
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.configure

/** Selects the Water My Plants catalog through the public catalog settings API. */
class WaterMyPlantsSettingsPlugin : Plugin<Settings> {
    /** Applies the reusable settings plugin and supplies the Water My Plants catalog provider. */
    override fun apply(
        settings: Settings,
    ) {
        settings.pluginManager.apply(DependencyCatalogSettingsPlugin::class.java)
        settings.extensions.configure<DependencyCatalogSettingsExtension> {
            from(WaterMyPlantsCatalogProvider())
        }
        val versions = WaterMyPlantsVersionProperties.load(settings.rootDir)
        WaterMyPlantsTestCatalogConfigurator(
            versions = versions,
        ).configure(
            settings = settings,
        )
        WaterMyPlantsToolingPluginCatalogConfigurator(
            versions = versions,
        ).configure(
            settings = settings,
        )
    }
}
