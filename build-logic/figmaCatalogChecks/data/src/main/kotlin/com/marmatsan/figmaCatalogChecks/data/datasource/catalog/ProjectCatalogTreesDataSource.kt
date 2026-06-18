package com.marmatsan.figmaCatalogChecks.data.datasource.catalog

import com.marmatsan.figmaCatalogChecks.data.dependencies.catalog.DependenciesCatalogTreesReader
import com.marmatsan.figmaCatalogChecks.data.gradle.catalog.BuildLogicSettingsCatalogReader
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.LibraryCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.model.catalog.PluginCatalogTree
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreeSource
import com.marmatsan.figmaCatalogChecks.domain.port.catalog.ProjectCatalogTreesPort
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
class ProjectCatalogTreesDataSource(
    private val buildLogicSettingsCatalogReader: BuildLogicSettingsCatalogReader,
    private val dependenciesCatalogTreesReader: DependenciesCatalogTreesReader
) : ProjectCatalogTreesPort {
    override fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree =
        when (source) {
            ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readLibraryTreeWithVersionAliases()

            is ProjectCatalogTreeSource.BuildLogicSettings ->
                buildLogicSettingsCatalogReader.readLibraryTree(File(source.settingsFilePath))
        }

    override fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree =
        when (source) {
            ProjectCatalogTreeSource.DependenciesDslVersionAliases ->
                dependenciesCatalogTreesReader.readPluginTreeWithVersionAliases()

            is ProjectCatalogTreeSource.BuildLogicSettings ->
                buildLogicSettingsCatalogReader.readPluginTree(File(source.settingsFilePath))
        }
}
