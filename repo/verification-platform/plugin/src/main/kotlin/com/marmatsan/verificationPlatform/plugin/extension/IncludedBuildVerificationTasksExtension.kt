package com.marmatsan.verificationPlatform.plugin.extension

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import java.io.File

/** Binds reusable-build verification tasks to stable root entry points. */
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
        bindToCheckWhenRequired(
            binding = binding,
            requiredByCheck = requiredByCheck,
        )
        return binding
    }

    /**
     * Runs one reusable build through the repository wrapper with explicit project properties.
     *
     * Use this binding when a source-independent verification needs values that Gradle task
     * references cannot forward into an included build. The nested invocation remains explicit:
     * [buildDirectory] selects the build, [taskPath] selects its public verification entry point,
     * and [projectProperties] declares every composition-owned input.
     *
     * @param name Stable task name exposed by the root build.
     * @param buildDirectory Reusable build directory to execute.
     * @param taskPath Public task path within [buildDirectory].
     * @param projectProperties Gradle project properties passed as `-Pkey=value` arguments.
     * @param description Human-readable task description.
     * @param requiredByCheck Whether the root `check` task depends on this binding.
     * @param group Gradle task group.
     * @return Root [Exec] task provider created for the isolated invocation.
     */
    fun isolatedGradleBuildTask(
        name: String,
        buildDirectory: File,
        taskPath: String,
        projectProperties: Map<String, String>,
        description: String,
        requiredByCheck: Boolean = false,
        group: String = "verification",
    ): TaskProvider<Exec> {
        val wrapper =
            project.rootProject.file(
                if (
                    System.getProperty("os.name").startsWith(
                        "Windows",
                        ignoreCase = true,
                    )
                ) {
                    "gradlew.bat"
                } else {
                    "gradlew"
                },
            )
        val binding =
            project.tasks.register(
                name,
                Exec::class.java,
            ) { task ->
                task.group = group
                task.description = description
                task.workingDir(buildDirectory)
                task.commandLine(
                    buildList {
                        add(wrapper.absolutePath)
                        add("--no-daemon")
                        add(taskPath)
                        projectProperties.forEach { (key, value) ->
                            add("-P$key=$value")
                        }
                        add("--stacktrace")
                    },
                )
            }
        bindToCheckWhenRequired(
            binding = binding,
            requiredByCheck = requiredByCheck,
        )
        return binding
    }

    private fun bindToCheckWhenRequired(
        binding: TaskProvider<out Task>,
        requiredByCheck: Boolean,
    ) {
        if (requiredByCheck) {
            project.tasks.matching { task -> task.name == "check" }.configureEach { task ->
                task.dependsOn(binding)
            }
        }
    }
}
