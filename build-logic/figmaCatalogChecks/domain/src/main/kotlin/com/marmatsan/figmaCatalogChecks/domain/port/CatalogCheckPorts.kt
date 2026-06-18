package com.marmatsan.figmaCatalogChecks.domain.port

import com.marmatsan.figmaCatalogChecks.domain.model.*

data class VersionsFileSource(
    val path: String
)

data class FigmaVersionsSource(
    val section: FigmaNodeReference,
    val versionComponent: FigmaNodeReference,
    val token: String
)

interface RepositoryVersionsPort {
    fun readVersions(source: VersionsFileSource): Map<String, String>
}

interface FigmaVersionsPort {
    fun readVersions(source: FigmaVersionsSource): Map<String, String>
}

sealed interface ProjectCatalogTreeSource {
    data object DependenciesDslVersionAliases : ProjectCatalogTreeSource

    data class BuildLogicSettings(
        val settingsFilePath: String
    ) : ProjectCatalogTreeSource
}

data class FigmaCatalogTreeSource(
    val section: FigmaNodeReference,
    val token: String
)

interface ProjectCatalogTreesPort {
    fun readLibraryTree(source: ProjectCatalogTreeSource): LibraryCatalogTree
    fun readPluginTree(source: ProjectCatalogTreeSource): PluginCatalogTree
}

interface FigmaCatalogTreesPort {
    fun readLibraryTree(source: FigmaCatalogTreeSource): LibraryCatalogTree
    fun readPluginTree(source: FigmaCatalogTreeSource): PluginCatalogTree
}

data class ProjectModulesSource(
    val rootSettingsFilePath: String,
    val buildLogicSettingsFilePath: String
)

data class FigmaModuleComponentSource(
    val component: FigmaNodeReference,
    val token: String
)

interface ProjectModulesPort {
    fun readModules(source: ProjectModulesSource): Set<String>
}

interface FigmaModulesPort {
    fun readModules(source: FigmaModuleComponentSource): Set<String>
}
