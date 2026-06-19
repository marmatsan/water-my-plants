package com.marmatsan.figmaCatalogChecks.data.datasource.catalog

import com.marmatsan.figmaCatalogChecks.data.dependencies.catalog.DependenciesCatalogTreesReader
import com.marmatsan.figmaCatalogChecks.data.gradle.catalog.BuildLogicSettingsCatalogReader
import com.marmatsan.figmaCatalogChecks.data.gradle.catalog.GradleConventionPluginTreeReader
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreesPort
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class ProjectCatalogTreesDataSource(
    private val buildLogicSettingsCatalogReader: BuildLogicSettingsCatalogReader,
    private val dependenciesCatalogTreesReader: DependenciesCatalogTreesReader,
    private val gradleConventionPluginTreeReader: GradleConventionPluginTreeReader
) : ProjectCatalogTreesPort {
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree =
        when (source) {
            ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readLibraryTreeWithVersionAliases()

            is ProjectCatalogTreeSource.BuildLogicSettings ->
                buildLogicSettingsCatalogReader.readLibraryTree(File(source.settingsFilePath))

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins ->
                error("Custom Gradle convention plugins do not define a library catalog tree")
        }

    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree =
        when (source) {
            ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readPluginTreeWithVersionAliases()

            is ProjectCatalogTreeSource.BuildLogicSettings ->
                buildLogicSettingsCatalogReader.readPluginTree(File(source.settingsFilePath))

            is ProjectCatalogTreeSource.CustomGradleConventionPlugins ->
                gradleConventionPluginTreeReader.readPluginTree(File(source.rootDirPath))
        }
}
