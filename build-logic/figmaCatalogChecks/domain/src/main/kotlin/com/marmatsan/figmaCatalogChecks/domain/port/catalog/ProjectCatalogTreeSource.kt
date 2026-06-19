package com.marmatsan.figmaCatalogChecks.domain.port.catalog

sealed interface ProjectCatalogTreeSource {
    data object DependenciesDslVersionAliases : ProjectCatalogTreeSource

    data class BuildLogicSettings(
        val settingsFilePath: String
    ) : ProjectCatalogTreeSource

    data class CustomGradleConventionPlugins(
        val rootDirPath: String
    ) : ProjectCatalogTreeSource
}
