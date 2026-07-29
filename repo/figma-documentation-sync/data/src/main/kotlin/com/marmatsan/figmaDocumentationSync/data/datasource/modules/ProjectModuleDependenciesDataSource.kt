package com.marmatsan.figmaDocumentationSync.data.datasource.modules

import com.marmatsan.figmaDocumentationSync.data.gradle.modules.GradleModuleDependenciesReader
import com.marmatsan.figmaDocumentationSync.domain.model.modules.ModuleDependency
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesScope
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModuleDependenciesSource
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Adapter that exposes Gradle module dependency parsing through
 * [ProjectModuleDependenciesPort].
 *
 * The domain only asks for dependencies by [ProjectModuleDependenciesSource].
 * This adapter maps the source scope to the correct Gradle reader entry point.
 */
@Inject
class ProjectModuleDependenciesDataSource(
    private val gradleModuleDependenciesReader: GradleModuleDependenciesReader
) : ProjectModuleDependenciesPort {
    /** Reads module edges from the Gradle source and scope selected by [source]. */
    override fun readModuleDependencies(
        source: ProjectModuleDependenciesSource
    ): Set<ModuleDependency> =
        when (source.scope) {
            ProjectModuleDependenciesScope.Main -> {
                gradleModuleDependenciesReader.readMain(
                    rootDir = File(source.rootDirPath)
                )
            }

            ProjectModuleDependenciesScope.IncludedBuild -> {
                gradleModuleDependenciesReader.readIncludedBuild(
                    rootDir = File(source.rootDirPath),
                    modulePathPrefix = source.modulePathPrefix
                )
            }
        }
}
