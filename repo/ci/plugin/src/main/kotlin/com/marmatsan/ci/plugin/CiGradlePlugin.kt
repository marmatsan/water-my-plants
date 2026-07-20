package com.marmatsan.ci.plugin

import com.marmatsan.ci.data.gradle.GradleProjectModuleGraphSource
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Registers the reviewed Gradle entry points for repository CI planning. */
class CiGradlePlugin : Plugin<Project> {
    /**
     * Applies the CI composition root to [project].
     *
     * The plugin must be applied to the root project because module-graph
     * discovery and report locations are repository-wide concerns.
     */
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

        val checkDocumentation = project.tasks.register(
            "checkDocumentation",
            CheckDocumentationTask::class.java
        ) { task ->
            task.group = "verification"
            task.description = "Validates typed documentation, links, sources, and committed change coverage."
            task.dependsOn(generateCiPlan)
            task.repositoryRoot.set(project.layout.projectDirectory)
            task.coverageManifest.set(project.layout.projectDirectory.file(".teamcity/documentation-coverage.json"))
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
        }

        project.tasks.register("checkRepositoryDiff", CheckRepositoryDiffTask::class.java) { task ->
            task.group = "verification"
            task.description = "Checks committed documentation-only diffs for whitespace errors."
            task.dependsOn(checkDocumentation)
            task.repositoryRoot.set(project.layout.projectDirectory)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
        }

        val checkTeamCityDsl = project.tasks.register("checkTeamCityDsl", CheckTeamCityDslTask::class.java) { task ->
            task.group = "verification"
            task.description = "Generates and validates the effective TeamCity Kotlin DSL."
            task.dependsOn(checkDocumentation)
            task.repositoryRoot.set(project.layout.projectDirectory)
            task.teamCityPom.set(project.layout.projectDirectory.file(".teamcity/pom.xml"))
        }

        project.gradle.projectsEvaluated {
            project.allprojects.forEach { candidate ->
                candidate.tasks.matching { task -> task.name == "check" }.configureEach { task ->
                    task.dependsOn(checkDocumentation)
                    task.mustRunAfter(checkTeamCityDsl)
                }
            }
        }

        project.tasks.register("prepareTeamCityCiPlan", PrepareTeamCityCiPlanTask::class.java) { task ->
            task.group = "verification"
            task.description = "Generates the CI plan and exports its allow-listed TeamCity parameters."
            task.dependsOn(generateCiPlan)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
        }

        project.tasks.register(
            "generateCiTopologyPreview",
            GenerateCiTopologyPreviewTask::class.java
        ) { task ->
            task.group = "verification"
            task.description = "Previews provider-neutral CI lanes without changing active TeamCity jobs."
            task.dependsOn(generateCiPlan)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
            task.availableAgents.convention(
                project.providers.gradleProperty("ciAvailableAgents").map(String::toInt).orElse(1)
            )
            task.outputFile.convention(
                project.layout.buildDirectory.file("reports/ci/ci-topology-preview.json")
            )
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
