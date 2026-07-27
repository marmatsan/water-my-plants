package com.marmatsan.verificationPlatform.plugin.extension

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

/** Binds included-build verification tasks to stable root entry points. */
class IncludedBuildVerificationTasksExtension internal constructor(
    private val project: Project,
) {
    /**
     * Exposes an included-build task through the root build without coupling the platform to its name.
     *
     * @return the root task provider created for the binding.
     */
    fun includedBuildTask(
        name: String,
        buildName: String,
        taskPath: String,
        description: String,
        requiredByCheck: Boolean = false,
        group: String = "verification",
    ): TaskProvider<Task> {
        val binding =
            project.tasks.register(name) { task ->
                task.group = group
                task.description = description
                task.dependsOn(project.gradle.includedBuild(buildName).task(taskPath))
            }
        if (requiredByCheck) {
            project.tasks.matching { task -> task.name == "check" }.configureEach { task ->
                task.dependsOn(binding)
            }
        }
        return binding
    }
}
