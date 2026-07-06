package com.marmatsan.figmaDesignSync.domain.port.modules

import com.marmatsan.figmaDesignSync.domain.port.gradle.IncludedBuildSource

/**
 * Root and included-build settings files used to discover Gradle module paths.
 *
 * Reference files are the root `settings.gradle.kts` plus any configured
 * included-build `settings.gradle.kts` files.
 *
 * Example:
 * ```
 * ProjectModulesSource(
 *     rootSettingsFilePath = "settings.gradle.kts",
 *     includedBuilds = listOf(
 *         IncludedBuildSource(
 *             settingsFilePath = "repo/gradle-plugins/settings.gradle.kts",
 *             rootDirPath = "repo/gradle-plugins",
 *             modulePathPrefix = ":gradle-plugins"
 *         )
 *     )
 * )
 * ```
 *
 * @property rootSettingsFilePath Path to the root Gradle settings file.
 * @property includedBuilds Included builds whose modules should be included in
 * the design model.
 */
data class ProjectModulesSource(
    val rootSettingsFilePath: String,
    val includedBuilds: List<IncludedBuildSource> = emptyList()
)
