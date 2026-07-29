package com.marmatsan.verificationPlatform.plugin.registration

import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

/** Registers one lazily configured task in the shared verification group. */
internal fun <TaskType : Task> TaskContainer.registerVerificationTask(
    name: String,
    type: Class<TaskType>,
    description: String,
    configure: (TaskType) -> Unit
): TaskProvider<TaskType> =
    register(
        name,
        type
    ) { task ->
        task.group = "verification"
        task.description = description
        configure(task)
    }
