package com.marmatsan.verificationPlatform.plugin.registration

import com.marmatsan.verificationPlatform.plugin.task.documentation.CheckDocumentationTask
import com.marmatsan.verificationPlatform.plugin.task.teamcity.CheckTeamCityDslTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Connects focused verification tasks to every project `check` lifecycle. */
internal class VerificationLifecycleConfigurer(
    private val project: Project,
    private val configurableTasks: ConfigurableVerificationTasks,
    private val checkDocumentation: TaskProvider<CheckDocumentationTask>,
    private val checkTeamCityDsl: TaskProvider<CheckTeamCityDslTask>
) {
    /** Defers lifecycle wiring until all projects and their `check` tasks exist. */
    fun configure() {
        project.gradle.projectsEvaluated {
            project.allprojects.forEach { candidate ->
                candidate.tasks.matching { task -> task.name == "check" }.configureEach { task ->
                    task.dependsOn(checkDocumentation)
                    task.dependsOn(configurableTasks.checkIncludedBuildVersions)
                    task.dependsOn(configurableTasks.checkModuleBoundaries)
                    task.dependsOn(configurableTasks.checkTypedResultUsage)
                    task.mustRunAfter(checkTeamCityDsl)
                }
            }
        }
    }
}
