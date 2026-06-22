package com.marmatsan.figmaDesignSync.data.datasource.catalog

import com.marmatsan.figmaDesignSync.data.dependencies.catalog.DependenciesCatalogTreesReader
import com.marmatsan.figmaDesignSync.data.gradle.catalog.BuildLogicSettingsCatalogReader
import com.marmatsan.figmaDesignSync.data.gradle.catalog.GradleConventionPluginTreeReader
import com.marmatsan.figmaDesignSync.data.gradle.catalog.GradleCatalogUsageReader
import com.marmatsan.figmaDesignSync.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaDesignSync.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaDesignSync.domain.port.catalog.ProjectCatalogTreesPort
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class ProjectCatalogTreesDataSource(
    private val buildLogicSettingsCatalogReader: BuildLogicSettingsCatalogReader,
    private val dependenciesCatalogTreesReader: DependenciesCatalogTreesReader,
    private val gradleCatalogUsageReader: GradleCatalogUsageReader,
    private val gradleConventionPluginTreeReader: GradleConventionPluginTreeReader
) : ProjectCatalogTreesPort {
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree =
        when (source) {
            is ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readLibraryTreeWithVersionAliases(
                    rootDir = File(source.rootDirPath)
                )

            is ProjectCatalogTreeSource.BuildLogicSettings ->
                buildLogicSettingsCatalogReader.readLibraryTree(
                    settingsFile = File(source.settingsFilePath),
                    usageByAlias = gradleCatalogUsageReader.readBuildLogicLibraryUsages(
                        buildLogicRootDir = File(source.settingsFilePath).parentFile
                    )
                )

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins ->
                error("Custom Gradle convention plugins do not define a library catalog tree")
        }

    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree =
        when (source) {
            is ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readPluginTreeWithVersionAliases(
                    rootDir = File(source.rootDirPath)
                )

            is ProjectCatalogTreeSource.BuildLogicSettings ->
                buildLogicSettingsCatalogReader.readPluginTree(
                    settingsFile = File(source.settingsFilePath),
                    usageByAlias = gradleCatalogUsageReader.readBuildLogicPluginUsages(
                        buildLogicRootDir = File(source.settingsFilePath).parentFile
                    )
                )

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins ->
                gradleConventionPluginTreeReader.readPluginTree(
                    rootDir = File(source.rootDirPath),
                    usageByPluginId = gradleCatalogUsageReader.readMainLiteralPluginUsages(
                        rootDir = File(source.rootDirPath)
                    )
                )
        }
}
