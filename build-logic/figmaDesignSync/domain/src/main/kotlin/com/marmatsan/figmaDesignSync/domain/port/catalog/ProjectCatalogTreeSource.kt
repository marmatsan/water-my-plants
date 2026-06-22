package com.marmatsan.figmaDesignSync.domain.port.catalog

sealed interface ProjectCatalogTreeSource {
    data class DependenciesDslVersionAliases(
        val rootDirPath: String
    ) : ProjectCatalogTreeSource

    data class BuildLogicSettings(
        val settingsFilePath: String
    ) : ProjectCatalogTreeSource

    data class CustomGradleConventionPlugins(
        val rootDirPath: String
    ) : ProjectCatalogTreeSource

    data class CustomGradlePlugins(
        val rootDirPath: String
    ) : ProjectCatalogTreeSource
}
