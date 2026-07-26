package com.marmatsan.waterMyPlants.projectConfig

import com.marmatsan.dependencies.gradle.DependencyCatalogSettingsExtension
import com.marmatsan.dependencies.gradle.DependencyCatalogSettingsPlugin
import com.marmatsan.waterMyPlants.projectConfig.catalog.WaterMyPlantsCatalogProvider
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.kotlin.dsl.configure

/** Selects the Water My Plants catalog through the public catalog settings API. */
class WaterMyPlantsSettingsPlugin : Plugin<Settings> {
    override fun apply(
        settings: Settings,
    ) {
        settings.pluginManager.apply(DependencyCatalogSettingsPlugin::class.java)
        settings.extensions.configure<DependencyCatalogSettingsExtension> {
            from(WaterMyPlantsCatalogProvider())
        }
    }
}
