package com.marmatsan.figmaDocumentationSync.plugin.generator

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
 * @property primaryCatalogModelName Stable JSON key for the project's main catalog.
 * @property dependencyCatalogProviderClassName Project-config catalog adapter.
 * @property ciDocumentationEnabled Whether the optional CI model is included.
 * @property versionsFile Source `repo/dependency-catalog/versions.properties` file.
 * @property rootSettingsFile Root `settings.gradle.kts`.
 * @property ciExternalTopologyFile Versioned external CI topology.
 * @property ciWindowsRuntimeFile Versioned Windows service runtime.
 * @property ciConfigurationModelName Stable JSON key for the configured CI adapter.
 * @property ciConfigurationProviderClassName Project-config CI adapter.
 * @property ciGeneratedConfigurationDirectory Effective generated CI configuration.
 * @property projectRootDirectory Repository root.
 * @property includedBuilds Included builds that contribute catalogs, modules,
 * and module dependency graphs.
 */
internal data class FigmaDesignModelGenerationRequest(
    val branch: String,
    val gitSha: String,
    val generatedAt: Instant,
    val primaryCatalogModelName: String,
    val dependencyCatalogProviderClassName: String,
    val ciDocumentationEnabled: Boolean,
    val ciConfigurationModelName: String?,
    val ciConfigurationProviderClassName: String?,
    val versionsFile: File,
    val rootSettingsFile: File,
    val ciExternalTopologyFile: File?,
    val ciWindowsRuntimeFile: File?,
    val ciGeneratedConfigurationDirectory: File?,
    val projectRootDirectory: File,
    val includedBuilds: List<FigmaDesignModelIncludedBuildSource>
)
