package com.marmatsan.ci.plugin

import com.marmatsan.ci.data.gradle.GradleProjectModuleGraphSource
import org.gradle.api.Plugin
import org.gradle.api.Project

class CiGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        require(project == project.rootProject) {
            "com.marmatsan.ci must be applied to the root project."
        }

        val generateCiPlan = project.tasks.register("generateCiPlan", GenerateCiPlanTask::class.java) { task ->
            task.group = "verification"
            task.description = "Generates the provider-neutral CI verification plan."
            task.repositoryRoot.set(project.layout.projectDirectory)
            project.providers.gradleProperty("ciComparisonBase").orNull?.let(task.comparisonBaseOverride::set)
            task.outputFile.convention(project.layout.buildDirectory.file("reports/ci/ci-plan.json"))
        }

        project.gradle.projectsEvaluated {
            val graph = GradleProjectModuleGraphSource().read(project)
            generateCiPlan.configure { task ->
                task.moduleDirectories.set(
                    graph.modules.associate { module -> module.id to module.directory }
                )
                task.moduleDependencyEdges.set(
                    graph.dependencies.map { dependency ->
                        "${dependency.dependentModule}->${dependency.dependencyModule}"
                    }
                )
            }
        }

        project.tasks.register("prepareTeamCityCiPlan", PrepareTeamCityCiPlanTask::class.java) { task ->
            task.group = "verification"
            task.description = "Generates the CI plan and exports its allow-listed TeamCity parameters."
            task.dependsOn(generateCiPlan)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
        }

        project.tasks.register(
            "runTeamCityInfrastructureHealth",
            RunTeamCityInfrastructureHealthTask::class.java
        ) { task ->
            task.group = "verification"
            task.description = "Queues the non-gating TeamCity Infrastructure Health pipeline."
            task.serverUrl.convention(
                project.providers.gradleProperty("teamCityInfrastructureHealthServerUrl")
                    .orElse("http://127.0.0.1:8111")
            )
            task.buildTypeId.convention(
                project.providers.gradleProperty("teamCityInfrastructureHealthBuildTypeId")
                    .orElse("WaterMyPlants_WaterMyPlantsInfrastructureHealth")
            )
            task.branch.convention(
                project.providers.gradleProperty("teamCityInfrastructureHealthBranch").orElse("main")
            )
            task.teamCityToken.convention(project.providers.environmentVariable("TEAMCITY_TOKEN"))
        }
    }
}
