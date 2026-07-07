package com.marmatsan.figmaDesignSync.domain.port.catalog

import com.marmatsan.figmaDesignSync.domain.port.gradle.IncludedBuildSource

/**
 * Repository source that can be read as catalog trees for Figma documentation.
 *
 * Each variant names a technical source without exposing `File`, Gradle APIs,
 * or parser details to the domain layer.
 *
 * Example:
 * ```
 * ProjectCatalogTreeSource.IncludedBuildSettings(
 *     includedBuild = IncludedBuildSource(
 *         settingsFilePath = "repo/gradle-plugins/settings.gradle.kts",
 *         rootDirPath = "repo/gradle-plugins",
 *         modulePathPrefix = ":gradle-plugins"
 *     )
 * )
 * ```
 */
sealed interface ProjectCatalogTreeSource {
    /**
     * Source for dependencies declared through the repository dependency DSL.
     *
     * Reference files:
     * `repo/dependency-catalog/src/main/kotlin/com/marmatsan/dependencies/LibraryTrees.kt`
     * and `repo/dependency-catalog/src/main/kotlin/com/marmatsan/dependencies/PluginTrees.kt`.
     *
     * @property rootDirPath Repository root containing the dependency DSL.
     */
    data class DependenciesDslVersionAliases(
        val rootDirPath: String
    ) : ProjectCatalogTreeSource

    /**
     * Source for catalogs declared in an included-build settings file.
     *
     * @property includedBuild Included build containing the settings catalogs.
     */
    data class IncludedBuildSettings(
        val includedBuild: IncludedBuildSource
    ) : ProjectCatalogTreeSource

    /**
     * Source for custom convention plugin declarations under included builds.
     *
     * This is used to document convention plugins that are implemented in the
     * repository rather than coming from an external catalog.
     *
     * @property rootDirPath Repository root containing modules that apply the
     * convention plugins.
     * @property includedBuilds Included builds that publish convention plugins.
     */
    data class CustomGradleConventionPlugins(
        val rootDirPath: String,
        val includedBuilds: List<IncludedBuildSource>
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
