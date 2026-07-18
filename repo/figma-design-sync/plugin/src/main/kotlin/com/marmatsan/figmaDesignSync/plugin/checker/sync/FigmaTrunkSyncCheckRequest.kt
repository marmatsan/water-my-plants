package com.marmatsan.figmaDesignSync.plugin.checker.sync

import com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelIncludedBuildSource
import java.io.File
import java.time.Instant

/**
 * Input snapshot used to verify that the Figma document is synced with the
 * current repository state.
 *
 * The source fields mirror [com.marmatsan.figmaDesignSync.plugin.generator.FigmaDesignModelGenerationRequest],
 * with the additional Figma metadata node URL and API token needed to read
 * shared plugin data.
 */
internal data class FigmaTrunkSyncCheckRequest(
    val metadataNodeUrl: String,
    val token: String,
    val metadataNamespace: String,
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
