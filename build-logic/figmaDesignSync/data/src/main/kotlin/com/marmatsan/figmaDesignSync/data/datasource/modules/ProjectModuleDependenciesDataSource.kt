package com.marmatsan.figmaDesignSync.data.datasource.modules

import com.marmatsan.figmaDesignSync.data.gradle.modules.GradleModuleDependenciesReader
import com.marmatsan.figmaDesignSync.domain.model.modules.ModuleDependency
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesScope
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModuleDependenciesSource
import java.io.File
import me.tatarka.inject.annotations.Inject

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
    override fun readModuleDependencies(source: ProjectModuleDependenciesSource): Set<ModuleDependency> =
        when (source.scope) {
            ProjectModuleDependenciesScope.Main -> gradleModuleDependenciesReader.readMain(File(source.rootDirPath))
            ProjectModuleDependenciesScope.BuildLogic -> gradleModuleDependenciesReader.readBuildLogic(
                File(source.rootDirPath)
            )
        }
}
