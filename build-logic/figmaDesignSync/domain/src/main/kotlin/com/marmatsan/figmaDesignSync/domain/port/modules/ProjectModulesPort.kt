package com.marmatsan.figmaDesignSync.domain.port.modules

/**
 * Port for reading the set of Gradle modules declared by settings sources.
 *
 * The module list is written into `design-model.json` so Figma can document the
 * repository structure alongside dependency graphs.
 *
 * Example:
 * ```
 * port.readModules(
 *     ProjectModulesSource(
 *         rootSettingsFilePath = "settings.gradle.kts",
 *         buildLogicSettingsFilePath = "build-logic/settings.gradle.kts"
 *     )
 * )
 * ```
 */
interface ProjectModulesPort {
    fun readModules(source: ProjectModulesSource): Set<String>
}
