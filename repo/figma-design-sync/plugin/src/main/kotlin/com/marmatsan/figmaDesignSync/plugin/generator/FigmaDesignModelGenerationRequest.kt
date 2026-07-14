package com.marmatsan.figmaDesignSync.plugin.generator

import java.io.File
import java.time.Instant

/**
 * Complete input snapshot required to generate `design-model.json`.
 *
 * File and directory values live in the plugin layer because they are Gradle
 * task inputs. The generator immediately translates them to domain source
 * objects before crossing into ports.
 *
 * @property branch Current Git branch recorded in the generated metadata.
 * @property gitSha Current Git commit recorded in the generated metadata.
 * @property generatedAt Timestamp written for traceability.
 * @property versionsFile Source `repo/dependency-catalog/versions.properties` file.
 * @property rootSettingsFile Root `settings.gradle.kts`.
 * @property ciExternalTopologyFile Versioned external CI topology.
 * @property teamCityGeneratedConfigurationDirectory Effective TeamCity
 * configuration generated from `.teamcity/settings.kts`.
 * @property projectRootDirectory Repository root.
 * @property includedBuilds Included builds that contribute catalogs, modules,
 * and module dependency graphs.
 */
internal data class FigmaDesignModelGenerationRequest(
    val branch: String,
    val gitSha: String,
    val generatedAt: Instant,
    val versionsFile: File,
    val rootSettingsFile: File,
    val ciExternalTopologyFile: File,
    val teamCityGeneratedConfigurationDirectory: File,
    val projectRootDirectory: File,
    val includedBuilds: List<FigmaDesignModelIncludedBuildSource>
)
