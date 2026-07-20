package com.marmatsan.figmaDocumentationSync.data.datasource.modules

import com.marmatsan.figmaDocumentationSync.data.gradle.modules.GradleProjectModulesReader
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModulesPort
import com.marmatsan.figmaDocumentationSync.domain.port.modules.ProjectModulesSource
import me.tatarka.inject.annotations.Inject
import java.io.File

/**
 * Adapter that discovers Gradle module paths from root and included-build
 * settings files.
 *
 * The generated module set is consumed by `design-model.json` to document the
 * repository structure independently from Gradle's runtime model.
 */
@Inject
class ProjectModulesDataSource(
    private val gradleProjectModulesReader: GradleProjectModulesReader,
) : ProjectModulesPort {
    override fun readModules(
        source: ProjectModulesSource,
    ): Set<String> =
        gradleProjectModulesReader.readModules(
            rootSettingsFile = File(source.rootSettingsFilePath),
            includedBuilds =
                source.includedBuilds.map { includedBuild ->
                    GradleProjectModulesReader.IncludedBuild(
                        settingsFile = File(includedBuild.settingsFilePath),
                        modulePathPrefix = includedBuild.modulePathPrefix,
                    )
                },
        )
}
