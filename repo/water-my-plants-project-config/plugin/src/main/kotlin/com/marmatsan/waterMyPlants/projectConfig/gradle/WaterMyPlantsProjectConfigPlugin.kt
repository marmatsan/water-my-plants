package com.marmatsan.waterMyPlants.projectConfig.gradle

import com.marmatsan.waterMyPlants.projectConfig.figma.configuration.WaterMyPlantsFigmaExtensionConfigurator
import com.marmatsan.waterMyPlants.projectConfig.figma.configuration.WaterMyPlantsFigmaWriterProjectConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Composes the Water My Plants adapters around the reusable Figma plugin. */
class WaterMyPlantsProjectConfigPlugin : Plugin<Project> {
    /** Applies the reusable plugin and delegates repository configuration to focused registrars. */
    override fun apply(
        project: Project
    ) {
        project.pluginManager.apply("com.marmatsan.figmaDocumentationSync")
        WaterMyPlantsFigmaExtensionConfigurator(
            project = project,
            writerConfig = WaterMyPlantsFigmaWriterProjectConfig.value
        ).configure()
        WaterMyPlantsFigmaWriterTasksRegistrar(
            project = project,
            writerConfig = WaterMyPlantsFigmaWriterProjectConfig.value
        ).register()
        WaterMyPlantsTeamCityFigmaTasksRegistrar(
            project = project
        ).register()
    }
}
