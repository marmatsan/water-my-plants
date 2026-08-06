package com.marmatsan.waterMyPlants.projectConfig.teamcity.configuration

import com.marmatsan.figmaDocumentationSync.teamcity.operations.gradle.FigmaTeamCityOperationsExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Supplies Water My Plants identities to the reusable TeamCity operations plugin. */
internal class WaterMyPlantsTeamCityOperationsConfigurator(
    private val project: Project
) {
    /** Configures the canonical pipeline, branch, artifact producer, and public TeamCity origin. */
    fun configure() {
        project.extensions.configure<FigmaTeamCityOperationsExtension> {
            buildTypeId.set("WaterMyPlants_WaterMyPlantsFigmaSync")
            branch.set("main")
            mainBranchAliases.set(
                listOf(
                    "main",
                    "<default>",
                    "refs/heads/main"
                )
            )
            requiredBuildTypeName.set("Generate main design model")
            serverUrl.set("https://teamcity.marmatsan.dev")
        }
    }
}
