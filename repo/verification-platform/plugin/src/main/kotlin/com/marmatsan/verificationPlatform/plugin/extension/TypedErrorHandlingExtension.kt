package com.marmatsan.verificationPlatform.plugin.extension

import com.marmatsan.verificationPlatform.plugin.task.errorhandling.CheckTypedResultUsageTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Configures the product source scopes governed by one typed-result implementation. */
class TypedErrorHandlingExtension internal constructor(
    private val project: Project,
    private val checkTypedResultUsage: TaskProvider<CheckTypedResultUsageTask>,
) {
    /** Selects the only fully qualified `Result` type accepted in configured product sources. */
    fun standardResult(
        qualifiedName: String,
    ) {
        require(qualifiedName.isNotBlank()) {
            "The standard Result qualified name must not be blank."
        }
        checkTypedResultUsage.configure { task ->
            task.acceptedResultQualifiedName.set(qualifiedName)
        }
    }

    /** Adds production Kotlin files below one repository-relative product scope. */
    fun productSourceScope(
        relativePath: String,
    ) {
        checkTypedResultUsage.configure { task ->
            task.inspectedFiles.from(
                project.fileTree(project.layout.projectDirectory.dir(relativePath)) { files ->
                    files.include("**/src/main/**/*.kt")
                    files.exclude(
                        "**/build/**",
                        "**/generated/**",
                        "**/tmp/**",
                    )
                },
            )
        }
    }
}
