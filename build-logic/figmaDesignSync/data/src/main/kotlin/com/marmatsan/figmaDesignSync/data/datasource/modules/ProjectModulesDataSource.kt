package com.marmatsan.figmaDesignSync.data.datasource.modules


import com.marmatsan.figmaDesignSync.data.gradle.modules.GradleProjectModulesReader
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDesignSync.domain.port.modules.ProjectModulesSource
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
