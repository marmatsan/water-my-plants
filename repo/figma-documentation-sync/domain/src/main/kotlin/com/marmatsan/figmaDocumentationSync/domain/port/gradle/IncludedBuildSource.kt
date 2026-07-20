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
 */
data class IncludedBuildSource(
    val settingsFilePath: String,
    val rootDirPath: String,
    val modulePathPrefix: String,
    val publishesCatalogs: Boolean = true,
    val publishesConventionPlugins: Boolean = false,
)
