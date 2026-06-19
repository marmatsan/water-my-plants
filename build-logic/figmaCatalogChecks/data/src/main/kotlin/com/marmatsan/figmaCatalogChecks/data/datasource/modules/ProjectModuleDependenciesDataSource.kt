package com.marmatsan.figmaCatalogChecks.data.datasource.modules

import com.marmatsan.figmaCatalogChecks.data.gradle.modules.GradleModuleDependenciesReader
import com.marmatsan.figmaCatalogChecks.domain.model.modules.ModuleDependency
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModuleDependenciesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModuleDependenciesScope
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModuleDependenciesSource
import java.io.File
import me.tatarka.inject.annotations.Inject

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
