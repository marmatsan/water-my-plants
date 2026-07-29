package com.marmatsan.figmaDocumentationSync.domain.port.gradle

/**
 * Gradle included build that contributes repository model data.
 *
 * The source carries physical files plus the logical module prefix used in the
 * generated design model. Keeping the prefix explicit lets the sync model
 * represent included-build modules without coupling readers to a fixed
 * directory name.
 *
 * Example:
 * ```
 * IncludedBuildSource(
 *     settingsFilePath = "repo/gradle-plugins/settings.gradle.kts",
 *     rootDirPath = "repo/gradle-plugins",
 *     modulePathPrefix = ":gradle-plugins",
 *     publishesCatalogs = true,
 *     publishesConventionPlugins = true
 * )
 * ```
 *
 * @property settingsFilePath repository-relative included-build settings path.
 * @property rootDirPath repository-relative included-build root directory.
 * @property modulePathPrefix logical module prefix used in generated identities.
 * @property publishesCatalogs whether the build contributes dependency catalogs.
 * @property publishesConventionPlugins whether the build contributes convention plugins.
 */
data class IncludedBuildSource(
    val settingsFilePath: String,
    val rootDirPath: String,
    val modulePathPrefix: String,
    val publishesCatalogs: Boolean = true,
    val publishesConventionPlugins: Boolean = false
)
