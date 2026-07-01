package com.marmatsan.figmaDesignSync.domain.port.modules

/**
 * Root and build-logic settings files used to discover Gradle module paths.
 *
 * Reference files are the root `settings.gradle.kts` and
 * `build-logic/settings.gradle.kts`.
 *
 * Example:
 * ```
 * ProjectModulesSource(
 *     rootSettingsFilePath = "settings.gradle.kts",
 *     buildLogicSettingsFilePath = "build-logic/settings.gradle.kts"
 * )
 * ```
 *
 * @property rootSettingsFilePath Path to the root Gradle settings file.
 * @property buildLogicSettingsFilePath Path to the build-logic settings file.
 */
data class ProjectModulesSource(
    val rootSettingsFilePath: String,
    val buildLogicSettingsFilePath: String
)
