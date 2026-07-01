package com.marmatsan.figmaDesignSync.domain.port.catalog

/**
 * Repository source that can be read as catalog trees for Figma documentation.
 *
 * Each variant names a technical source without exposing `File`, Gradle APIs,
 * or parser details to the domain layer.
 *
 * Example:
 * ```
 * ProjectCatalogTreeSource.BuildLogicSettings(
 *     settingsFilePath = "build-logic/settings.gradle.kts"
 * )
 * ```
 */
sealed interface ProjectCatalogTreeSource {
    /**
     * Source for dependencies declared through the repository dependency DSL.
     *
     * Reference files:
     * `build-logic/dependencies/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt`
     * and `build-logic/dependencies/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt`.
     *
     * @property rootDirPath Repository root containing the dependency DSL.
     */
    data class DependenciesDslVersionAliases(
        val rootDirPath: String
    ) : ProjectCatalogTreeSource

    /**
     * Source for catalogs declared in `build-logic/settings.gradle.kts`.
     *
     * @property settingsFilePath Path to `build-logic/settings.gradle.kts`.
     */
    data class BuildLogicSettings(
        val settingsFilePath: String
    ) : ProjectCatalogTreeSource

    /**
     * Source for custom convention plugin declarations under `build-logic`.
     *
     * This is used to document convention plugins that are implemented in the
     * `build-logic` included build rather than coming from an external catalog.
     *
     * @property rootDirPath Repository root containing `build-logic`.
     */
    data class CustomGradleConventionPlugins(
        val rootDirPath: String
    ) : ProjectCatalogTreeSource

    /**
     * Source for custom Gradle plugin declarations applied by project modules.
     *
     * This source lets the design model include repository-owned plugins that
     * appear in module `build.gradle.kts` files.
     *
     * @property rootDirPath Repository root containing project modules.
     */
    data class CustomGradlePlugins(
        val rootDirPath: String
    ) : ProjectCatalogTreeSource
}
