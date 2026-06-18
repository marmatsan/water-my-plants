package com.marmatsan.figmaCatalogChecks.data.datasource.modules


import com.marmatsan.figmaCatalogChecks.data.gradle.modules.GradleProjectModulesReader
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaCatalogChecks.domain.port.modules.ProjectModulesSource
import me.tatarka.inject.annotations.Inject
import java.io.File

@Inject
class ProjectModulesDataSource(
    private val gradleProjectModulesReader: GradleProjectModulesReader
) : ProjectModulesPort {
    override fun readModules(source: ProjectModulesSource): Set<String> =
        gradleProjectModulesReader.readModules(
            rootSettingsFile = File(source.rootSettingsFilePath),
            buildLogicSettingsFile = File(source.buildLogicSettingsFilePath)
        )
}
