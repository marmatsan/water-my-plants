package com.marmatsan.waterMyPlants.projectConfig

import org.gradle.api.Plugin
import org.gradle.api.Project

/** Composes the Water My Plants adapters around the reusable Figma plugin. */
class WaterMyPlantsProjectConfigPlugin : Plugin<Project> {
    override fun apply(
        project: Project,
    ) {
        project.pluginManager.apply("com.marmatsan.figmaDocumentationSync")
        WaterMyPlantsFigmaExtensionConfigurator(
            project = project,
            writerConfig = WaterMyPlantsFigmaWriterProjectConfig.value,
        ).configure()
        WaterMyPlantsFigmaWriterTasksRegistrar(
            project = project,
            writerConfig = WaterMyPlantsFigmaWriterProjectConfig.value,
        ).register()
        WaterMyPlantsTeamCityFigmaTasksRegistrar(
            project = project,
        ).register()
    }
}
