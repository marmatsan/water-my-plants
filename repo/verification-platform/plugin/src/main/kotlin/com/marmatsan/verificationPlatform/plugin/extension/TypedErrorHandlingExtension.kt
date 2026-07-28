package com.marmatsan.verificationPlatform.plugin.extension

import com.marmatsan.verificationPlatform.plugin.task.errorhandling.CheckTypedResultUsageTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Configures production source scopes governed by one typed-result implementation. */
class TypedErrorHandlingExtension internal constructor(
    private val project: Project,
    private val checkTypedResultUsage: TaskProvider<CheckTypedResultUsageTask>,
) {
    /** Selects the only fully qualified `Result` type accepted in configured production sources. */
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

    /** Adds production Kotlin files below one repository-relative scope. */
    fun productionSourceScope(
        relativePath: String,
    ) {
        require(relativePath.isNotBlank()) {
            "The production source scope must not be blank."
        }
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

    /**
     * Adds production Kotlin files below one Water My Plants product scope.
     *
     * @deprecated Use [productionSourceScope], which also describes reusable
     * repository modules without implying that they are product code.
     */
    @Deprecated(
        message = "Use productionSourceScope(relativePath)",
        replaceWith = ReplaceWith("productionSourceScope(relativePath)"),
    )
    fun productSourceScope(
        relativePath: String,
    ) = productionSourceScope(
        relativePath = relativePath,
    )
}
