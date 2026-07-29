package com.marmatsan.verificationPlatform.plugin.registration

import com.marmatsan.verificationPlatform.data.gradle.GradleProjectModuleGraphSource
import com.marmatsan.verificationPlatform.plugin.task.boundary.CheckIncludedBuildVersionsTask
import com.marmatsan.verificationPlatform.plugin.task.boundary.CheckModuleBoundariesTask
import com.marmatsan.verificationPlatform.plugin.task.ci.GenerateCiPlanTask
import com.marmatsan.verificationPlatform.plugin.task.errorhandling.CheckTypedResultUsageTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Lazy task providers consumed by the public verification-platform extension. */
internal data class ConfigurableVerificationTasks(
    /** Provider-neutral CI plan generator. */
    val generateCiPlan: TaskProvider<GenerateCiPlanTask>,
    /** Included-build version-ownership verification. */
    val checkIncludedBuildVersions: TaskProvider<CheckIncludedBuildVersionsTask>,
    /** Module dependency-boundary verification. */
    val checkModuleBoundaries: TaskProvider<CheckModuleBoundariesTask>,
    /** Typed Result usage verification. */
    val checkTypedResultUsage: TaskProvider<CheckTypedResultUsageTask>
)

/** Registers tasks whose inputs are configured through the public extension. */
internal class ConfigurableVerificationTasksRegistrar(
    private val project: Project
) {
    /** Registers configurable tasks and attaches the evaluated Gradle module graph. */
    fun register(): ConfigurableVerificationTasks {
        val generateCiPlan =
            project.tasks.registerVerificationTask(
                "generateCiPlan",
                GenerateCiPlanTask::class.java,
                "Generates the provider-neutral CI verification plan."
            ) { task ->
                task.repositoryRoot.set(project.layout.projectDirectory)
                project.providers
                    .gradleProperty("ciComparisonBase")
                    .orNull
                    ?.let(task.comparisonBaseOverride::set)
                task.outputFile.convention(project.layout.buildDirectory.file("reports/ci/ci-plan.json"))
            }

        val checkIncludedBuildVersions =
            project.tasks.registerVerificationTask(
                "checkIncludedBuildVersions",
                CheckIncludedBuildVersionsTask::class.java,
                "Verifies included-build version ownership and configured cross-build alignment."
            ) { task ->
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.includedBuildPaths.convention(emptyList())
                task.alignedVersionProperties.convention(emptyList())
            }

        val checkModuleBoundaries =
            project.tasks.registerVerificationTask(
                "checkModuleBoundaries",
                CheckModuleBoundariesTask::class.java,
                "Verifies configured module and included-build dependency boundaries."
            ) { task ->
                task.repositoryRoot.set(project.layout.projectDirectory)
                task.reusableScopePaths.convention(emptyList())
                task.forbiddenReferencesByScope.convention(emptyMap())
            }

        val checkTypedResultUsage =
            project.tasks.registerVerificationTask(
                "checkTypedResultUsage",
                CheckTypedResultUsageTask::class.java,
                "Verifies that production sources use the configured typed Result."
            ) { task ->
                task.repositoryRoot.set(project.layout.projectDirectory)
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

        return ConfigurableVerificationTasks(
            generateCiPlan = generateCiPlan,
            checkIncludedBuildVersions = checkIncludedBuildVersions,
            checkModuleBoundaries = checkModuleBoundaries,
            checkTypedResultUsage = checkTypedResultUsage
        )
    }
}
