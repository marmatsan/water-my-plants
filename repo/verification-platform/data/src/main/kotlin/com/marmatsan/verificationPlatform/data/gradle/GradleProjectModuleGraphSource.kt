package com.marmatsan.verificationPlatform.data.gradle

import com.marmatsan.verificationPlatform.domain.model.ModuleDependency
import com.marmatsan.verificationPlatform.domain.model.RepositoryModule
import com.marmatsan.verificationPlatform.domain.model.RepositoryModuleGraph
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/** Reads the evaluated root Gradle project model into the provider-neutral graph. */
class GradleProjectModuleGraphSource {
    /**
     * Captures project modules and project-dependency edges from [rootProject].
     *
     * Only subprojects with a build file are included. The result is sorted and
     * deduplicated so domain planning remains deterministic.
     *
     * @throws IllegalArgumentException when called with a non-root project.
     */
    fun read(
        rootProject: Project,
    ): RepositoryModuleGraph {
        require(rootProject == rootProject.rootProject) {
            "The CI module graph must be read from the root project."
        }

        val moduleProjects =
            rootProject.subprojects
                .filter { project -> project.buildFile.isFile }
                .sortedBy(Project::getPath)
        val modules =
            moduleProjects.map { project ->
                RepositoryModule(
                    id = project.path,
                    directory =
                        rootProject.relativePath(project.projectDir).replace(
                            '\\',
                            '/',
                        ),
                )
            }
        val dependencies =
            moduleProjects
                .flatMap { dependentProject ->
                    dependentProject.configurations.flatMap { configuration ->
                        configuration.dependencies
                            .withType(ProjectDependency::class.java)
                            .map { dependency ->
                                ModuleDependency(
                                    dependentModule = dependentProject.path,
                                    dependencyModule = dependency.path,
                                )
                            }
                    }
                }.distinct()
                .sortedWith(
                    compareBy(
                        ModuleDependency::dependentModule,
                        ModuleDependency::dependencyModule,
                    ),
                )

        return RepositoryModuleGraph(
            modules = modules,
            dependencies = dependencies,
        )
    }
}
