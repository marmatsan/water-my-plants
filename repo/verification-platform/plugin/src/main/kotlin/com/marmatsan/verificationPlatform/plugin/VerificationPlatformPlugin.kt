package com.marmatsan.verificationPlatform.plugin

import com.marmatsan.verificationPlatform.data.gradle.GradleProjectModuleGraphSource
import com.marmatsan.verificationPlatform.plugin.extension.VerificationPlatformExtension
import com.marmatsan.verificationPlatform.plugin.task.boundary.CheckIncludedBuildVersionsTask
import com.marmatsan.verificationPlatform.plugin.task.boundary.CheckModuleBoundariesTask
import com.marmatsan.verificationPlatform.plugin.task.ci.GenerateCiPlanTask
import com.marmatsan.verificationPlatform.plugin.task.ci.GenerateCiTopologyPreviewTask
import com.marmatsan.verificationPlatform.plugin.task.documentation.CheckDocumentationTask
import com.marmatsan.verificationPlatform.plugin.task.errorhandling.CheckTypedResultUsageTask
import com.marmatsan.verificationPlatform.plugin.task.git.CheckGitWorkflowTask
import com.marmatsan.verificationPlatform.plugin.task.git.CheckRepositoryDiffTask
import com.marmatsan.verificationPlatform.plugin.task.teamcity.CheckTeamCityDslTask
import com.marmatsan.verificationPlatform.plugin.task.teamcity.PrepareTeamCityCiPlanTask
import com.marmatsan.verificationPlatform.plugin.task.teamcity.RunTeamCityInfrastructureHealthTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Registers the reviewed Gradle entry points exposed by the verification platform. */
class VerificationPlatformPlugin : Plugin<Project> {
    /**
     * Applies the verification-platform composition root to [project].
     *
     * The plugin must be applied to the root project because module-graph
     * discovery and report locations are repository-wide concerns.
     */
    override fun apply(
        project: Project,
    ) {
        require(project == project.rootProject) {
            "com.marmatsan.verificationPlatform must be applied to the root project."
        }

        val generateCiPlan =
            project.tasks.register(
                "generateCiPlan",
                GenerateCiPlanTask::class.java,
            ) { task ->
                task.group = "verification"
                task.description = "Generates the provider-neutral CI verification plan."
                task.repositoryRoot.set(project.layout.projectDirectory)
                project.providers
                    .gradleProperty("ciComparisonBase")
                    .orNull
                    ?.let(task.comparisonBaseOverride::set)
                task.outputFile.convention(project.layout.buildDirectory.file("reports/ci/ci-plan.json"))
            }

        val checkIncludedBuildVersions =
            project.tasks.register(
                "checkIncludedBuildVersions",
                CheckIncludedBuildVersionsTask::class.java,
            ) { task ->
                task.group = "verification"
                task.description = "Verifies that configured included builds own their versions.properties."
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.includedBuildPaths.convention(emptyList())
            }
        val checkModuleBoundaries =
            project.tasks.register(
                "checkModuleBoundaries",
                CheckModuleBoundariesTask::class.java,
            ) { task ->
                task.group = "verification"
                task.description = "Verifies configured module and included-build dependency boundaries."
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.reusableScopePaths.convention(emptyList())
                task.forbiddenReferencesByScope.convention(emptyMap())
            }
        val checkTypedResultUsage =
            project.tasks.register(
                "checkTypedResultUsage",
                CheckTypedResultUsageTask::class.java,
            ) { task ->
                task.group = "verification"
                task.description = "Verifies that product sources use the configured typed Result."
                task.repositoryRoot.set(project.layout.projectDirectory)
            }
        val extension =
            VerificationPlatformExtension(
                project = project,
                generateCiPlan = generateCiPlan,
                checkIncludedBuildVersions = checkIncludedBuildVersions,
                checkModuleBoundaries = checkModuleBoundaries,
                checkTypedResultUsage = checkTypedResultUsage,
            )
        project.extensions.add(
            "verificationPlatform",
            extension,
        )

        project.gradle.projectsEvaluated {
            val graph = GradleProjectModuleGraphSource().read(project)
            generateCiPlan.configure { task ->
                task.moduleDirectories.set(
                    graph.modules.associate { module -> module.id to module.directory },
                )
                task.moduleDependencyEdges.set(
                    graph.dependencies.map { dependency ->
                        "${dependency.dependentModule}->${dependency.dependencyModule}"
                    },
                )
            }
        }

        val checkGitWorkflow =
            project.tasks.register(
                "checkGitWorkflow",
                CheckGitWorkflowTask::class.java,
            ) { task ->
                task.group = "verification"
                task.description = "Validates the current branch against the trunk-based Git workflow."
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.branchOverride.convention(
                    project.providers
                        .gradleProperty("gitWorkflowBranch")
                        .orElse(project.providers.environmentVariable("GIT_WORKFLOW_BRANCH")),
                )
            }

        val checkDocumentation =
            project.tasks.register(
                "checkDocumentation",
                CheckDocumentationTask::class.java,
            ) { task ->
                task.group = "verification"
                task.description = "Validates typed documentation, links, sources, and committed change coverage."
                task.dependsOn(checkGitWorkflow)
                task.dependsOn(generateCiPlan)
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.coverageManifest.set(project.layout.projectDirectory.file(".teamcity/documentation-coverage.json"))
                task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
            }

        project.tasks.register(
            "checkRepositoryDiff",
            CheckRepositoryDiffTask::class.java,
        ) { task ->
            task.group = "verification"
            task.description = "Checks committed documentation-only diffs for whitespace errors."
            task.dependsOn(checkDocumentation)
            task.repositoryRoot.set(project.layout.projectDirectory)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
        }

        val checkTeamCityDsl =
            project.tasks.register(
                "checkTeamCityDsl",
                CheckTeamCityDslTask::class.java,
            ) { task ->
                task.group = "verification"
                task.description = "Generates and validates the effective TeamCity Kotlin DSL."
                task.dependsOn(checkDocumentation)
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.teamCityPom.set(extension.teamCity.pom)
                task.generatedConfigurationDirectory.set(extension.teamCity.generatedConfigurationDirectory)
                task.pipelineBuildTypeId.set(extension.teamCity.pipelineBuildTypeId)
                task.gateBuildTypeId.set(extension.teamCity.gateBuildTypeId)
                task.authoritativeStatusName.set(extension.teamCity.authoritativeStatusName)
            }

        project.gradle.projectsEvaluated {
            project.allprojects.forEach { candidate ->
                candidate.tasks.matching { task -> task.name == "check" }.configureEach { task ->
                    task.dependsOn(checkDocumentation)
                    task.dependsOn(checkIncludedBuildVersions)
                    task.dependsOn(checkModuleBoundaries)
                    task.dependsOn(checkTypedResultUsage)
                    task.mustRunAfter(checkTeamCityDsl)
                }
            }
        }

        project.tasks.register(
            "prepareTeamCityCiPlan",
            PrepareTeamCityCiPlanTask::class.java,
        ) { task ->
            task.group = "verification"
            task.description = "Generates the CI plan and exports its allow-listed TeamCity parameters."
            task.dependsOn(generateCiPlan)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
        }

        project.tasks.register(
            "generateCiTopologyPreview",
            GenerateCiTopologyPreviewTask::class.java,
        ) { task ->
            task.group = "verification"
            task.description = "Previews provider-neutral CI lanes without changing active TeamCity jobs."
            task.dependsOn(generateCiPlan)
            task.planFile.set(generateCiPlan.flatMap(GenerateCiPlanTask::outputFile))
            task.availableAgents.convention(
                project.providers
                    .gradleProperty("ciAvailableAgents")
                    .map(
                        String::toInt,
                    ).orElse(1),
            )
            task.outputFile.convention(
                project.layout.buildDirectory.file("reports/ci/ci-topology-preview.json"),
            )
        }

        project.tasks.register(
            "runTeamCityInfrastructureHealth",
            RunTeamCityInfrastructureHealthTask::class.java,
        ) { task ->
            task.group = "verification"
            task.description = "Queues the non-gating TeamCity Infrastructure Health pipeline."
            task.serverUrl.convention(
                project.providers
                    .gradleProperty("teamCityInfrastructureHealthServerUrl")
                    .orElse("http://127.0.0.1:8111"),
            )
            task.buildTypeId.convention(
                project.providers
                    .gradleProperty("teamCityInfrastructureHealthBuildTypeId")
                    .orElse(extension.teamCity.infrastructureHealthBuildTypeId),
            )
            task.branch.convention(
                project.providers.gradleProperty("teamCityInfrastructureHealthBranch").orElse("main"),
            )
            task.teamCityToken.convention(project.providers.environmentVariable("TEAMCITY_TOKEN"))
        }
    }
}
