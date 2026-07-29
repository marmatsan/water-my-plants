package com.marmatsan.verificationPlatform.plugin.extension

import com.marmatsan.verificationPlatform.plugin.task.boundary.CheckIncludedBuildVersionsTask
import com.marmatsan.verificationPlatform.plugin.task.boundary.CheckModuleBoundariesTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

/** Configures independently owned version registries and reusable-source dependency boundaries. */
class RepositoryBoundariesExtension internal constructor(
    private val project: Project,
    private val checkIncludedBuildVersions: TaskProvider<CheckIncludedBuildVersionsTask>,
    private val checkModuleBoundaries: TaskProvider<CheckModuleBoundariesTask>
) {
    /** Adds one included build whose settings and local version registry must be verified. */
    fun versionedBuild(
        relativePath: String
    ) {
        checkIncludedBuildVersions.configure { task ->
            task.includedBuildPaths.add(relativePath)
            task.includedBuildConfigurationFiles.from(
                project.layout.projectDirectory.file("$relativePath/settings.gradle.kts"),
                project.layout.projectDirectory.file("$relativePath/versions.properties")
            )
        }
    }

    /**
     * Requires every configured included build that declares [propertyName] to
     * use the same value while allowing non-consumers to omit the property.
     */
    fun alignedVersion(
        propertyName: String
    ) {
        require(propertyName.isNotBlank()) {
            "The aligned version property name must not be blank."
        }
        checkIncludedBuildVersions.configure { task ->
            task.alignedVersionProperties.add(propertyName)
        }
    }

    /** Adds a reusable source scope and the implementation references it must not know. */
    fun reusableScope(
        relativePath: String,
        vararg forbiddenReferences: String
    ) {
        checkModuleBoundaries.configure { task ->
            task.reusableScopePaths.add(relativePath)
            task.forbiddenReferencesByScope.put(
                relativePath,
                forbiddenReferences.joinToString(CheckModuleBoundariesTask.REFERENCE_SEPARATOR)
            )
            task.inspectedFiles.from(
                project.fileTree(project.layout.projectDirectory.dir(relativePath)) { files ->
                    files.include(
                        "settings.gradle.kts",
                        "**/*.gradle.kts",
                        "**/src/main/**/*.kt"
                    )
                    files.exclude(
                        "**/build/**",
                        "**/.gradle/**",
                        "**/tmp/**"
                    )
                }
            )
        }
    }
}
